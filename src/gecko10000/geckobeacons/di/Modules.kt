package gecko10000.geckobeacons.di

import gecko10000.geckobeacons.BeaconManager
import gecko10000.geckobeacons.CommandHandler
import gecko10000.geckobeacons.GeckoBeacons
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: GeckoBeacons) = module {
    single { plugin }
    single(createdAtStart = true) { BeaconManager() }
    single(createdAtStart = true) { CommandHandler() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
