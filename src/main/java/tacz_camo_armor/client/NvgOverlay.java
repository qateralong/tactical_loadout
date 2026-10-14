package tacz_camo_armor.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.device.Devices;
import tacz_camo_armor.device.MountedDevice;

public final class NvgOverlay {
    private static final ResourceLocation WHITE = TaczCamoArmor.id("textures/misc/white.png");
    private static final ResourceLocation NOISE = TaczCamoArmor.id("textures/misc/nvg_noise.png");
    private static final ResourceLocation MONO = TaczCamoArmor.id("textures/misc/nvg_pvs14.png");
    private static final ResourceLocation PANO = TaczCamoArmor.id("textures/misc/nvg_gpnvg18.png");
    private static final RandomSource RANDOM = RandomSource.create();

    private NvgOverlay() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        MountedDevice nvg = Devices.activeNvg(mc.player);
        if (nvg == null) {
            return;
        }
        boolean panoramic = nvg.kind() == DeviceKind.GPNVG18;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
        if (panoramic) {
            RenderSystem.setShaderColor(0.82F, 0.95F, 1.0F, 1.0F);
        } else {
            RenderSystem.setShaderColor(0.35F, 1.0F, 0.42F, 1.0F);
        }
        graphics.blit(WHITE, 0, 0, 0, 0, width, height, 16, 16);

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.12F);
        graphics.blit(NOISE, 0, 0, width, height, RANDOM.nextInt(128), RANDOM.nextInt(128), 128, 128, 256, 256);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int vignetteWidth = Math.min(width, panoramic ? height * 2 : height);
        int left = (width - vignetteWidth) / 2;
        graphics.blit(panoramic ? PANO : MONO, left, 0, 0, 0, vignetteWidth, height, vignetteWidth, height);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        graphics.blit(WHITE, 0, 0, 0, 0, left, height, 16, 16);
        graphics.blit(WHITE, left + vignetteWidth, 0, 0, 0, width - left - vignetteWidth, height, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
