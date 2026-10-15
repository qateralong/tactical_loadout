package tacz_camo_armor.neoforge;

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
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.advancement.ModAdvancements;
import tacz_camo_armor.camo.Camouflage;
import tacz_camo_armor.combat.BallisticHits;
import tacz_camo_armor.device.DeviceEvents;
import tacz_camo_armor.grenade.SmokeClouds;
import tacz_camo_armor.item.RepairKitItem;

/** Forwards NeoForge game events to the shared handlers. */
@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class NeoForgeEvents {
    private NeoForgeEvents() {
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (DeviceEvents.blocksDamage(target, event.getSource())) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(BallisticHits.onIncomingDamage(target, event.getSource(), event.getAmount()));
        if (BallisticHits.excludesOwnArmor(target, event.getSource())) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) ->
                    BallisticHits.armorAbsorptionWithoutOwn(target, container.getNewDamage(), container.getSource()));
        }
    }

    @SubscribeEvent
    static void onDamageDealt(LivingDamageEvent.Post event) {
        BallisticHits.onDamageDealt(event.getEntity(), event.getSource(), event.getBlockedDamage());
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
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        DeviceEvents.onPlayerTick(event.getEntity());
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
}
