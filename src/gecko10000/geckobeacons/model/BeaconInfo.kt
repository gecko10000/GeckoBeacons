package gecko10000.geckobeacons.model

import gecko10000.geckobeacons.config.BeaconEffect
import kotlinx.serialization.Serializable

@Serializable
data class BeaconInfo(
    val activeEffects: Set<BeaconEffect>,
)
