package tacz_camo_armor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.item.TacticalArmorItem;

public class TacticalArmorRenderer extends GeoArmorRenderer<TacticalArmorItem> {
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
