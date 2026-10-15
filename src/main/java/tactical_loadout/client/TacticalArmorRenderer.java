package tactical_loadout.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tactical_loadout.registry.ModDataComponents;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.device.MountedDevice;
import tactical_loadout.item.ArmorTier;
import tactical_loadout.item.TacticalArmorItem;

import java.util.Map;
import java.util.WeakHashMap;

public class TacticalArmorRenderer extends GeoArmorRenderer<TacticalArmorItem> {
    private static final float NVG_STOWED = (float) Math.toRadians(100);
    private static final Map<Entity, NvgState> NVG_STATES = new WeakHashMap<>();
    private float nvgAngle;

    public TacticalArmorRenderer() {
        super(new TacticalArmorModel());
    }

    @Override
    public void preRender(PoseStack poseStack, TacticalArmorItem animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        for (GeoBone bone : model.topLevelBones()) {
            applyTier(bone, animatable.tier());
        }
        ItemStack stack = getCurrentStack();
        MountedDevice nvg = stack == null ? null : stack.get(ModDataComponents.NVG.get());
        boolean mask = stack != null && stack.has(ModDataComponents.GAS_MASK.get());
        setVisible(model, "nvg_mount", nvg != null);
        setVisible(model, "nvg_pvs14", nvg != null && nvg.kind() == DeviceKind.PVS14);
        setVisible(model, "nvg_gpnvg18", nvg != null && nvg.kind() == DeviceKind.GPNVG18);
        setVisible(model, "nvg_gpnvg18_green", nvg != null && nvg.kind() == DeviceKind.GPNVG18_GREEN);
        setVisible(model, "nvg_thermal", nvg != null && nvg.kind() == DeviceKind.THERMAL);
        setVisible(model, "nvg_battery", nvg != null && (nvg.kind() == DeviceKind.GPNVG18 || nvg.kind() == DeviceKind.GPNVG18_GREEN));
        setVisible(model, "gas_mask", mask);
        if (nvg != null) {
            boolean down = stack.getOrDefault(ModDataComponents.NVG_DOWN.get(), false);
            nvgAngle = animateNvg(getCurrentEntity(), down ? 0.0F : NVG_STOWED);
        }
    }

    private static void setVisible(BakedGeoModel model, String name, boolean visible) {
        model.getBone(name).ifPresent(bone -> bone.setHidden(!visible));
    }

    private static float animateNvg(@Nullable Entity entity, float target) {
        if (entity == null) {
            return target;
        }
        long now = System.nanoTime();
        NvgState state = NVG_STATES.computeIfAbsent(entity, e -> new NvgState(target, now));
        float dt = Math.min(0.1F, (now - state.time) / 1.0E9F);
        state.angle += (target - state.angle) * Math.min(1.0F, dt * 12.0F);
        state.time = now;
        return state.angle;
    }

    private static final class NvgState {
        private float angle;
        private long time;

        private NvgState(float angle, long time) {
            this.angle = angle;
            this.time = time;
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, TacticalArmorItem animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, int colour) {
        String name = bone.getName();
        if (name.startsWith("nvg_") && !name.equals("nvg_mount") && !name.equals("nvg_battery")) {
            bone.setRotX(nvgAngle);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
    }

    // Tier detail bones are named "medium_*" / "heavy_*" and nested under the slot bones.
    private static void applyTier(GeoBone bone, ArmorTier tier) {
        for (GeoBone child : bone.getChildBones()) {
            String name = child.getName();
            if (name.startsWith("medium_")) {
                child.setHidden(tier == ArmorTier.LIGHT);
            } else if (name.startsWith("heavy_")) {
                child.setHidden(tier != ArmorTier.HEAVY);
            }
            applyTier(child, tier);
        }
    }
}
