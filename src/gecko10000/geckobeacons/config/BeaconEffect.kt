@file:UseSerializers(PotionEffectTypeSerializer::class)

package gecko10000.geckobeacons.config

import gecko10000.geckolib.config.serializers.PotionEffectTypeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.potion.PotionEffectType

@Serializable
data class BeaconEffect(
    val effect: PotionEffectType,
    val level: Int,
)
