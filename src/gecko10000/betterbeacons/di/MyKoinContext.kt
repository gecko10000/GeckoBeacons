package gecko10000.betterbeacons.di

import gecko10000.betterbeacons.BetterBeacons
import org.koin.core.Koin
import org.koin.dsl.koinApplication

object MyKoinContext {
    internal lateinit var koin: Koin
    fun init(plugin: BetterBeacons) {
        koin = koinApplication(createEagerInstances = false) { modules(pluginModules(plugin)) }.koin
        koin.createEagerInstances()
    }

}
