package com.bibireden.data_attributes.ui.components.config.entities

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.config.ConfigDockComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.EntityTypesProvider
import com.bibireden.data_attributes.ui.options.UIAttributeComponentOptions
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntityTypesHeaderComponent(override var identifier: Identifier, private val entityTypes: MutableMap<Identifier, EntityTypeEntry>, private val provider: EntityTypesProvider, private val options: UIAttributeComponentOptions)
    : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.entityTypeHeaders[identifier.toString()] ?: true), AttributeConfigComponent<EntityType<*>> {

    override val registry: Registry<EntityType<*>> = Registries.ENTITY_TYPE

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, registry, { it.translationKey }, options.isReadonly))
    }

    private fun createEntry(entryId: Identifier, entry: EntityTypeEntry): EntityTypesComponent = childById(EntityTypesComponent::class.java, "entry#$entryId") ?: EntityTypesComponent(entryId, identifier, entry, provider, options).also { it.id("entry#$entryId")}.also(::child)

    override fun update() {
        titleLayout().tooltip(null)
        when {
            !isRegistered -> {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            }
            options.isReadonly -> {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.readonly"))
            }
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    fun addEntityTypes(entityTypes: MutableMap<Identifier, EntityTypeEntry>) {
        for ((id, types) in entityTypes) createEntry(id, types)
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.entityTypeHeaders[identifier.toString()] = it }

        if (!options.isReadonly) {
            child(
                ConfigDockComponent(ConfigDockComponent.ConfigDefaultProperties({ _, _ ->
                    provider.backing.remove(identifier)
                    remove()
                })
                { _, _ ->
                    if (childById(FlowLayout::class.java, "edit-field") == null) {
                        val field = FieldComponents.identifier(
                            { newId, _ ->
                                if (provider.backing.containsKey(newId) || !registry.containsId(newId)) return@identifier

                                provider.backing.remove(identifier)?.let { provider.backing[newId] = it }

                                identifier = newId

                                children().filterIsInstance<EntityTypesComponent>().forEach { it.updateParent(identifier) }

                                update()
                            },
                            autocomplete = registry.ids
                        )

                        field.textBox.predicate = { provider.backing[identifier]?.data?.get(it) == null && registry.containsId(it) }

                        child(0, field)
                    }
                }).child(
                    Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                        val map = provider.backing[identifier]?.data?.toMutableMap() ?: mutableMapOf()
                        val childId = Identifier("unknown")
                        val entry = EntityTypeEntry()
                        map[childId] = entry
                        provider.backing[identifier] = EntityTypeData(map)

                        child(1, EntityTypesComponent(childId, identifier, entry, provider, options))

                        update()
                    }
                        .renderer(ButtonRenderers.STANDARD)
                        .horizontalSizing(Sizing.content())
                        .verticalSizing(Sizing.fixed(20))
                )
            )
        }

        addEntityTypes(this.entityTypes)

        update()
    }
}