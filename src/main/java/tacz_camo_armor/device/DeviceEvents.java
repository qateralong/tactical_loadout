package tacz_camo_armor.device;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.registry.ModDataComponents;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class DeviceEvents {
    private static final int EFFECT_TICKS = 300;
    private static final int REFRESH_BELOW = 220;

    private DeviceEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        MountedDevice nvg = Devices.activeNvg(player);
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (nvg == null) {
            if (current != null && isOwnEffect(current)) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            return;
        }
        if (current == null || (isOwnEffect(current) && current.getDuration() < REFRESH_BELOW)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_TICKS, 0, true, false, false));
        }
        if (nvg.kind().usesCharge() && player.tickCount % 20 == 0) {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            MountedDevice drained = nvg.withCharge(nvg.clampedCharge() - 20);
            head.set(ModDataComponents.NVG.get(), drained);
            if (!drained.hasCharge()) {
                Devices.toggleNvg(player);
            }
        }
    }

    private static boolean isOwnEffect(MobEffectInstance effect) {
        return effect.isAmbient() && !effect.isVisible() && !effect.showIcon() && effect.getDuration() <= EFFECT_TICKS;
    }

    @SubscribeEvent
    static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        Holder<MobEffect> effect = event.getEffectInstance().getEffect();
        if (entity.level().isClientSide || !(effect.equals(MobEffects.POISON) || effect.equals(MobEffects.WITHER))
                || event.getResult() == MobEffectEvent.Applicable.Result.DO_NOT_APPLY || !entity.canBeAffected(event.getEffectInstance())) {
            return;
        }
        int cost = Math.max(1, event.getEffectInstance().getDuration() / 20);
        if (consumeFilter(entity, cost)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypes.DRAGON_BREATH) && consumeFilter(event.getEntity(), 1)) {
            event.setCanceled(true);
        }
    }

    private static boolean consumeFilter(LivingEntity entity, int cost) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        MountedDevice mask = head.get(ModDataComponents.GAS_MASK.get());
        if (mask == null || !mask.hasCharge()) {
            return false;
        }
        head.set(ModDataComponents.GAS_MASK.get(), mask.withCharge(mask.clampedCharge() - cost));
        return true;
    }
}
