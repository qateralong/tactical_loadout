package tactical_loadout.legacyforge;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.network.CustomPacketPayload;
import tactical_loadout.platform.ItemEntry;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Forge before 1.20.5: deferred registers, a simple channel for packets. */
public final class LegacyForgePlatform implements Platform {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> registerFor(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, TacticalLoadout.MOD_ID));
    }

    @Override
    public <R, T extends R> RegistryEntry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        RegistryObject<T> object = registerFor(registry).register(name, factory);
        return new RegistryEntry<R, T>(ResourceKey.create(registry, TacticalLoadout.id(name)), () -> holder(object));
    }

    @Override
    public <T extends Item> ItemEntry<T> registerItem(String name, Supplier<T> factory) {
        RegistryObject<T> object = registerFor(Registries.ITEM).register(name, factory);
        return new ItemEntry<>(ResourceKey.create(Registries.ITEM, TacticalLoadout.id(name)), () -> holder(object));
    }

    @SuppressWarnings("unchecked")
    private static <R> Holder<R> holder(RegistryObject<?> object) {
        return (Holder<R>) object.getHolder().orElseThrow();
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
        LegacyForgeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        LegacyForgeNetwork.CHANNEL.sendToServer(payload);
    }
}
