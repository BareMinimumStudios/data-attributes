package net.bms.data_attributes.config

import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.entry.Entry
import me.fzzyhmstrs.fzzy_config.entry.EntryTransient
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext
import me.fzzyhmstrs.fzzy_config.util.AllowableIdentifiers
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedIdentifierMap
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber
import net.bms.data_attributes.DataAttributes
import net.bms.data_attributes.api.attribute.AttributeFormat
import net.bms.data_attributes.api.attribute.StackingBehavior
import net.bms.data_attributes.api.attribute.StackingFormula
import net.bms.data_attributes.config.entities.EntityTypeData
import net.bms.data_attributes.config.entities.EntityTypeEntry
import net.bms.data_attributes.config.functions.AttributeFunction
import net.bms.data_attributes.config.impl.AttributeConfigManager
import net.bms.data_attributes.config.models.AttributeOverride
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Fzzy Config front-end for Data Attributes.
 *
 * Registry suggestions are intentionally decoupled from validation. A syntactically
 * valid identifier is allowed even when its owning mod is absent or has not registered
 * yet; the runtime manager remains the authority and safely skips unresolved IDs.
 * This prevents one stale/optional key from blocking every other config edit.
 */
@RootConfig
@Version(3)
@Translatable.Name("Data Attributes")
@Translatable.Prefix("Server config wins over matching datapack entries. Accepted in-game changes apply live; /reload is only needed for datapack JSON edits. Datapack Baseline Preview is informational and never writes back into a datapack.")
@Translatable.Desc("Configure attribute bounds, parent/child functions, and entity base attributes. Registry suggestions are provided without making optional mod IDs fatal.")
class DataAttributesConfig : Config(ResourceLocation.fromNamespaceAndPath(DataAttributes.MOD_ID, "config")) {

    @Translatable.Name("Server Configuration")
    @Translatable.Prefix("These are the authoritative user overrides. Matching entries here replace the equivalent datapack value; entries not listed here continue to use datapack or vanilla behavior.")
    @Translatable.Desc("Editable server-owned Data Attributes configuration.")
    class ServerSection : ConfigSection() {
        @Translatable.Name("Attribute Definition Overrides")
        @Translatable.Prefix("Changes the Attribute class itself: allowed bounds, stacking behavior, smoothness, and display formatting. This section NEVER sets an entity base value. Use Entity Base Attributes below for that. Example: minecraft:generic.max_health")
        @Translatable.Desc("Highest-priority attribute-definition rules. Minimum/maximum are ceilings/floors for calculated values, not entity values. Unknown but syntactically valid IDs are retained for optional-mod compatibility and ignored until registered.")
        var overrides = ValidatedIdentifierMap(
            emptyMap(),
            attributeIdentifier(ATTRIBUTE_DEFAULT),
            ValidatedAny(AttributeOverrideConfig())
        )

        @Translatable.Name("Attribute Functions")
        @Translatable.Prefix("Outer key = parent/source attribute. Nested key = child/derived attribute that receives the contribution. Open each function to choose Add or Multiply and its coefficient.")
        @Translatable.Desc("Parent/child attribute relationships. Self-links and unresolved attributes are ignored safely at runtime.")
        var functions = ValidatedIdentifierMap(
            emptyMap(),
            attributeIdentifier(ATTRIBUTE_DEFAULT),
            ValidatedIdentifierMap(
                emptyMap(),
                attributeIdentifier(ATTRIBUTE_CHILD_DEFAULT),
                ValidatedAny(AttributeFunctionConfig())
            )
        )

        @Translatable.Name("Entity Base Attributes")
        @Translatable.Prefix("THIS is the section that sets actual entity attribute base values. Add an entity type/group, then add attribute IDs and the base value each entity should receive. Definition Overrides above only change allowed bounds/behavior.")
        @Translatable.Desc("Base values attached to explicit entity types or Data Attributes entity groups. Existing loaded entities adopt supplier-owned base changes on their next tick.")
        var entities = ValidatedIdentifierMap(
            emptyMap(),
            entityIdentifier(ENTITY_DEFAULT),
            ValidatedAny(EntityTypeConfig())
        )
    }

    @Translatable.Name("Attribute Override")
    @Translatable.Prefix("Definition-level behavior only. This object does not set the base/current value of any entity. Native bounds can be inherited independently.")
    @Translatable.Desc("Definition rules for one registered attribute: allowed bounds, stacking and formatting.")
    class AttributeOverrideConfig {
        @Translatable.Name("Enabled")
        @Translatable.Desc("Master switch for this override. Disabled entries remain in the config but do not clamp or alter the target attribute.")
        var enabled = ValidatedBoolean(true)

