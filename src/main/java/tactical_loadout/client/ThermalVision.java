package tactical_loadout.client;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.device.Devices;
import tactical_loadout.device.MountedDevice;

public final class ThermalVision {
    private static final ResourceLocation SHADER = TacticalLoadout.id("shaders/post/thermal.json");
    private static final double RANGE = 64.0;
    // Line-of-sight checks run once per client tick instead of once per entity per frame.
    private static final IntSet VISIBLE = new IntOpenHashSet();
    private static boolean active;

    private ThermalVision() {
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return false;
        }
        MountedDevice device = Devices.activeNvg(mc.player);
        return device != null && device.kind() == DeviceKind.THERMAL;
    }

    public static boolean shouldGlow(Entity entity) {
        return active && VISIBLE.contains(entity.getId());
    }

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        active = isActive();
        VISIBLE.clear();
        if (active && mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof LivingEntity && !(entity instanceof ArmorStand) && entity != mc.player
                        && entity.distanceToSqr(mc.player) < RANGE * RANGE && mc.player.hasLineOfSight(entity)) {
                    VISIBLE.add(entity.getId());
                }
            }
        }
        updateShader();
    }

    private static void updateShader() {
        Minecraft mc = Minecraft.getInstance();
        PostChain current = mc.gameRenderer.currentEffect();
        boolean ours = current != null && current.getName().equals(SHADER.toString());
        if (active) {
            if (!ours) {
                mc.gameRenderer.loadEffect(SHADER);
            }
        } else if (ours) {
            mc.gameRenderer.shutdownEffect();
        }
    }
}
