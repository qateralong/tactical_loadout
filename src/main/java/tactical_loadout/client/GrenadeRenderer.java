package tactical_loadout.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import tactical_loadout.grenade.GrenadeEntity;

public class GrenadeRenderer extends EntityRenderer<GrenadeEntity> {
    private static final float SCALE = 0.45F;
    private final ItemRenderer itemRenderer;

    public GrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.1F;
    }

    @Override
    public void render(GrenadeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isInvisible()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.heading()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.spin(partialTick)));
        poseStack.scale(SCALE, SCALE, SCALE);
        itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GrenadeEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
