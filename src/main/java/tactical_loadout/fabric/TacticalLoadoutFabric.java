package tactical_loadout.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.device.DeviceEvents;
import tactical_loadout.device.Devices;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.ToggleNvgPayload;

import java.util.Map;

public final class TacticalLoadoutFabric implements ModInitializer {
    // Vanilla chests that also roll one of the mod's supply tables, like the NeoForge loot modifiers do.
    private static final Map<ResourceLocation, String> SUPPLIES = Map.of(
            ResourceLocation.withDefaultNamespace("chests/pillager_outpost"), "chests/outpost_supplies",
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"), "chests/dungeon_supplies",
            ResourceLocation.withDefaultNamespace("chests/ancient_city"), "chests/ancient_city_supplies");

    @Override
    public void onInitialize() {
        TacticalLoadout.init();
        TacticalLoadout.commonSetup();
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

        PayloadTypeRegistry.playS2C().register(FlashPayload.TYPE, FlashPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleNvgPayload.TYPE, ToggleNvgPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleNvgPayload.TYPE,
                (payload, context) -> context.server().execute(() -> Devices.toggleNvg(context.player())));

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !DeviceEvents.blocksDamage(entity, source));
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) ->
                BallisticHits.onDamageDealt(entity, source, blocked ? 1.0F : 0.0F));
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) ->
                ModAdvancements.onEquipmentChange(entity, slot));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> BallisticHits.onServerStopped());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DeviceEvents.onPlayerTick(player);
            }
        });

        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            String supplies = SUPPLIES.get(key.location());
            if (supplies != null && source.isBuiltin()) {
                ResourceKey<LootTable> extra = ResourceKey.create(Registries.LOOT_TABLE, TacticalLoadout.id(supplies));
                table.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(extra)));
            }
        });
    }
}
