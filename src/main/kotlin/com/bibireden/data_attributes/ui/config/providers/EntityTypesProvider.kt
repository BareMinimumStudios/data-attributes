package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.identifier.Identifiers
import com.bibireden.data_attributes.ui.button.Buttons
import com.bibireden.data_attributes.ui.components.config.entities.EntityTypesHeaderComponent
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.util.Identifier

class EntityTypesProvider(val option: Option<Map<Identifier, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = option.value().toMutableMap()

    init {
        child(Buttons.add {
            val identifier = Identifiers.unknown()
            val entry = EntityTypeData()
            backing[identifier] = entry
            child(EntityTypesHeaderComponent(identifier, entry.data.toMutableMap(), this).id(id.toString()))
        })

        for ((id, entry) in backing) {
            child(EntityTypesHeaderComponent(id, entry.data.toMutableMap(), this).id(id.toString()))
        }
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = backing
}