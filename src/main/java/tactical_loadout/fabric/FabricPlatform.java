package tactical_loadout.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.platform.ItemEntry;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

import java.util.function.Supplier;
//? if >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import tactical_loadout.network.CustomPacketPayload;
*///?}

/** Fabric registers immediately, so every entry is bound as soon as it is declared. */
public final class FabricPlatform implements Platform {
    @SuppressWarnings("unchecked")
    private static <R> Holder.Reference<R> bind(ResourceKey<? extends Registry<R>> registry, String name, R value) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.get(registry.location());
        return Registry.registerForHolder(target, TacticalLoadout.id(name), value);
    }

    @Override
    public <R, T extends R> RegistryEntry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Holder.Reference<R> holder = bind(registry, name, factory.get());
        return new RegistryEntry<>(holder.key(), () -> holder);
    }

    @Override
    public <T extends Item> ItemEntry<T> registerItem(String name, Supplier<T> factory) {
        Holder.Reference<Item> holder = bind(BuiltInRegistries.ITEM.key(), name, factory.get());
        return new ItemEntry<>(holder.key(), () -> holder);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return FabricItemGroup.builder();
    }

    //? if >=1.20.5 {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
    //?} else {
    /*@Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload.id(), encode(payload));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload.id(), encode(payload));
    }

    private static FriendlyByteBuf encode(CustomPacketPayload payload) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        return buf;
    }
    *///?}
}
