package tactical_loadout.grenade;

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
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.device.Devices;
import tactical_loadout.device.MountedDevice;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.network.FlashPayload;
import tactical_loadout.platform.Platform;

import java.util.Map;
import java.util.WeakHashMap;

public final class Flashbang {
    private static final double NVG_AMPLIFICATION = 1.5;
    // Game time until which a mob is blind. Not saved: a flash lasts seconds, so a reload simply ends it.
    private static final Map<Entity, Long> BLINDED_UNTIL = new WeakHashMap<>();

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
            double amplification = nvg != null && !nvg.kind().isThermal() ? NVG_AMPLIFICATION : 1.0;
            strength = Math.min(1.0, strength * amplification);
            int ticks = (int) Math.round(strength * ServerConfig.flashSeconds() * 20 * amplification);
            if (entity instanceof ServerPlayer player) {
                Platform.INSTANCE.sendToPlayer(player, new FlashPayload((float) strength, ticks));
            } else if (entity instanceof Mob mob) {
                BLINDED_UNTIL.put(mob, level.getGameTime() + ticks);
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
        Long until = BLINDED_UNTIL.get(entity);
        if (until == null) {
            return false;
        }
        if (until > entity.level().getGameTime()) {
            return true;
        }
        BLINDED_UNTIL.remove(entity);
        return false;
    }
}
