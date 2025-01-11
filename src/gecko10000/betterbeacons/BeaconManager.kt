package gecko10000.betterbeacons

import gecko10000.betterbeacons.config.BeaconEffect
import gecko10000.betterbeacons.di.MyKoinComponent
import gecko10000.betterbeacons.guis.BeaconGUI
import gecko10000.betterbeacons.model.BeaconInfo
import gecko10000.betterbeacons.model.CustomBeacon
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Beacon
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.koin.core.component.inject
import redempt.redlib.misc.EventListener
import redempt.redlib.misc.Task

class BeaconManager : MyKoinComponent {

    private val plugin: BetterBeacons by inject()
    private val json: Json by inject()
    private val beaconKey = NamespacedKey(plugin, "bb")

    private val loadedBeacons: MutableMap<Block, CustomBeacon> = mutableMapOf()

    init {
        Bukkit.getWorlds()
            .flatMap { it.loadedChunks.toList() }
            .forEach(this::load)
        EventListener(ChunkLoadEvent::class.java) { e -> load(e.chunk) }
        EventListener(ChunkUnloadEvent::class.java) { e ->
            loadedBeacons.keys
                .filter { it.chunk == e.chunk }
                .forEach { loadedBeacons.remove(it) }
        }
        EventListener(BlockPlaceEvent::class.java) { e ->
            val state = e.block.state
            if (state is Beacon)
                load(state)
        }
        EventListener(BlockBreakEvent::class.java) { e ->
            if (e.block.type != Material.BEACON) return@EventListener
            removeBeacon(e.block)
        }
        EventListener(EntityExplodeEvent::class.java) { e -> cancelExplosion(e.blockList()) }
        EventListener(BlockExplodeEvent::class.java) { e -> cancelExplosion(e.blockList()) }
        EventListener(PlayerInteractEvent::class.java, EventPriority.MONITOR) { e ->
            if (e.useInteractedBlock() == Event.Result.DENY) return@EventListener
            if (e.action != Action.RIGHT_CLICK_BLOCK) return@EventListener
            val block = e.clickedBlock
            if (block?.type != Material.BEACON) return@EventListener
            e.setUseInteractedBlock(Event.Result.DENY)
            val beacon = get(block) ?: return@EventListener
            BeaconGUI(e.player, beacon)
        }
        Task.syncRepeating(
            { ->
                for (map in loadedBeacons.values) {
                    doEffects(map)
                }
            },
            0L, plugin.config.effectApplicationIntervalSeconds * 20L
        )
    }

    private fun cancelExplosion(blocks: MutableList<Block>) {
        blocks.removeIf { it.type == Material.BEACON }
    }

    private fun findPyramidSize(effect: BeaconEffect): Int {
        return plugin.config.beaconEffectLevels.entries.first { effect in it.value }.key
    }

    private fun doEffects(customBeacon: CustomBeacon) {
        val pyramidSize = getPyramidLayerCount(customBeacon.block)
        val nearbyPlayers =
            customBeacon.block.location.getNearbyPlayers(plugin.config.beaconRanges[pyramidSize]!!.toDouble())
        for (effect in customBeacon.beaconInfo.activeEffects) {
            val neededSize = findPyramidSize(effect)
            if (pyramidSize < neededSize) {
                disableEffect(customBeacon, effect)
                continue
            }
            val potionEffect =
                PotionEffect(effect.effect, plugin.config.effectDurationSeconds * 20, effect.level - 1, true, false)
            nearbyPlayers.forEach { it.addPotionEffect(potionEffect) }
        }
    }

    private fun createBeacon(block: Block): CustomBeacon {
        val beacon = CustomBeacon(block, BeaconInfo(setOf()))
        addBeacon(beacon)
        return beacon
    }

    private fun addBeacons(beacons: Set<CustomBeacon>) = beacons.forEach(this::addBeacon)

    private fun addBeacon(beacon: CustomBeacon) {
        loadedBeacons[beacon.block] = beacon
    }

    private fun removeBeacon(beacon: Block) {
        loadedBeacons.remove(beacon)
    }

    private fun load(chunk: Chunk) {
        val beaconStates = chunk.getTileEntities({ it.type == Material.BEACON }, false)
            .filterIsInstance<Beacon>()
        beaconStates.forEach {
            it.setPrimaryEffect(null)
            it.setSecondaryEffect(null)
        }
        val beacons = beaconStates
            .map(this::load)
        addBeacons(beacons.toSet())
    }

    private fun load(beacon: Beacon): CustomBeacon {
        val infoString = beacon.persistentDataContainer.get(beaconKey, PersistentDataType.STRING)
            ?: return createBeacon(beacon.block)
        val beaconInfo = json.decodeFromString<BeaconInfo>(infoString)
        return CustomBeacon(block = beacon.block, beaconInfo = beaconInfo)
    }

    private fun save(customBeacon: CustomBeacon) {
        val beacon = customBeacon.block.state as? Beacon ?: return
        val serializedInfo = json.encodeToString(customBeacon.beaconInfo)
        beacon.persistentDataContainer.set(beaconKey, PersistentDataType.STRING, serializedInfo)
        beacon.update()
    }

    private fun get(block: Block): CustomBeacon? {
        return loadedBeacons[block]
    }

    private fun update(customBeacon: CustomBeacon, newInfo: BeaconInfo): CustomBeacon {
        val newBeacon = customBeacon.copy(beaconInfo = newInfo)
        addBeacon(newBeacon)
        save(newBeacon)
        return newBeacon
    }

    fun enableEffect(beacon: CustomBeacon, effect: BeaconEffect): CustomBeacon {
        val newBeacon = update(
            beacon,
            newInfo = beacon.beaconInfo.copy(activeEffects = beacon.beaconInfo.activeEffects.filter { it.effect != effect.effect }
                .toSet().plus(effect))
        )
        doEffects(newBeacon)
        return newBeacon
    }

    fun disableEffect(beacon: CustomBeacon, effect: BeaconEffect): CustomBeacon {
        return update(
            beacon,
            newInfo = beacon.beaconInfo.copy(activeEffects = beacon.beaconInfo.activeEffects.minus(effect))
        )
    }

    fun getPyramidLayerCount(beaconBlock: Block): Int {
        var layerCount = 0
        var minCorner = beaconBlock
        var maxCorner = beaconBlock
        while (true) {
            minCorner = minCorner.getRelative(-1, -1, -1)
            maxCorner = maxCorner.getRelative(1, -1, 1)
            for (x in minCorner.x..maxCorner.x) {
                for (z in minCorner.z..maxCorner.z) {
                    val block = beaconBlock.world.getBlockAt(x, minCorner.y, z)
                    if (block.type !in plugin.config.pyramidBlocks) return layerCount
                }
            }
            layerCount++
            if (layerCount == plugin.config.maxPyramidSize) return layerCount
        }
    }

}
