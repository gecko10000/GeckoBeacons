package gecko10000.betterbeacons.model

import gecko10000.betterbeacons.config.BeaconEffect
import kotlinx.serialization.Serializable

@Serializable
data class BeaconInfo(
    val activeEffects: Set<BeaconEffect>,
)
