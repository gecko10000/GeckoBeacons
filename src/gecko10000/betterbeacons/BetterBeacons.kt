package gecko10000.betterbeacons

import gecko10000.betterbeacons.config.Config
import gecko10000.betterbeacons.di.MyKoinContext
import gecko10000.geckolib.config.YamlFileManager
import org.bukkit.plugin.java.JavaPlugin

class BetterBeacons : JavaPlugin() {

    private val configFile = YamlFileManager(
        configDirectory = dataFolder,
        initialValue = Config(),
        serializer = Config.serializer(),
    )

    val config: Config
        get() = configFile.value

    override fun onEnable() {
        MyKoinContext.init(this)
    }

    override fun onDisable() {
    }

    fun reloadConfigs() {
        configFile.reload()
    }

}
