package tactical_loadout.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootPool;
//? if >=1.21 {
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;
//?} else {
/*import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.fml.config.ModConfig;
*///?}
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.device.DeviceEvents;
import tactical_loadout.device.Devices;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.network.StrugglePayload;
import tactical_loadout.network.ToggleNvgPayload;
import tactical_loadout.gear.GearEvents;
import tactical_loadout.gear.Helpless;
import tactical_loadout.gear.Restraints;
import tactical_loadout.medical.Medical;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
//? if >=1.21 {
import tactical_loadout.network.DownedPayload;
import tactical_loadout.network.RestraintPayload;
//?}

import java.util.Map;

public final class TacticalLoadoutFabric implements ModInitializer {
    // Vanilla chests that also roll one of the mod's supply tables, like the NeoForge loot modifiers do.
    private static final Map<ResourceLocation, String> SUPPLIES = Map.of(
            TacticalLoadout.location("minecraft", "chests/pillager_outpost"), "chests/outpost_supplies",
            TacticalLoadout.location("minecraft", "chests/simple_dungeon"), "chests/dungeon_supplies",
            TacticalLoadout.location("minecraft", "chests/ancient_city"), "chests/ancient_city_supplies");

    @Override
    public void onInitialize() {
        TacticalLoadout.init();
        TacticalLoadout.commonSetup();
        //? if >=1.21 {
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

        PayloadTypeRegistry.playS2C().register(FlashPayload.TYPE, FlashPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleNvgPayload.TYPE, ToggleNvgPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleNvgPayload.TYPE,
                (payload, context) -> context.server().execute(() -> Devices.toggleNvg(context.player())));
        PayloadTypeRegistry.playS2C().register(RestraintPayload.TYPE, RestraintPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(DownedPayload.TYPE, DownedPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(StrugglePayload.TYPE, StrugglePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StrugglePayload.TYPE,
                (payload, context) -> context.server().execute(() -> Restraints.struggle(context.player())));
        //?} else {
        /*ForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        ForgeConfigRegistry.INSTANCE.register(TacticalLoadout.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

        ServerPlayNetworking.registerGlobalReceiver(ToggleNvgPayload.ID,
                (server, player, handler, buf, sender) -> server.execute(() -> Devices.toggleNvg(player)));
        ServerPlayNetworking.registerGlobalReceiver(StrugglePayload.ID,
                (server, player, handler, buf, sender) -> server.execute(() -> Restraints.struggle(player)));
        *///?}

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !DeviceEvents.blocksDamage(entity, source));
        //? if >=1.21 {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) -> {
            BallisticHits.onDamageDealt(entity, source, blocked ? 1.0F : 0.0F);
            Medical.onDamageDealt(entity, source, damage);
        });
        //?}
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> !GearEvents.preventDeath(entity, source));
        // Tied or downed players can look around but not fight, use items or build.
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> Helpless.is(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> Helpless.is(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> Helpless.is(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) -> Helpless.is(player)
                ? InteractionResultHolder.fail(player.getItemInHand(hand)) : InteractionResultHolder.pass(player.getItemInHand(hand)));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> !Helpless.is(player));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (Helpless.is(player)) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && GearEvents.onEntityUse(player, entity, player.getItemInHand(hand))) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GearEvents.onLogin(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GearEvents.onRespawn(newPlayer));
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) ->
                ModAdvancements.onEquipmentChange(entity, slot));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            BallisticHits.onServerStopped();
            GearEvents.onServerStopped();
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DeviceEvents.onPlayerTick(player);
                GearEvents.onPlayerTick(player);
            }
        });

        //? if >=1.21 {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            String supplies = SUPPLIES.get(key.location());
            if (supplies != null && source.isBuiltin()) {
                ResourceKey<LootTable> extra = ResourceKey.create(Registries.LOOT_TABLE, TacticalLoadout.id(supplies));
                table.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(extra)));
            }
        });
        //?} else {
        /*LootTableEvents.MODIFY.register((resources, loot, id, table, source) -> {
            String supplies = SUPPLIES.get(id);
            if (supplies != null && source.isBuiltin()) {
                table.withPool(LootPool.lootPool().add(LootTableReference.lootTableReference(TacticalLoadout.id(supplies))));
            }
        });
        *///?}
    }
}
