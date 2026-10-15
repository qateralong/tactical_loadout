package tactical_loadout.medical;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.registry.ModEffects;

/** Bullet and shrapnel wounds that may start bleeding. */
public final class Medical {
    public static final ResourceKey<DamageType> BLEEDING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, TacticalLoadout.id("bleeding"));

    private Medical() {
    }

    /** The leather rustle of a bandage or a helping hand. */
    public static SoundEvent patchSound() {
        //? if >=1.21 {
        return SoundEvents.ARMOR_EQUIP_LEATHER.value();
        //?} else {
        /*return SoundEvents.ARMOR_EQUIP_LEATHER;
        *///?}
    }

    public static DamageSource bleedingSource(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLEEDING_DAMAGE));
    }

    public static boolean isBleeding(LivingEntity entity) {
        return entity.hasEffect(ModEffects.bleeding());
    }

    public static void stopBleeding(LivingEntity entity) {
        entity.removeEffect(ModEffects.bleeding());
    }

    /** Called once damage was actually dealt. */
    public static void onDamageDealt(LivingEntity entity, DamageSource source, float damage) {
        if (damage <= 0.0F || entity.level().isClientSide || !entity.isAlive()) {
            return;
        }
        if (!BallisticHits.isBallistic(source) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        if (entity.getRandom().nextDouble() < ServerConfig.bleedChance()) {
            entity.addEffect(new MobEffectInstance(ModEffects.bleeding(), ServerConfig.bleedSeconds() * 20, 0, false, true, true));
        }
    }
}
