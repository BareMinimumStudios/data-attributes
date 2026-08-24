package net.bms.data_attributes

import net.bms.data_attributes.config.Configs
import net.fabricmc.api.ModInitializer

object DataAttributesFabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        Configs.init()
    }
}