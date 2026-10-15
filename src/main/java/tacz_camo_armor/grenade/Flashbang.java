package tacz_camo_armor.grenade;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.advancement.ModAdvancements;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.device.Devices;
import tacz_camo_armor.device.MountedDevice;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.network.FlashPayload;

public final class Flashbang {
    private static final double NVG_AMPLIFICATION = 1.5;
    private static final String BLINDED_UNTIL = TaczCamoArmor.MOD_ID + ":flashed_until";

    private Flashbang() {
    }

    static void detonate(ServerLevel level, Entity grenade) {
        Vec3 pos = grenade.position().add(0, 0.2, 0);
        double radius = ServerConfig.flashRadius();
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.5F, 1.8F);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 3.0F, 1.2F);
        level.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        Entity thrower = grenade instanceof Projectile projectile ? projectile.getOwner() : null;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius))) {
            double strength = entity.isSpectator() ? 0 : strength(level, entity, pos, radius);
            if (strength < 0.05) {
                continue;
            }
            // Night vision tubes amplify the flash; a thermal imager does not.
            MountedDevice nvg = Devices.activeNvg(entity);
            double amplification = nvg != null && nvg.kind() != DeviceKind.THERMAL ? NVG_AMPLIFICATION : 1.0;
            strength = Math.min(1.0, strength * amplification);
            int ticks = (int) Math.round(strength * ServerConfig.flashSeconds() * 20 * amplification);
            if (entity instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new FlashPayload((float) strength, ticks));
            } else if (entity instanceof Mob mob) {
                mob.getPersistentData().putLong(BLINDED_UNTIL, level.getGameTime() + ticks);
                mob.setTarget(null);
                mob.getNavigation().stop();
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 2));
                ModAdvancements.award(thrower, ModAdvancements.FLASH_MOB);
            }
        }
    }

    static double strength(ServerLevel level, LivingEntity entity, Vec3 pos, double radius) {
        Vec3 eye = entity.getEyePosition();
        double distance = eye.distanceTo(pos);
        if (distance > radius) {
            return 0;
        }
        if (level.clip(new ClipContext(eye, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() != HitResult.Type.MISS) {
            return 0;
        }
        double facing = Math.max(0, entity.getViewVector(1.0F).dot(pos.subtract(eye).normalize()));
        double strength = (1 - (distance / radius) * (distance / radius)) * (0.3 + 0.7 * facing);
        return distance < 3 ? Math.max(strength, 0.8) : strength;
    }

    public static boolean isBlinded(Entity entity) {
        if (!(entity instanceof Mob mob) || !mob.getPersistentData().contains(BLINDED_UNTIL)) {
            return false;
        }
        if (mob.getPersistentData().getLong(BLINDED_UNTIL) > mob.level().getGameTime()) {
            return true;
        }
        mob.getPersistentData().remove(BLINDED_UNTIL);
        return false;
    }
}