        @Translatable.Name("Bounds")
        var bounds = ConfigGroup("bounds", false)

        @Translatable.Name("Override Minimum")
        @Translatable.Desc("When off, Data Attributes inherits the attribute's native minimum.")
        var overrideMin = ValidatedBoolean(false)

        @Translatable.Name("Allowed Minimum")
        @Translatable.Desc("Definition floor used only when Override Minimum is enabled. This does NOT set an entity's attribute value.")
        var min = ValidatedDouble(0.0)

        @Translatable.Name("Override Maximum")
        @Translatable.Desc("When off, Data Attributes inherits the attribute's native maximum.")
        var overrideMax = ValidatedBoolean(false)

        @ConfigGroup.Pop
        @Translatable.Name("Allowed Maximum")
        @Translatable.Desc("Definition ceiling used only when Override Maximum is enabled. This does NOT set an entity's attribute value. If the resolved minimum is greater than this ceiling, the override is rejected safely.")
        var max = ValidatedDouble(1024.0)

        @Translatable.Name("Stacking")
        var stacking = ConfigGroup("stacking", true)

        @Translatable.Name("Stacking Formula")
        @Translatable.Desc("Flat uses direct values. Diminished progressively reduces additional contributions.")
        var formula = ValidatedEnum(StackingFormula.Flat)

        @ConfigGroup.Pop
        @Translatable.Name("Diminishing Smoothness")
        @Translatable.Desc("Only used by the Diminished formula. Lower values increase diminishing behavior; valid range is greater than 0 through 1.")
        var smoothness = ValidatedDouble(1.0, 1.0, 0.000001, ValidatedNumber.WidgetType.TEXTBOX)

        @Translatable.Name("Display")
        var display = ConfigGroup("display", true)

        @ConfigGroup.Pop
        @Translatable.Name("Display Format")
        @Translatable.Desc("How Data Attributes formats values exposed through its formatted-value API.")
        var format = ValidatedEnum(AttributeFormat.Whole)

        fun toRuntime() = AttributeOverride(
            enabled = enabled.get(),
            min = if (overrideMin.get()) min.get().takeIf { it.isFinite() } ?: Double.NaN else Double.NaN,
            max = if (overrideMax.get()) max.get().takeIf { it.isFinite() } ?: Double.NaN else Double.NaN,
            smoothness = smoothness.get(),
            formula = formula.get(),
            format = format.get()
        )

        /** Migrates the v2 NaN-as-inherit representation without discarding custom bounds. */
        fun migrateLegacyBounds() {
            val legacyMin = min.get()
            val legacyMax = max.get()

            overrideMin.trySetQuiet(legacyMin.isFinite())
            overrideMax.trySetQuiet(legacyMax.isFinite())
            if (!legacyMin.isFinite()) min.trySetQuiet(0.0)
            if (!legacyMax.isFinite()) max.trySetQuiet(1024.0)
        }

        companion object {
            fun preview(value: AttributeOverride): AttributeOverrideConfig = AttributeOverrideConfig().also { config ->
                config.enabled.trySetQuiet(value.enabled)
                config.overrideMin.trySetQuiet(!value.min.isNaN())
                config.overrideMax.trySetQuiet(!value.max.isNaN())
                config.min.trySetQuiet(value.min.takeIf { it.isFinite() } ?: 0.0)
                config.max.trySetQuiet(value.max.takeIf { it.isFinite() } ?: 1024.0)
                config.smoothness.trySetQuiet(value.smoothness)
                config.formula.trySetQuiet(value.formula)
                config.format.trySetQuiet(value.format)
            }
        }

    }

    @Translatable.Name("Attribute Function")
    @Translatable.Prefix("The outer map key is the parent/source attribute; the nested key is the child/derived attribute. This object controls how the parent's current value contributes to that child.")
    @Translatable.Desc("One parent-to-child attribute relationship.")
    class AttributeFunctionConfig {
        @Translatable.Name("Enabled")
        @Translatable.Desc("Disable this relationship without deleting it from the config.")
        var enabled = ValidatedBoolean(true)

        @Translatable.Name("Operation")
        @Translatable.Desc("Add contributes parentValue × Value. Multiply scales the child by 1 + (parentValue × Value).")
        var behavior = ValidatedEnum(StackingBehavior.Add)

        @Translatable.Name("Coefficient")
        @Translatable.Desc("Multiplier applied to the parent/source attribute. For example, 0.25 means 25% of the parent value for Add.")
        var value = ValidatedDouble(0.0)

