package com.bibireden.data_attributes.ui.button

import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import net.minecraft.text.Text

object Buttons {
    fun add(onPress: (ButtonComponent) -> Unit): ButtonComponent = Components.button(Text.translatable("text.config.data_attributes.buttons.add"), onPress).renderer(ButtonRenderers.STANDARD)
}