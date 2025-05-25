package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.identifier.Identifiers
import com.bibireden.data_attributes.ui.button.Buttons
import com.bibireden.data_attributes.ui.components.config.AttributeOverrideComponent
import com.bibireden.data_attributes.ui.components.labels.LabelComponents
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeOverrideProvider(val option: Option<Map<Identifier, AttributeOverride>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing: MutableMap<Identifier, AttributeOverride> = option.value().toMutableMap()

    /** Construct single override entry that discerns defaults from config-based ones. */
    private fun createOverrideEntry(id: Identifier, override: AttributeOverride) {
        child(AttributeOverrideComponent(id, override, backing, AttributeOverrideComponent.Options(isReadonly = false)))
    }

    init {
        child(
            Buttons.add {
                val identifier = Identifiers.unknown()
                val override = AttributeOverride()
                backing[identifier] = override
                child(1, AttributeOverrideComponent(identifier, override, backing, AttributeOverrideComponent.Options(isReadonly = false)))
            }
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        backing.forEach(::createOverrideEntry)

        DataAttributesAPI.serverManager.defaults.forEach { (rid, cache) ->
            child(LabelComponents.header(Text.literal("<< ${rid.namespace} >>")))

            cache.overrides.entries.forEach { (id, override) ->
                child(AttributeOverrideComponent(id, override, backing, AttributeOverrideComponent.Options(isReadonly = true)))
            }
        }
    }

    override fun isValid() = !this.option.detached()

    override fun parsedValue() = backing
}