        fun toRuntime() = AttributeFunction(enabled.get(), behavior.get(), value.get())

        companion object {
            fun preview(value: AttributeFunction): AttributeFunctionConfig = AttributeFunctionConfig().also { config ->
                config.enabled.trySetQuiet(value.enabled)
                config.behavior.trySetQuiet(value.behavior)
                config.value.trySetQuiet(value.value)
            }
        }
    }

    @Translatable.Name("Entity / Group Base Values")
    @Translatable.Prefix("These entries directly set supplier base values. Explicit values on a concrete entity type override inherited values from Data Attributes implicit entity groups.")
    @Translatable.Desc("Actual attribute base values attached to one entity type or implicit entity group.")
    class EntityTypeConfig {
        @Translatable.Name("Attributes")
        @Translatable.Prefix("Keyed by registered or optional attribute IDs. Unknown IDs remain in the file but are ignored until their owning mod is present.")
        @Translatable.Desc("Base values added to this entity type or group.")
        var data = ValidatedIdentifierMap(
            emptyMap(),
            attributeIdentifier(ATTRIBUTE_DEFAULT),
            ValidatedAny(EntityAttributeConfig())
        )

        fun toRuntime() = EntityTypeData(
            data.entries.associateTo(LinkedHashMap()) { (id, entry) -> id to entry.toRuntime() }
        )
    }

    @Translatable.Name("Entity Attribute")
    @Translatable.Desc("Actual base value assigned to this entity/group. Bounds from Attribute Definition Overrides constrain the calculated result; they are not copied into this field.")
    class EntityAttributeConfig {
        @Translatable.Name("Entity Base Value")
        @Translatable.Desc("Sets the raw supplier base for this entity/group. The effective calculated value is still constrained by the Attribute definition. Non-finite values are ignored at runtime.")
        var value = ValidatedDouble(0.0)

        fun toRuntime() = EntityTypeEntry(value.get())

        companion object {
            fun preview(value: EntityTypeEntry): EntityAttributeConfig = EntityAttributeConfig().also { config ->
                config.value.trySetQuiet(value.value)
            }
        }
    }

    /**
     * Read-only-at-application map used solely for GUI previews. It is transient,
     * never written to disk or included in Fzzy synchronization, and discards edits
     * made in its browser popup. The real values are supplied by Data Attributes'
     * own server snapshot packet.
     */
    class PreviewIdentifierMap<V : Any>(
        defaultValue: Map<ResourceLocation, V>,
        keyHandler: ValidatedIdentifier,
        valueHandler: Entry<V, *>
    ) : ValidatedIdentifierMap<V>(defaultValue, keyHandler, valueHandler), EntryTransient {
        override fun setAndUpdate(input: Map<ResourceLocation, V>) {
            // Preview browser: intentionally discard GUI edits without producing an error.
        }

        fun replace(values: Map<ResourceLocation, V>) {
            trySetQuiet(LinkedHashMap(values))
        }
    }

    @Translatable.Name("Datapack Baseline Preview")
    @Translatable.Prefix("Informational only. These are the raw values supplied by loaded mods/datapacks before Server Configuration above is merged on top. Editing a preview is intentionally discarded.")
    @Translatable.Desc("Browse the datapack layer that the server config overrides.")
    class DatapackPreviewSection : ConfigSection(), EntryTransient {
        @Translatable.Name("Datapack Attribute Definition Overrides")
        @Translatable.Desc("Raw datapack definition-level overrides before server config priority is applied. These do not set entity base values.")
        var overrides = PreviewIdentifierMap(
            emptyMap(),
            attributeIdentifier(ATTRIBUTE_DEFAULT),
            ValidatedAny(AttributeOverrideConfig())
        )

        @Translatable.Name("Datapack Attribute Functions")
        @Translatable.Desc("Raw datapack parent/child functions before server config priority is applied.")
        var functions = PreviewIdentifierMap(
            emptyMap(),
            attributeIdentifier(ATTRIBUTE_DEFAULT),
            ValidatedIdentifierMap(
                emptyMap(),
                attributeIdentifier(ATTRIBUTE_CHILD_DEFAULT),
                ValidatedAny(AttributeFunctionConfig())
            )
        )

        @Translatable.Name("Datapack Entity Base Attributes")
        @Translatable.Desc("Raw datapack entity/group base values before server config priority is applied.")
        var entities = PreviewIdentifierMap(
            emptyMap(),
            entityIdentifier(ENTITY_DEFAULT),
            ValidatedAny(EntityTypeConfig())
        )

