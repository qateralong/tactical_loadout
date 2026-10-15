package tactical_loadout.platform;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/** Everything the shared code needs from the mod loader. */
public interface Platform {
    //? if neoforge {
    Platform INSTANCE = new tactical_loadout.neoforge.NeoForgePlatform();
    //?} elif fabric {
    /*Platform INSTANCE = new tactical_loadout.fabric.FabricPlatform();
    *///?}

    <R, T extends R> RegistryEntry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    <T extends Item> ItemEntry<T> registerItem(String name, Supplier<T> factory);

    boolean isModLoaded(String modId);

    CreativeModeTab.Builder creativeTab();

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);
}
