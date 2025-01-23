package gecko10000.geckobeacons.di

import gecko10000.geckobeacons.GeckoBeacons
import org.koin.core.Koin
import org.koin.dsl.koinApplication

object MyKoinContext {
    internal lateinit var koin: Koin
    fun init(plugin: GeckoBeacons) {
        koin = koinApplication(createEagerInstances = false) { modules(pluginModules(plugin)) }.koin
        koin.createEagerInstances()
    }

}
