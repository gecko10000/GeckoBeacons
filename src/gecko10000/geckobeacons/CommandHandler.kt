package gecko10000.geckobeacons

import gecko10000.geckobeacons.di.MyKoinComponent
import gecko10000.geckolib.extensions.parseMM
import org.bukkit.command.CommandSender
import org.koin.core.component.inject
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class CommandHandler : MyKoinComponent {

    private val plugin: GeckoBeacons by inject()

    init {
        CommandParser(plugin.getResource("command.rdcml")).parse().register("mm", this)
    }

    @CommandHook("reload")
    fun reload(sender: CommandSender) {
        plugin.reloadConfigs()
        sender.sendMessage(parseMM("<green>Configs reloaded."))
    }
}
