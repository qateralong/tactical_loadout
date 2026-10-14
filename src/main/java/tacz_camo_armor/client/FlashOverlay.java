package tacz_camo_armor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ClientConfig;

public final class FlashOverlay {
    private static final ResourceLocation WHITE = TaczCamoArmor.id("textures/misc/white.png");
    private static final long FADE_MS = 3000L;
    private static float strength;
    private static long start;
    private static long duration;

    private FlashOverlay() {
    }

    public static void trigger(float value, int ticks) {
        float current = alpha();
        if (value >= current) {
            strength = value;
            start = System.currentTimeMillis();
            duration = Math.max(500L, ticks * 50L);
        }
    }

    // CS2-like: full white for ~40% of the duration, then up to a 3 s fade.
    private static float alpha() {
        long elapsed = System.currentTimeMillis() - start;
        if (duration == 0 || elapsed >= duration) {
            return 0.0F;
        }
        float peak = Math.min(ClientConfig.flashMaxOpacity(), 0.55F + strength);
        long fade = Math.min(FADE_MS, Math.round(duration * 0.6));
        long hold = duration - fade;
        if (elapsed < hold) {
            return peak;
        }
        return peak * (1.0F - (float) (elapsed - hold) / fade);
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        float alpha = alpha();
        if (alpha <= 0.0F) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(WHITE, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
