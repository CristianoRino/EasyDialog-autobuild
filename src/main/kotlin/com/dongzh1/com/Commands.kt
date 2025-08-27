package com.dongzh1.com

import com.google.gson.*
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.easylib.util.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

@ECommandHeader(command = "dialog")
object Commands {

    private val dialogs = ArgNode("dialogs", exec = {
        File(plugin.dataFolder, "dialogs").list()?.toList() ?: emptyList()
    }, parse = {
        it
    })

    /**
     * 处理JSON文件，移除不必要的空白字符但保留字符串内的转义字符
     * @param fileName 要处理的JSON文件名
     * @param prettyPrint 是否格式化输出（默认为false，生成紧凑JSON）
     * @return 处理后的JSON字符串，如果处理失败则返回null
     */
    private fun processJsonFile(fileName: String, prettyPrint: Boolean = false): String? {
        val file = File(plugin.dataFolder, "dialogs/$fileName")

        // 1. 读取文件内容
        val fileContent = try {
            file.readText(StandardCharsets.UTF_8)
        } catch (e: IOException) {
            plugin.logger.severe("无法读取文件: ${file.absolutePath}")
            plugin.logger.severe("错误信息: ${e.message}")
            return null
        }

        // 2. 解析JSON内容
        val jsonElement: JsonElement = try {
            JsonParser.parseString(fileContent)
        } catch (e: JsonSyntaxException) {
            plugin.logger.severe("JSON语法错误: ${e.message}")
            plugin.logger.warning("原始JSON内容: ${fileContent.take(100)}...") // 只记录前100个字符
            return null
        } catch (e: Exception) {
            plugin.logger.severe("解析JSON时发生未知错误: ${e.message}")
            return null
        }

        // 3. 配置Gson实例
        val gson: Gson = if (prettyPrint) {
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping() // 可选：防止HTML字符被转义
                .create()
        } else {
            Gson() // 紧凑格式
        }

        // 4. 序列化回字符串
        return try {
            gson.toJson(jsonElement)
        } catch (e: Exception) {
            plugin.logger.severe("序列化JSON时发生错误: ${e.message}")
            null
        }
    }

    @CommandBody
    private val show = command<CommandSender>("show") {
        description = EasyDialog.lang.getString("commands.show.description")
        arg(dialogs) { dArg ->
            players(optional = true) { pArg ->
                exec {
                    val fileName = dArg.value()
                    if (!sender.hasPermission("dialog.show.$fileName")) {
                        sender.sendMessage(
                            EasyDialog.lang.getString("warn.permission")
                                ?.replace("{permission}", "dialog.show.$fileName") ?: ""
                        )
                        return@exec
                    }
                    var p = if (sender is Player) sender as Player else null
                    val player = pArg.valueOrNull()
                    if (player != null) {
                        if (!sender.hasPermission("dialog.show.admin")) {
                            sender.sendMessage(
                                EasyDialog.lang.getString("warn.permission")
                                    ?.replace("{permission}", "dialog.show.admin") ?: ""
                            )
                            return@exec
                        }
                        p = player
                    }
                    if (p == null) {
                        sender.sendMessage(EasyDialog.lang.getString("commands.show.noPlayer") ?: "")
                        return@exec
                    }
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "minecraft:dialog show ${sender.name} ${processJsonFile(fileName)}"
                    )
                }
            }
        }

    }
}