package net.bms.data_attributes.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import net.bms.data_attributes.DataAttributesClient
import net.bms.data_attributes.config.impl.AttributeConfigManager

object Configs {
    lateinit var CONFIG: DataAttributesConfig
        private set

    @JvmStatic
    fun init() {
        if (::CONFIG.isInitialized) return
        CONFIG = ConfigApi.registerAndLoadConfig(::DataAttributesConfig)
        ConfigState.replace(CONFIG.toSnapshot())

        // Datapack previews are carried by Data Attributes' own server snapshot.
        // Keeping them out of Fzzy's persisted/synced config prevents informational
        // state from becoming user configuration or being written back to disk.
        DataAttributesClient.installDatapackPreviewSink(::refreshDatapackPreview)
        refreshDatapackPreview(DataAttributesClient.MANAGER.datapackDefaults)
    }

    @JvmStatic
    fun refreshDatapackPreview(data: AttributeConfigManager.Data) {
        if (::CONFIG.isInitialized) CONFIG.refreshDatapackPreview(data)
    }
}
