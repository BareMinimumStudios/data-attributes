package net.bms.data_attributes.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi

object Configs {
    @JvmField val CONFIG = ConfigApi.registerAndLoadConfig(::DataAttributesConfig)

    fun init() {}
}