package tactical_loadout.grenade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tactical_loadout.config.ServerConfig;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class SmokeClouds {
    private static final Set<GrenadeEntity> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());

    private SmokeClouds() {
    }

    static void add(GrenadeEntity grenade) {
        ACTIVE.add(grenade);
    }

    static void remove(GrenadeEntity grenade) {
        ACTIVE.remove(grenade);
    }

    @Nullable
    static GrenadeEntity coveringSmoke(Level level, Vec3 point) {
        double radius = ServerConfig.smokeRadius();
        for (GrenadeEntity smoke : ACTIVE) {
            if (!smoke.isRemoved() && smoke.level() == level && smoke.isSmoking()
                    && smoke.position().add(0, 1.0, 0).distanceTo(point) < radius) {
                return smoke;
            }
        }
        return null;
    }

    /** Multiplier for how well the looker sees the target through smoke, or 0 while the looker is blinded. */
    public static double visibility(LivingEntity target, Entity looker) {
        if (target.level().isClientSide) {
            return 1.0;
        }
        if (Flashbang.isBlinded(looker)) {
            return 0.0;
        }
        double radius = ServerConfig.smokeRadius();
        Vec3 eye = looker.getEyePosition();
        Vec3 aim = target.getBoundingBox().getCenter();
        for (GrenadeEntity smoke : ACTIVE) {
            if (smoke.isRemoved() || smoke.level() != target.level() || !smoke.isSmoking()) {
                continue;
            }
            Vec3 center = smoke.position().add(0, 1.0, 0);
            if (center.distanceTo(aim) < radius) {
                return 0.1;
            }
            if (segmentDistance(eye, aim, center) < radius) {
                return 0.15;
            }
        }
        return 1.0;
    }

    private static double segmentDistance(Vec3 from, Vec3 to, Vec3 point) {
        Vec3 segment = to.subtract(from);
        double length = segment.lengthSqr();
        double t = length == 0 ? 0 : Math.max(0, Math.min(1, point.subtract(from).dot(segment) / length));
        return from.add(segment.scale(t)).distanceTo(point);
    }
}
