package tactical_loadout.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.platform.ItemEntry;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NeoForgePlatform implements Platform {
    // The mod shipped as "tacz_camo_armor" before the rename; aliases keep items and entities in old worlds.
    private static final String LEGACY_ID = "tacz_camo_armor";
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> registerFor(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, TacticalLoadout.MOD_ID));
    }

    @Override
    public <R, T extends R> RegistryEntry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        DeferredHolder<R, T> holder = registerFor(registry).register(name, factory);
        aliasLegacy(registry, name);
        return new RegistryEntry<>(holder.getKey(), () -> holder);
    }

    @Override
    public <T extends Item> ItemEntry<T> registerItem(String name, Supplier<T> factory) {
        DeferredHolder<Item, T> holder = registerFor(Registries.ITEM).register(name, factory);
        aliasLegacy(Registries.ITEM, name);
        return new ItemEntry<>(holder.getKey(), () -> holder);
    }

    private <R> void aliasLegacy(ResourceKey<? extends Registry<R>> registry, String name) {
        registerFor(registry).addAlias(ResourceLocation.fromNamespaceAndPath(LEGACY_ID, name), TacticalLoadout.id(name));
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
