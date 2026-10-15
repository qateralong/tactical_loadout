package tacz_camo_armor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ClientConfig;
import tacz_camo_armor.grenade.GrenadeItem;
import tacz_camo_armor.network.ToggleNvgPayload;
import tacz_camo_armor.platform.Platform;

/** Client logic shared by all loaders; the loader-specific code only forwards its events here. */
public final class ClientHooks {
    public static final KeyMapping TOGGLE_NVG = new KeyMapping("key." + TaczCamoArmor.MOD_ID + ".nvg",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories." + TaczCamoArmor.MOD_ID);

    private ClientHooks() {
    }

    /** Field of view multiplier while winding up a throw; the loaders apply the FOV effect scale before this point. */
    public static float fovModifier(Player player, float current) {
        float charge = grenadeCharge(player);
        if (charge <= 0.0F || !ClientConfig.throwFovEffect()) {
            return current;
        }
        double scale = Minecraft.getInstance().options.fovEffectScale().get();
        return current * (1.0F + 0.12F * charge * (float) scale);
    }

    // Winding up a throw should not crawl like drawing a bow; vanilla multiplies the input by 0.2 right after this.
    public static void onMovementInput(Player player, Input input) {
        if (grenadeCharge(player) > 0.0F) {
            input.forwardImpulse *= 3.5F;
            input.leftImpulse *= 3.5F;
        }
    }

    private static float grenadeCharge(Player player) {
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof GrenadeItem)) {
            return 0.0F;
        }
        return Math.max(0.01F, GrenadeItem.charge(player.getTicksUsingItem()));
    }

    public static void onLoggingOut() {
        FlashOverlay.reset();
    }

    public static void onClientTick() {
        ThermalVision.tick();
        while (TOGGLE_NVG.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                Platform.INSTANCE.sendToServer(new ToggleNvgPayload());
            }
        }
    }
}
