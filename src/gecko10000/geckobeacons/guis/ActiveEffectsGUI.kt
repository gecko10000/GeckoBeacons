package gecko10000.geckobeacons.guis

import com.google.common.collect.HashMultimap
import gecko10000.geckobeacons.BeaconManager
import gecko10000.geckobeacons.GeckoBeacons
import gecko10000.geckobeacons.config.BeaconEffect
import gecko10000.geckobeacons.di.MyKoinComponent
import gecko10000.geckobeacons.model.CustomBeacon
import gecko10000.geckolib.GUI
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.extensions.withDefaults
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.itemutils.ItemUtils

@Suppress("UnstableApiUsage")
class ActiveEffectsGUI(player: Player, private var beacon: CustomBeacon) : GUI(player), MyKoinComponent {

    private val plugin: GeckoBeacons by inject()
    private val beaconManager: BeaconManager by inject()

    private fun effectDisableButton(effect: BeaconEffect): ItemButton {
        val item = ItemStack.of(plugin.config.effectMaterialMappings[effect.effect]!!)
        item.setData(DataComponentTypes.MAX_STACK_SIZE, effect.level)
        item.amount = effect.level
        item.editMeta {
            it.displayName(
                MM.deserialize(
                    "<green><effect> <level>",
                    Placeholder.component("effect", Component.translatable(effect.effect.translationKey())),
                    Placeholder.unparsed("level", effect.level.toString())
                ).withDefaults()
            )
            it.lore(
                listOf(
                    parseMM("<red>Click to disable."),
                    parseMM("<red>No refunds.")
                )
            )
            it.attributeModifiers = HashMultimap.create()
        }
        return ItemButton.create(item) { _ ->
            beacon = beaconManager.disableEffect(beacon, effect)
            updateInventory()
        }
    }

    private fun updateInventory(guiToUse: InventoryGUI? = null) {
        val gui = guiToUse ?: this.inventory
        gui.fill(0, gui.size, FILLER)
        gui.addButton(gui.size - 9, BACK { BeaconGUI(player, beacon) })
        var i = 0
        for (effect in beacon.beaconInfo.activeEffects) {
            gui.addButton(i++, effectDisableButton(effect))
        }
    }

    override fun createInventory(): InventoryGUI {
        val size = ItemUtils.minimumChestSize(beacon.beaconInfo.activeEffects.size) + 9
        val gui = InventoryGUI(Bukkit.createInventory(this, size, plugin.config.activeEffectsName))
        updateInventory(gui)
        return gui
    }
}
