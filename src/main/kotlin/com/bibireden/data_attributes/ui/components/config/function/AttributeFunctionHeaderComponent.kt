package com.bibireden.data_attributes.ui.components.config.function

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.config.ConfigDockComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.AttributeFunctionProvider
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeFunctionHeaderComponent(override var identifier: Identifier, private val functions: MutableMap<Identifier, AttributeFunction>, private val provider: AttributeFunctionProvider) : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] ?: true), AttributeConfigComponent<EntityAttribute> {
    override val registry: Registry<EntityAttribute>
        get() = Registries.ATTRIBUTE

    private fun createEntry(childId: Identifier, function: AttributeFunction): AttributeFunctionComponent = childById(AttributeFunctionComponent::class.java, "child#$childId") ?: AttributeFunctionComponent(childId, function, identifier, provider).also { it.id("child#$childId") }.also(::child)

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, Registries.ATTRIBUTE, { it.translationKey }, false))
    }

    override fun update() {
        titleLayout().tooltip(null)
        when {
            !isRegistered -> {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            }
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    fun addFunctions(functions: MutableMap<Identifier, AttributeFunction>) {
        for ((id, function) in functions) createEntry(id, function)
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] = it }

        update()

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

                            for (afc in children().filterIsInstance<AttributeFunctionComponent>()) { afc.updateParent(identifier) }

                            update()
                        },
                        autocomplete = registry.ids
                    )

                    field.textBox.predicate = { provider.backing[identifier]?.get(it) == null && registry.containsId(it) }

                    child(0, field)
                }
            }).child(
                Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                    val entries = provider.backing.computeIfAbsent(identifier) { mutableMapOf() }

                    val childId = Identifier("unknown")
                    val function = AttributeFunction()

                    entries[childId] = function

                    provider.backing[identifier] = entries

                    child(2, AttributeFunctionComponent(childId, function, identifier, provider))

                    update()
                }
                    .renderer(ButtonRenderers.STANDARD)
                    .horizontalSizing(Sizing.content())
                    .verticalSizing(Sizing.fixed(20))
            )
        )

        addFunctions(this.functions)
    }
}