        fun refresh(data: AttributeConfigManager.Data) {
            overrides.replace(sortedIdentifiers(data.overrides).mapValuesTo(LinkedHashMap()) { (_, value) ->
                AttributeOverrideConfig.preview(value)
            })

            functions.replace(sortedIdentifiers(data.functions).mapValuesTo(LinkedHashMap()) { (_, children) ->
                sortedIdentifiers(children).mapValuesTo(LinkedHashMap()) { (_, value) ->
                    AttributeFunctionConfig.preview(value)
                }
            })

            entities.replace(sortedIdentifiers(data.entityTypes).mapValuesTo(LinkedHashMap()) { (_, dataValue) ->
                EntityTypeConfig().also { entry ->
                    entry.data.trySetQuiet(
                        sortedIdentifiers(dataValue.data).mapValuesTo(LinkedHashMap()) { (_, value) ->
                            EntityAttributeConfig.preview(value)
                        }
                    )
                }
            })
        }

        private fun <V> sortedIdentifiers(values: Map<ResourceLocation, V>): LinkedHashMap<ResourceLocation, V> =
            values.entries
                .sortedBy { it.key.toString() }
                .associateTo(LinkedHashMap()) { it.key to it.value }
    }

    @Translatable.Name("Server Configuration")
    @Translatable.Desc("Editable, highest-priority settings owned by this server.")
    var serverConfig = ServerSection()

    // Deliberately declared after serverConfig so Fzzy renders the informational layer below the authoritative layer.
    @Translatable.Name("Datapack Baseline Preview")
    @Translatable.Desc("Read-only-at-application view of the merged datapack layer underneath Server Configuration.")
    var datapackPreview = DatapackPreviewSection()

    fun toSnapshot(): ConfigSnapshot = ConfigSnapshot(
        overrides = serverConfig.overrides.entries.associateTo(LinkedHashMap()) { (id, value) -> id to value.toRuntime() },
        functions = serverConfig.functions.entries.associateTo(LinkedHashMap()) { (parent, children) ->
            parent to children.entries.associateTo(LinkedHashMap()) { (child, function) -> child to function.toRuntime() }
        },
        entityTypes = serverConfig.entities.entries.associateTo(LinkedHashMap()) { (id, value) -> id to value.toRuntime() }
    )

    fun refreshDatapackPreview(data: AttributeConfigManager.Data) {
        datapackPreview.refresh(data)
    }

    override fun update(deserializedVersion: Int) {
        if (deserializedVersion < 3) {
            serverConfig.overrides.values.forEach(AttributeOverrideConfig::migrateLegacyBounds)
        }
    }

    override fun onUpdateServer(context: ServerUpdateContext) {
        ConfigState.replace(toSnapshot())
        // Keep the local informational layer current after an in-game server edit.
        refreshDatapackPreview(DataAttributes.MANAGER.datapackDefaults)
        DataAttributes.reload(context.getServer())
    }

    override fun fileType(): FileType = FileType.JSON5

    companion object {
        private val ATTRIBUTE_DEFAULT = ResourceLocation.fromNamespaceAndPath("minecraft", "generic.max_health")
        private val ATTRIBUTE_CHILD_DEFAULT = ResourceLocation.fromNamespaceAndPath("minecraft", "generic.movement_speed")
        private val ENTITY_DEFAULT = ResourceLocation.fromNamespaceAndPath("minecraft", "pig")

        /**
         * Syntax-safe identifier input with live registry suggestions but no hard
         * membership requirement. This is deliberate: optional-mod IDs must not
         * make unrelated config edits impossible when that mod is absent.
         */
        private fun suggestedIdentifier(
            defaultValue: ResourceLocation,
            suggestions: Supplier<List<ResourceLocation>>
        ): ValidatedIdentifier {
            val sortedSuggestions = Supplier {
                suggestions.get().distinct().sortedBy { it.toString() }
            }
            val allowable = AllowableIdentifiers(Predicate { true }, sortedSuggestions, false)
            return ValidatedIdentifier(defaultValue, allowable, ValidatedIdentifier.DEFAULT_WEAK)
        }

        private fun attributeIdentifier(defaultValue: ResourceLocation): ValidatedIdentifier =
            suggestedIdentifier(defaultValue, Supplier { BuiltInRegistries.ATTRIBUTE.keySet().toList() })

        private fun entityIdentifier(defaultValue: ResourceLocation): ValidatedIdentifier =
            suggestedIdentifier(defaultValue, Supplier {
                buildList {
                    addAll(BuiltInRegistries.ENTITY_TYPE.keySet())
                    addAll(AttributeConfigManager.ENTITY_TYPE_INSTANCES.keys)
                }
            })
    }
}
