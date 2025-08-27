package com.dongzh1.com

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.chat.BuiltInConfiguration
import org.bukkit.configuration.Configuration

@Suppress("unused")
class EasyDialog : EasyPlugin() {
    companion object {
        lateinit var lang: Configuration
    }

    override fun load() {
        super.load()
        lang = BuiltInConfiguration("language.yml")
        saveResource("dialogs/help.json", false)
        saveDefaultConfig()
    }

    override fun enable() {

    }

}
