package tactical_loadout.legacyforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.camo.Camouflage;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.device.DeviceEvents;
import tactical_loadout.grenade.SmokeClouds;
import tactical_loadout.item.RepairKitItem;

import java.util.Map;

/** Forwards Forge game events to the shared handlers. */
public final class LegacyForgeEvents {
    // Vanilla chests that also roll one of the mod's supply tables, like the NeoForge loot modifiers do.
    private static final Map<ResourceLocation, String> SUPPLIES = Map.of(
            TacticalLoadout.location("minecraft", "chests/pillager_outpost"), "chests/outpost_supplies",
            TacticalLoadout.location("minecraft", "chests/simple_dungeon"), "chests/dungeon_supplies",
            TacticalLoadout.location("minecraft", "chests/ancient_city"), "chests/ancient_city_supplies");

    private LegacyForgeEvents() {
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (DeviceEvents.blocksDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        event.setAmount(BallisticHits.onIncomingDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        BallisticHits.onDamageDealt(event.getEntity(), event.getSource(), 0.0F);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BallisticHits.onServerStopped();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DeviceEvents.onPlayerTick(event.player);
        }
    }

    // Forge fires this event from canBeAffected, which the gas mask check calls itself, so nested calls are skipped.
    private static boolean checkingEffect;

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (checkingEffect || event.getResult() == Event.Result.DENY) {
            return;
        }
        checkingEffect = true;
        try {
            if (DeviceEvents.blocksEffect(event.getEntity(), event.getEffectInstance())) {
                event.setResult(Event.Result.DENY);
            }
        } finally {
            checkingEffect = false;
        }
    }

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() == null) {
            return;
        }
        event.modifyVisibility(Camouflage.visibility(event.getEntity()));
        event.modifyVisibility(SmokeClouds.visibility(event.getEntity(), event.getLookingEntity()));
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        RepairKitItem.AnvilResult result = RepairKitItem.repair(event.getLeft(), event.getRight(), event.getName());
        if (result != null) {
            event.setOutput(result.output());
            event.setCost(result.cost());
            event.setMaterialCost(result.kitsUsed());
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        ModAdvancements.onEquipmentChange(event.getEntity(), event.getSlot());
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        String supplies = SUPPLIES.get(event.getName());
        if (supplies != null) {
            event.getTable().addPool(LootPool.lootPool().add(LootTableReference.lootTableReference(TacticalLoadout.id(supplies))).build());
        }
    }
}
