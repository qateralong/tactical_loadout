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
import tactical_loadout.gear.GearEvents;
import tactical_loadout.gear.Helpless;
import tactical_loadout.medical.Downed;
import tactical_loadout.medical.Medical;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;

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
        float amount = GearEvents.incomingDamage(event.getEntity(), event.getSource(), event.getAmount());
        event.setAmount(BallisticHits.onIncomingDamage(event.getEntity(), event.getSource(), amount));
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        BallisticHits.onDamageDealt(event.getEntity(), event.getSource(), 0.0F);
        Medical.onDamageDealt(event.getEntity(), event.getSource(), event.getAmount());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BallisticHits.onServerStopped();
        GearEvents.onServerStopped();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DeviceEvents.onPlayerTick(event.player);
            GearEvents.onPlayerTick(event.player);
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
    // A player who would die goes down instead when the downed state is on.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (GearEvents.preventDeath(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && Downed.blocksHealing(player)) {
            event.setCanceled(true);
        }
    }

    // Tied or downed players can look around but not fight, use items or build.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        } else if (event.getHand() == InteractionHand.MAIN_HAND
                && GearEvents.onEntityUse(event.getEntity(), event.getTarget(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && Helpless.is(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (Helpless.is(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && Helpless.is(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GearEvents.onLogin(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GearEvents.onRespawn(player);
        }
    }
}
