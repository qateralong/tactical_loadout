package tacz_camo_armor.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.platform.ItemEntry;
import tacz_camo_armor.platform.Platform;
import tacz_camo_armor.platform.RegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NeoForgePlatform implements Platform {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> registerFor(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, TaczCamoArmor.MOD_ID));
    }

    @Override
    public <R, T extends R> RegistryEntry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        DeferredHolder<R, T> holder = registerFor(registry).register(name, factory);
        return new RegistryEntry<>(holder.getKey(), () -> holder);
    }

    @Override
    public <T extends Item> ItemEntry<T> registerItem(String name, Supplier<T> factory) {
        DeferredHolder<Item, T> holder = registerFor(Registries.ITEM).register(name, factory);
        return new ItemEntry<>(holder.getKey(), () -> holder);
    }

    void registerAll(IEventBus modBus) {
        registers.values().forEach(register -> register.register(modBus));
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return CreativeModeTab.builder();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
