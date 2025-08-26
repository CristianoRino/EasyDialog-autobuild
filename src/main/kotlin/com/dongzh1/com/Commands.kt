package com.dongzh1.com

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.easylib.util.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

@ECommandHeader(command = "dialog")
object Commands {

    private val dialogs = ArgNode("dialogs", exec = {
        File(plugin.dataFolder,"dialogs").list()?.toList() ?: emptyList()
    }, parse = {
        it
    })

    @CommandBody
    private val show = command<CommandSender>("show") {
        description = EasyDialog.lang.getString("commands.show.description")
        arg(dialogs){ dArg->
            players(optional = true) {pArg->
                exec {
                    val fileName = dArg.value()
                    if (!sender.hasPermission("dialog.show.$fileName")){
                        sender.sendMessage(EasyDialog.lang.getString("warn.permission")?.replace("{permission}","dialog.show.$fileName")?:"")
                        return@exec
                    }
                    var p = if (sender is Player) sender as Player else null
                    val player = pArg.valueOrNull()
                    if (player != null) {
                        if (!sender.hasPermission("dialog.show.admin")){
                            sender.sendMessage(EasyDialog.lang.getString("warn.permission")?.replace("{permission}","dialog.show.admin")?:"")
                            return@exec
                        }
                        p = player
                    }
                    if (p == null){
                        sender.sendMessage(EasyDialog.lang.getString("commands.show.noPlayer")?:"")
                        return@exec
                    }
                    val file = File(plugin.dataFolder,"dialogs/${fileName}")
                    val string = file.readText(Charsets.UTF_8).replace("\r", "").replace("\n","").replace("\r\n","").replacePlaceholder(p)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"minecraft:dialog show ${sender.name} $string")
                }
            }
        }

    }
}