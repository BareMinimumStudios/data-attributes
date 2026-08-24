package net.bms.data_attributes.networking

import net.bms.data_attributes.DataAttributes
import io.wispforest.owo.network.OwoNetChannel
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object NetworkingChannels {
    @JvmField
    val RELOAD = OwoNetChannel.create(DataAttributes.id("reload"))
}