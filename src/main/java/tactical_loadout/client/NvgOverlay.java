package tactical_loadout.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.device.Devices;
import tactical_loadout.device.MountedDevice;

public final class NvgOverlay {
    private static final ResourceLocation WHITE = TacticalLoadout.id("textures/misc/white.png");
    private static final ResourceLocation NOISE = TacticalLoadout.id("textures/misc/nvg_noise.png");
    private static final ResourceLocation MONO = TacticalLoadout.id("textures/misc/nvg_pvs14.png");
    private static final ResourceLocation PANO = TacticalLoadout.id("textures/misc/nvg_gpnvg18.png");
    private static final ResourceLocation THERMAL = TacticalLoadout.id("textures/misc/nvg_thermal.png");
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
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        RenderSystem.enableBlend();
        if (nvg.kind() != DeviceKind.THERMAL) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
            if (nvg.kind() == DeviceKind.GPNVG18) {
                RenderSystem.setShaderColor(0.82F, 0.95F, 1.0F, 1.0F);
            } else {
                RenderSystem.setShaderColor(0.35F, 1.0F, 0.42F, 1.0F);
            }
            graphics.blit(WHITE, 0, 0, 0, 0, width, height, 16, 16);
            RenderSystem.defaultBlendFunc();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, nvg.kind() == DeviceKind.THERMAL ? 0.07F : 0.12F);
        graphics.blit(NOISE, 0, 0, width, height, RANDOM.nextInt(128), RANDOM.nextInt(128), 128, 128, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!ClientConfig.nvgVignette()) {
            RenderSystem.disableBlend();
            return;
        }
        switch (nvg.kind()) {
            case GPNVG18, GPNVG18_GREEN -> {
                int w = Math.round(width * 1.4F);
                vignette(graphics, PANO, width, height, w, w / 2);
            }
            case THERMAL -> vignette(graphics, THERMAL, width, height, Math.round(width * 1.12F), Math.round(height * 1.12F));
            default -> {
                int size = Math.round(height * 1.7F);
                vignette(graphics, MONO, width, height, size, size);
            }
        }
        RenderSystem.disableBlend();
    }

    private static void vignette(GuiGraphics graphics, ResourceLocation texture, int width, int height, int w, int h) {
        int left = (width - w) / 2;
        int top = (height - h) / 2;
        graphics.blit(texture, left, top, 0, 0, w, h, w, h);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        if (left > 0) {
            graphics.blit(WHITE, 0, 0, 0, 0, left, height, 16, 16);
            graphics.blit(WHITE, left + w, 0, 0, 0, width - left - w, height, 16, 16);
        }
        if (top > 0) {
            graphics.blit(WHITE, 0, 0, 0, 0, width, top, 16, 16);
            graphics.blit(WHITE, 0, top + h, 0, 0, width, height - top - h, 16, 16);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
