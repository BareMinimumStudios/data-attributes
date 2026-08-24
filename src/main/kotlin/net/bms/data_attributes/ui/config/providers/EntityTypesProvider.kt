//package net.bms.data_attributes.ui.config.providers
//
//import net.bms.data_attributes.api.DataAttributesAPI
//import net.bms.data_attributes.config.entities.EntityTypeData
//import io.wispforest.owo.config.Option
//import io.wispforest.owo.config.ui.component.OptionValueProvider
//import io.wispforest.owo.ui.component.Components
//import io.wispforest.owo.ui.container.FlowLayout
//import io.wispforest.owo.ui.core.Sizing
//import net.bms.data_attributes.ui.components.config.entities.EntityTypesHeaderComponent
//import net.bms.data_attributes.ui.renderers.ButtonRenderers
//import net.minecraft.network.chat.Component
//import net.minecraft.resources.ResourceLocation
//import kotlin.collections.iterator
//import kotlin.toString
//
//class EntityTypesProvider(val option: Option<Map<ResourceLocation, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
//    val backing = option.value().toMutableMap()
//
//    init {
//        child(
//            Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
//                val identifier = ResourceLocation.tryBuild("unknown", "resource")!!
//                val entry = EntityTypeData()
//                backing[identifier] = entry
//                child(1, EntityTypesHeaderComponent(identifier, entry.data.toMutableMap(), this))
//            }
//                .renderer(ButtonRenderers.STANDARD)
//                .horizontalSizing(Sizing.content())
//                .verticalSizing(Sizing.fixed(20))
//        )
//
//        for ((id, entry) in backing) {
//            child(EntityTypesHeaderComponent(id, entry.data.toMutableMap(), this).id(id.toString()))
//        }
//        for ((id, entry) in DataAttributesAPI.serverManager.defaults.types.entries) {
//            val component = childById(EntityTypesHeaderComponent::class.java, id.toString())
//            if (component != null) {
//                component.addEntityTypes(entry)
//            }
//            else {
//                child(EntityTypesHeaderComponent(id, entry, this).id(id.toString()))
//            }
//        }
//    }
//
//    override fun isValid() = !this.option.detached()
//    override fun parsedValue() = backing
//}