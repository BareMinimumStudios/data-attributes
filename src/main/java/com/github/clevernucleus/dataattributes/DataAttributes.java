package com.github.clevernucleus.dataattributes;

import org.jetbrains.annotations.Nullable;

import com.github.clevernucleus.dataattributes.api.DataAttributesAPI;
import com.github.clevernucleus.dataattributes.api.event.EntityAttributeModifiedEvents;
import com.github.clevernucleus.dataattributes.impl.AttributeManager;
import com.github.clevernucleus.dataattributes.mutable.MutableAttributeContainer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking.LoginSynchronizer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DataAttributes implements ModInitializer {
    // Identifiers for networking
    public static final Identifier HANDSHAKE = new Identifier(DataAttributesAPI.MODID, "handshake");
    public static final Identifier RELOAD = new Identifier(DataAttributesAPI.MODID, "reload");

    // Attribute manager instance
    public static final AttributeManager MANAGER = new AttributeManager();

    // Sends attribute data to the client during login query start
    private static void loginQueryStart(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender sender,
            LoginSynchronizer synchronizer) {
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound tag = new NbtCompound();
        DataAttributes.MANAGER.toNbt(tag);
        buf.writeNbt(tag);
        sender.sendPacket(HANDSHAKE, buf);
    }

    // Placeholder for handling login query response
    private static void loginQueryResponse(MinecraftServer server, ServerLoginNetworkHandler handler,
            boolean understood, PacketByteBuf buf, LoginSynchronizer synchronizer, PacketSender responseSender) {
        // Implementation details can be added as needed
    }

    // Refreshes attributes for living entities
    public static void refreshAttributes(final Entity entity) {
        if (!(entity instanceof LivingEntity))
            return;
        ((MutableAttributeContainer) ((LivingEntity) entity).getAttributes()).refresh();
    }

    // Handles health modification events for living entities
    private static void healthModified(final EntityAttribute attribute, final @Nullable LivingEntity livingEntity,
            final EntityAttributeModifier modifier, final double prevValue, final boolean isWasAdded) {
        if (livingEntity == null)
            return;
        World world = livingEntity.getWorld();
        if (world.isClient)
            return;
        if (attribute != EntityAttributes.GENERIC_MAX_HEALTH)
            return;

        float c0 = livingEntity.getHealth();
        float c1 = c0 * livingEntity.getMaxHealth() / (float) prevValue;

        livingEntity.setHealth(c1);
    }

    @Override
    public void onInitialize() {
        // Registers the AttributeManager as a reload listener for server data
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MANAGER);

        // Registers event handlers
        ServerLoginConnectionEvents.QUERY_START.register(DataAttributes::loginQueryStart);
        ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE, DataAttributes::loginQueryResponse);
        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD
                .register((oldEntity, newEntity, from, to) -> refreshAttributes(newEntity));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD
                .register((player, from, to) -> refreshAttributes(player));
        EntityAttributeModifiedEvents.MODIFIED.register(DataAttributes::healthModified);
    }
}
