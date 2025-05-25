package com.bibireden.data_attributes.ui.components.labels

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Style
import net.minecraft.text.Text

object LabelComponents {
    fun header(text: Text): LabelComponent = Components.label(text.copy().setStyle(Style.EMPTY.withBold(true))).apply {
        horizontalTextAlignment(HorizontalAlignment.CENTER)
        horizontalSizing(Sizing.fill(100))
    }
}