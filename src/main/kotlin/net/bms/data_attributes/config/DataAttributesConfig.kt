package net.bms.data_attributes.config

import me.fzzyhmstrs.fzzy_config.annotations.NonSync
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedIdentifierMap
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedMap
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.api.DataAttributesAPI
import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.models.AttributeOverride
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

@RootConfig
@Version(1)
@Translatable.Name("Data Attributes Config")
@Translatable.Prefix("Welcome to the Data Attributes config. Hey, hey! You can view your own custom config and other configs here.")
@Translatable.Desc("A robust config for Data Attributes.")
class DataAttributesConfig : Config(ResourceLocation.fromNamespaceAndPath(DataAttributes.MOD_ID, "config")) {

    @Translatable.Name("Server Config")
    @Translatable.Prefix("Entries managed by the server directly. Will take precedent over any provided config from a mod or datapack.")
    @Translatable.Desc("You can set a entry and have it override on the server immediately when you save your changes. ")
    class CustomAttributeManagementSection : ConfigSection() {
        @Translatable.Name("Attribute Overrides")
        @Translatable.Prefix("Overrides of currently registered attributes within Minecraft itself.")
        @Translatable.Desc("An attribute override will safely hook onto an existing attribute, and overwrite its behavior, adjusting it to what you see fit.")
        var overrides = ValidatedIdentifierMap(
            DataAttributesAPI.serverManager.overrides,
            ValidatedIdentifier.ofRegistryKey(Registries.ATTRIBUTE),
            ValidatedAny(AttributeOverride())
        )

        @Translatable.Name("Attribute Functions")
        @Translatable.Prefix("Functions that operate the same as attribute modifier operations.")
        @Translatable.Desc("An attribute function will apply a specific additive or multiplicative behavior based on the value of its children.")
        var functions = ValidatedIdentifierMap(
            DataAttributesAPI.serverManager.functions,
            ValidatedIdentifier.ofRegistryKey(Registries.ATTRIBUTE),
            ValidatedIdentifierMap(
                mutableMapOf(),
                ValidatedIdentifier.ofRegistryKey(Registries.ATTRIBUTE),
                ValidatedAny(AttributeFunction()
                )
            )
        )

        // todo: might need to remove EntityTypeData entirely and replace it with a direct Map, also... need to somehow build it with a helper.
        @Translatable.Name("Entity Attributes")
        @Translatable.Prefix("Provides the ability to hook any attribute you want onto any entity.")
        @Translatable.Desc("An entity attribute can be added onto any entity, allowing it to be fetched, or modified.")
        var entities = ValidatedIdentifierMap(
        DataAttributesAPI.serverManager.entityTypes,
        ValidatedIdentifier.ofRegistryKey(Registries.ENTITY_TYPE),
            ValidatedAny(EntityTypeData())
        )
    }


    @Translatable.Name("Existing Configs")
    @Translatable.Prefix("Entries provided by a loaded datapack or mod. Volatile and can not be modified in any meaningful way in this config.")
    @Translatable.Desc("In order for any of these configs to receive updates, you need to unload/load/reload datapacks. You may do that by refreshing via. command or restarting your server. Priority matters.")
    class ExistingDatapackConfigs : ConfigSection() {

    }

    var serverConfig = CustomAttributeManagementSection()

    @NonSync
    // datapacks come from other mods, and they should not be considered in sync to client.
    // todo: we will most likely have to flush the config with updated data-pack information but that's a low priority.
    var datapackConfigs = ExistingDatapackConfigs()

    override fun onUpdateServer(context: ServerUpdateContext) {
        DataAttributes.reload(context.getServer())
    }

    override fun fileType(): FileType {
        return FileType.JSON5
    }
}