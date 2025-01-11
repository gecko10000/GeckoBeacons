package gecko10000.betterbeacons.guis

import com.google.common.collect.HashMultimap
import gecko10000.betterbeacons.BeaconManager
import gecko10000.betterbeacons.BetterBeacons
import gecko10000.betterbeacons.config.BeaconEffect
import gecko10000.betterbeacons.di.MyKoinComponent
import gecko10000.betterbeacons.model.CustomBeacon
import gecko10000.geckolib.GUI
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.extensions.smartS
import gecko10000.geckolib.extensions.withDefaults
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.misc.Task
import kotlin.properties.Delegates

@Suppress("UnstableApiUsage") // WELCOME TO THE DANGER ZONE
class BeaconGUI(
    player: Player,
    private var beacon: CustomBeacon,
) : GUI(player),
    MyKoinComponent {

    companion object {
        const val SIZE = 54
    }

    private val plugin: BetterBeacons by inject()
    private val beaconManager: BeaconManager by inject()
    private var pyramidLayers by Delegates.notNull<Int>()
    private var maxEffects by Delegates.notNull<Int>()
    private var activeEffects by Delegates.notNull<Int>()
    private var availableEffects by Delegates.notNull<Int>()

    private var selectedLevel: Int? = null
    private var selectedEffect: BeaconEffect? = null

    private fun glassItem(i: Int): ItemStack {
        val isUnlocked = i <= pyramidLayers
        val material = if (isUnlocked) Material.LIME_STAINED_GLASS_PANE
        else Material.RED_STAINED_GLASS_PANE
        val displayName = if (isUnlocked) parseMM("<green>Layer $i Unlocked")
        else parseMM("<red>Layer $i Locked")
        val item = ItemStack.of(material)
        item.editMeta {
            it.displayName(displayName)
            val cost = plugin.config.levelCosts[i]
            if (cost != null) {
                it.lore(
                    listOf(
                        MM.deserialize(
                            "<aqua>Cost: <amount> <item>",
                            Placeholder.unparsed("amount", cost.amount.toString()),
                            Placeholder.component("item", Component.translatable(cost.translationKey()))
                        ).withDefaults()
                    )
                )
            }
        }
        return item.asQuantity(i)
    }

    private fun createEffectItem(
        effect: BeaconEffect,
        isUnlocked: Boolean,
        isSelected: Boolean,
    ): ItemStack {
        val color = if (isSelected) "<green>✔ " else if (isUnlocked) "<yellow>\uD83D\uDD13 " else "<red>\uD83D\uDD12 "
        val name = MM.deserialize(
            "$color${plugin.config.effectTemplate}",
            Placeholder.component("effect", Component.translatable(effect.effect.translationKey())),
            Placeholder.unparsed("level", effect.level.toString())
        ).withDefaults()
        val item = ItemStack.of(plugin.config.effectMaterialMappings[effect.effect] ?: Material.BARRIER)
        item.setData(DataComponentTypes.MAX_STACK_SIZE, effect.level)
        //val f = ItemFlag.item.unsetData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
        item.amount = effect.level
        item.editMeta {
            it.displayName(name)
            if (isSelected) {
                it.setEnchantmentGlintOverride(true)
            }
            // it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            // https://discord.com/channels/289587909051416579/555462289851940864/1323653780717305888
            it.attributeModifiers = HashMultimap.create()
        }
        return item
    }

    private fun isHigherLevelSelected(effect: BeaconEffect): Boolean {
        return beacon.beaconInfo.activeEffects.any { it.effect == effect.effect && it.level > effect.level }
    }

    private fun createEffectButton(
        isUnlocked: Boolean,
        layerRequirement: Int,
        effect: BeaconEffect,
        isSelected: Boolean
    ): ItemButton {
        val item = createEffectItem(effect, isUnlocked, isSelected)
        return ItemButton.create(item) { e ->
            if (isHigherLevelSelected(effect)) {
                this.selectedEffect = null
                this.selectedLevel = null
                updateInventory()
                return@create
            }
            if (isSelected) {
                ActiveEffectsGUI(player, beacon)
                return@create
            }
            if (isUnlocked && availableEffects > 0) {
                this.selectedEffect = effect
                this.selectedLevel = layerRequirement
                updateInventory()
            }
        }
    }

    private fun effectsLeftButton(): ItemButton {
        val item = if (availableEffects <= 0) {
            ItemStack.of(Material.BARRIER)
        } else {
            ItemStack.of(Material.NETHER_STAR, availableEffects)
        }
        item.editMeta {
            it.displayName(parseMM(if (availableEffects == 0) "<red>$maxEffects/$maxEffects effect${maxEffects.smartS()} used" else "<green>$activeEffects/$maxEffects effect${maxEffects.smartS()} used"))
            it.lore(
                buildList {
                    add(parseMM("<yellow>Extra effects are unlocked every ${plugin.config.levelsPerEffect} layers."))
                    if (availableEffects <= 0 && maxEffects > 0) {
                        add(parseMM("<red>Disable an effect before selecting more."))
                    }
                }
            )
        }
        return ItemButton.create(item) { _ ->
            if (activeEffects == 0) return@create
            ActiveEffectsGUI(player, beacon)
        }
    }

    private fun createCostItem(level: Int): ItemStack {
        val item = plugin.config.levelCosts[level]?.clone() ?: return ItemStack.of(Material.BARRIER)
        item.editMeta {
            it.displayName(
                MM.deserialize(
                    "<aqua>Cost: ${item.amount} <item>",
                    Placeholder.component("item", Component.translatable(item.translationKey()))
                ).withDefaults()
            )
        }
        return item
    }

    private fun costBorderItem(isPaymentReady: Boolean): ItemStack {
        if (isPaymentReady) {
            val item = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
            item.editMeta {
                it.displayName(parseMM("<green>Payment ready for processing"))
            }
            return item
        }
        val item = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<aqua>Insert payment here"))
        }
        return item
    }

    private fun checkPaymentReady(): Boolean {
        val level = selectedLevel ?: return false
        val requiredItem = plugin.config.levelCosts[level] ?: return false
        val paymentItem = inventory.inventory.getItem(51) ?: return false
        return requiredItem.isSimilar(paymentItem) && paymentItem.amount >= requiredItem.amount
    }

    private fun confirmButton(): ItemButton {
        val level = selectedLevel!!
        val effect = selectedEffect!!
        val cost = plugin.config.levelCosts[level]!!
        val item = ItemStack.of(Material.LIME_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<green>Confirm purchase"))
            it.lore(
                listOf(
                    MM.deserialize(
                        "<gold>You are activating <yellow><bold><effect> <level>",
                        Placeholder.component(
                            "effect",
                            Component.translatable(effect.effect.translationKey())
                        ),
                        Placeholder.unparsed("level", effect.level.toString())
                    ).withDefaults(),
                    MM.deserialize(
                        "<gold>for <yellow><u><amount> <item>",
                        Placeholder.unparsed("amount", cost.amount.toString()),
                        Placeholder.component("item", Component.translatable(cost.translationKey()))
                    ).withDefaults()
                )
            )
        }
        return ItemButton.create(item) { e ->
            val isBeaconStillValid = beaconManager.getPyramidLayerCount(beacon.block) >= level
            if (!isBeaconStillValid) {
                player.closeInventory()
                return@create
            }
            val costItem = inventory.inventory.getItem(51) ?: return@create
            costItem.amount -= cost.amount
            beacon = beaconManager.enableEffect(beacon, effect)
            selectedLevel = null
            selectedEffect = null
            updateInventory()
        }
    }

    private fun updateInventory(guiToUse: InventoryGUI? = null) {
        pyramidLayers = beaconManager.getPyramidLayerCount(beacon.block)
        maxEffects = if (pyramidLayers == 0) 0 else pyramidLayers / plugin.config.levelsPerEffect + 1
        activeEffects = beacon.beaconInfo.activeEffects.size
        availableEffects = maxEffects - activeEffects
        val gui = guiToUse ?: this.inventory
        gui.fill(0, 51, FILLER)
        gui.fill(52, SIZE, FILLER)
        gui.addButton(45, effectsLeftButton())
        val isPaymentReady = checkPaymentReady()
        for (i in setOf(41, 42, 43, 50, 52)) {
            gui.inventory.setItem(i, costBorderItem(isPaymentReady))
        }
        if (isPaymentReady) {
            gui.addButton(53, confirmButton())
        } else {
            val existingButton = gui.getButton(53)
            existingButton?.let { gui.removeButton(it) }
        }
        for (i in 0..<plugin.config.maxPyramidSize) {
            gui.inventory.setItem(i, glassItem(i + 1))
            val beaconEffects = plugin.config.beaconEffectLevels[i + 1] ?: continue
            var slot = i
            for (effect in beaconEffects) {
                slot += 9
                gui.addButton(
                    slot,
                    createEffectButton(
                        i < pyramidLayers,
                        i + 1,
                        effect,
                        effect in beacon.beaconInfo.activeEffects
                    )
                )
            }
        }
        if (selectedLevel != null && selectedEffect != null) {
            gui.inventory.setItem(47, createEffectItem(selectedEffect!!, isSelected = true, isUnlocked = true))
            gui.inventory.setItem(48, createCostItem(selectedLevel!!))
        }
    }

    override fun createInventory(): InventoryGUI {
        val gui = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.guiName))
        gui.openSlot(51)
        updateInventory(gui)
        gui.setOnClickOpenSlot { _ ->
            Task.syncDelayed { ->
                updateInventory()
            }
        }
        return gui
    }
}
