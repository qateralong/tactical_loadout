package tacz_camo_armor.device;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.registry.ModDataComponents;

public final class DeviceEvents {
    private static final int EFFECT_TICKS = 300;
    private static final int REFRESH_BELOW = 220;
    private static final int LOW_FRACTION = 10;

    private DeviceEvents() {
    }

    public static void onPlayerTick(Player player) {
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
            } else {
                warnIfLow(player, nvg, drained, "nvg_low");
            }
        }
    }

    private static boolean isOwnEffect(MobEffectInstance effect) {
        return effect.isAmbient() && !effect.isVisible() && !effect.showIcon() && effect.getDuration() <= EFFECT_TICKS;
    }

    /** Whether a gas mask stops the effect; spends filter only on effects that would otherwise apply. */
    public static boolean blocksEffect(LivingEntity entity, MobEffectInstance instance) {
        Holder<MobEffect> effect = instance.getEffect();
        if (entity.level().isClientSide || !(effect.equals(MobEffects.POISON) || effect.equals(MobEffects.WITHER))
                || !entity.canBeAffected(instance)) {
            return false;
        }
        return consumeFilter(entity, Math.max(1, instance.getDuration() / 20));
    }

    public static boolean blocksDamage(LivingEntity entity, DamageSource source) {
        return source.is(DamageTypes.DRAGON_BREATH) && consumeFilter(entity, 1);
    }

    private static boolean consumeFilter(LivingEntity entity, int cost) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        MountedDevice mask = head.get(ModDataComponents.GAS_MASK.get());
        if (mask == null || !mask.hasCharge()) {
            return false;
        }
        MountedDevice used = mask.withCharge(mask.clampedCharge() - cost);
        head.set(ModDataComponents.GAS_MASK.get(), used);
        if (entity instanceof Player player) {
            warnIfLow(player, mask, used, used.hasCharge() ? "filter_low" : "filter_empty");
        }
        return true;
    }

    private static void warnIfLow(Player player, MountedDevice before, MountedDevice after, String message) {
        int threshold = after.kind().maxCharge() / LOW_FRACTION;
        if (before.clampedCharge() > threshold && after.clampedCharge() <= threshold || !after.hasCharge()) {
            player.displayClientMessage(Component.translatable("message." + TaczCamoArmor.MOD_ID + "." + message)
                    .withStyle(ChatFormatting.RED), true);
        }
    }
}
