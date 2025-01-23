@file:UseSerializers(
    MMComponentSerializer::class,
    PotionEffectTypeSerializer::class,
    ItemStackSerializer::class
)

package gecko10000.geckobeacons.config

import gecko10000.geckolib.config.serializers.ItemStackSerializer
import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.config.serializers.PotionEffectTypeSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

@Serializable
data class Config(
    val guiName: Component = MM.deserialize("<dark_green>Beacon"),
    val activeEffectsName: Component = MM.deserialize("<dark_green>Active Effects"),
    val pyramidBlocks: Set<Material> = setOf(
        Material.IRON_BLOCK,
        Material.GOLD_BLOCK,
        Material.DIAMOND_BLOCK,
        Material.EMERALD_BLOCK,
        Material.NETHERITE_BLOCK,
    ),
    val effectTemplate: String = "<effect>: Level <level>",
    val maxPyramidSize: Int = 9,
    // ~Night vision, ~fire resistance,
    // ~speed, ~dolphin's grace,
    // ~haste, ~invisibility,
    // ~jump boost, ~regeneration, ~resistance,
    // ~saturation, strength, water breathing
    val beaconEffectLevels: Map<Int, Set<BeaconEffect>> = mapOf(
        1 to setOf(
            BeaconEffect(PotionEffectType.NIGHT_VISION, 1),
            BeaconEffect(PotionEffectType.DOLPHINS_GRACE, 1),
        ),
        2 to setOf(
            BeaconEffect(PotionEffectType.FIRE_RESISTANCE, 1),
            BeaconEffect(PotionEffectType.DOLPHINS_GRACE, 2),
        ),
        3 to setOf(
            BeaconEffect(PotionEffectType.WATER_BREATHING, 1),
            BeaconEffect(PotionEffectType.STRENGTH, 1),
            BeaconEffect(PotionEffectType.JUMP_BOOST, 1),
        ),
        4 to setOf(
            BeaconEffect(PotionEffectType.SPEED, 1),
            BeaconEffect(PotionEffectType.DOLPHINS_GRACE, 3),
            BeaconEffect(PotionEffectType.HASTE, 1),
        ),
        5 to setOf(
            BeaconEffect(PotionEffectType.RESISTANCE, 1),
            BeaconEffect(PotionEffectType.INVISIBILITY, 1),
            BeaconEffect(PotionEffectType.JUMP_BOOST, 2),
        ),
        6 to setOf(
            BeaconEffect(PotionEffectType.SPEED, 2),
            BeaconEffect(PotionEffectType.DOLPHINS_GRACE, 4),
        ),
        7 to setOf(
            BeaconEffect(PotionEffectType.REGENERATION, 1),
            BeaconEffect(PotionEffectType.STRENGTH, 2),
            BeaconEffect(PotionEffectType.HASTE, 2),
        ),
        8 to setOf(
            BeaconEffect(PotionEffectType.SPEED, 3),
            BeaconEffect(PotionEffectType.DOLPHINS_GRACE, 5),
            BeaconEffect(PotionEffectType.JUMP_BOOST, 3),
        ),
        9 to setOf(
            BeaconEffect(PotionEffectType.RESISTANCE, 2),
            BeaconEffect(PotionEffectType.SATURATION, 1),
            BeaconEffect(PotionEffectType.HASTE, 3),
        ),
    ),
    val levelCosts: Map<Int, ItemStack> = mapOf(
        1 to ItemStack.of(Material.DIAMOND, 2),
        2 to ItemStack.of(Material.DIAMOND, 4),
        3 to ItemStack.of(Material.DIAMOND, 8),
        4 to ItemStack.of(Material.DIAMOND, 16),
        5 to ItemStack.of(Material.DIAMOND, 32),
        6 to ItemStack.of(Material.DIAMOND, 64),
        7 to ItemStack.of(Material.DIAMOND_BLOCK, 16),
        8 to ItemStack.of(Material.DIAMOND_BLOCK, 32),
        9 to ItemStack.of(Material.DIAMOND_BLOCK, 64),
    ),
    val effectMaterialMappings: Map<PotionEffectType, Material> = mapOf(
        PotionEffectType.NIGHT_VISION to Material.GLOW_ITEM_FRAME,
        PotionEffectType.DOLPHINS_GRACE to Material.HEART_OF_THE_SEA,
        PotionEffectType.FIRE_RESISTANCE to Material.NETHERITE_INGOT,
        PotionEffectType.SPEED to Material.SUGAR,
        PotionEffectType.JUMP_BOOST to Material.RABBIT_FOOT,
        PotionEffectType.STRENGTH to Material.ANVIL,
        PotionEffectType.HASTE to Material.NETHERITE_PICKAXE,
        PotionEffectType.INVISIBILITY to Material.WIND_CHARGE,
        PotionEffectType.RESISTANCE to Material.BEDROCK,
        PotionEffectType.REGENERATION to Material.GOLDEN_APPLE,
        PotionEffectType.WATER_BREATHING to Material.TURTLE_HELMET,
        PotionEffectType.SATURATION to Material.GOLDEN_CARROT,
    ),
    val levelsPerEffect: Int = 3,
    val effectApplicationIntervalSeconds: Int = 8,
    val effectDurationSeconds: Int = 20,
    val beaconRanges: Map<Int, Int> = mapOf(
        0 to 0,
        1 to 20,
        2 to 30,
        3 to 40,
        4 to 50,
        5 to 60,
        5 to 70,
        6 to 80,
        7 to 90,
        8 to 100,
        9 to 120,
    )
)
