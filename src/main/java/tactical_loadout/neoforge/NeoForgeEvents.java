package tactical_loadout.neoforge;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.camo.Camouflage;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.device.DeviceEvents;
import tactical_loadout.grenade.SmokeClouds;
import tactical_loadout.gear.EodSuit;
import tactical_loadout.gear.Helpless;
import tactical_loadout.gear.Restraints;
import tactical_loadout.item.RepairKitItem;
import tactical_loadout.gear.ZipTiesItem;
import tactical_loadout.medical.Downed;
import tactical_loadout.medical.Medical;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/** Forwards NeoForge game events to the shared handlers. */
@EventBusSubscriber(modid = TacticalLoadout.MOD_ID)
public final class NeoForgeEvents {
    private NeoForgeEvents() {
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target instanceof ServerPlayer player && Downed.isDowned(player)) {
            event.setAmount(Downed.incomingDamage(player, event.getAmount()));
            return;
        }
        if (DeviceEvents.blocksDamage(target, event.getSource())) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(EodSuit.blastDamage(target, event.getSource(), event.getAmount()));
        event.setAmount(BallisticHits.onIncomingDamage(target, event.getSource(), event.getAmount()));
        if (BallisticHits.excludesOwnArmor(target, event.getSource())) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) ->
                    BallisticHits.armorAbsorptionWithoutOwn(target, container.getNewDamage(), container.getSource()));
        }
    }

    @SubscribeEvent
    static void onDamageDealt(LivingDamageEvent.Post event) {
        BallisticHits.onDamageDealt(event.getEntity(), event.getSource(), event.getBlockedDamage());
        Medical.onDamageDealt(event.getEntity(), event.getSource(), event.getNewDamage());
    }

    @SubscribeEvent
    static void onArmorHurt(ArmorHurtEvent event) {
        event.getArmorMap().forEach((slot, entry) -> {
            if (BallisticHits.skipsVanillaWear(event.getDamageSource(), entry.armorItemStack)) {
                entry.newDamage = 0.0F;
            }
        });
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        BallisticHits.onServerStopped();
        Restraints.onServerStopped();
        Downed.onServerStopped();
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        DeviceEvents.onPlayerTick(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer player) {
            Downed.tick(player);
        }
    }

    @SubscribeEvent
    static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getResult() != MobEffectEvent.Applicable.Result.DO_NOT_APPLY
                && DeviceEvents.blocksEffect(event.getEntity(), event.getEffectInstance())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() == null) {
            return;
        }
        event.modifyVisibility(Camouflage.visibility(event.getEntity()));
        event.modifyVisibility(SmokeClouds.visibility(event.getEntity(), event.getLookingEntity()));
    }

    @SubscribeEvent
    static void onAnvilUpdate(AnvilUpdateEvent event) {
        RepairKitItem.AnvilResult result = RepairKitItem.repair(event.getLeft(), event.getRight(), event.getName());
        if (result != null) {
            event.setOutput(result.output());
            event.setCost(result.cost());
            event.setMaterialCost(result.kitsUsed());
        }
    }

    @SubscribeEvent
    static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        ModAdvancements.onEquipmentChange(event.getEntity(), event.getSlot());
    }

    // Tied players can look around but not fight, use items or build.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onAttack(AttackEntityEvent event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onInteractEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
        if (Helpless.is(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && Helpless.is(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onBreak(BlockEvent.BreakEvent event) {
        if (Helpless.is(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && Helpless.is(player)) {
            event.setCanceled(true);
        }
    }

    // Shears cut a tied player loose, three quick clicks like tying.
    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer actor) || !(event.getTarget() instanceof ServerPlayer victim)) {
            return;
        }
        boolean handled = event.getItemStack().is(Items.SHEARS) ? Restraints.cutClick(actor, victim)
                : !(event.getItemStack().getItem() instanceof ZipTiesItem) && Downed.reviveClick(actor, victim);
        if (handled) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && Downed.blocksHealing(player)) {
            event.setCanceled(true);
        }
    }

    // A player who would die goes down instead when the downed state is on.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Downed.tryGoDown(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Restraints.onLogin(player);
            Downed.onLogin(player);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Restraints.onRespawn(player);
            Downed.onRespawn(player);
        }
    }
}
