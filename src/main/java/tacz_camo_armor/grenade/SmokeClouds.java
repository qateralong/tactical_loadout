package tacz_camo_armor.grenade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
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

    @SubscribeEvent
    static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        Entity looker = event.getLookingEntity();
        LivingEntity target = event.getEntity();
        if (looker == null || target.level().isClientSide) {
            return;
        }
        if (Flashbang.isBlinded(looker)) {
            event.modifyVisibility(0.0);
            return;
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
                event.modifyVisibility(0.1);
                return;
            }
            if (segmentDistance(eye, aim, center) < radius) {
                event.modifyVisibility(0.15);
                return;
            }
        }
    }

    private static double segmentDistance(Vec3 from, Vec3 to, Vec3 point) {
        Vec3 segment = to.subtract(from);
        double length = segment.lengthSqr();
        double t = length == 0 ? 0 : Math.max(0, Math.min(1, point.subtract(from).dot(segment) / length));
        return from.add(segment.scale(t)).distanceTo(point);
    }
}
