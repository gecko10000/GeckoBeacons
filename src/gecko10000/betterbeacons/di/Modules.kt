package gecko10000.betterbeacons.di

import gecko10000.betterbeacons.BeaconManager
import gecko10000.betterbeacons.BetterBeacons
import gecko10000.betterbeacons.CommandHandler
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: BetterBeacons) = module {
    single { plugin }
    single(createdAtStart = true) { BeaconManager() }
    single(createdAtStart = true) { CommandHandler() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
