package tactical_loadout.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.config.ClientConfig;
import tactical_loadout.gear.Restraints;
import tactical_loadout.gear.RiotShieldItem;
import tactical_loadout.medical.Downed;
import tactical_loadout.grenade.GrenadeItem;
import tactical_loadout.network.StrugglePayload;
import tactical_loadout.network.ToggleNvgPayload;
import tactical_loadout.platform.Platform;

/** Client logic shared by all loaders; the loader-specific code only forwards its events here. */
public final class ClientHooks {
    public static final KeyMapping TOGGLE_NVG = new KeyMapping("key." + TacticalLoadout.MOD_ID + ".nvg",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories." + TacticalLoadout.MOD_ID);

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
        } else if (player.isUsingItem() && player.getUseItem().getItem() instanceof RiotShieldItem) {
            // Walking behind a raised ballistic shield at half speed instead of the shield's crawl.
            input.forwardImpulse *= 2.5F;
            input.leftImpulse *= 2.5F;
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
        Restraints.setClientRemaining(0);
        Downed.clearClient();
    }

    public static void onDowned(int entityId, int ticks) {
        Downed.setClientDowned(entityId, ticks > 0);
        Player local = Minecraft.getInstance().player;
        if (local != null && local.getId() == entityId) {
            Downed.setClientTicks(ticks);
        }
        if (ticks <= 0 && local != null && local.level().getEntity(entityId) instanceof Player player) {
            player.setForcedPose(null);
        }
    }

    /** After every player's tick on the client: downed players keep crawling. */
    public static void onPlayerTick(Player player) {
        if (Downed.isClientDowned(player)) {
            Downed.applyPose(player);
        }
    }

    public static void onClientTick() {
        ThermalVision.tick();
        Downed.clientTick();
        while (TOGGLE_NVG.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                Platform.INSTANCE.sendToServer(new ToggleNvgPayload());
            }
        }
        // Tied players hammer the jump key to break free; jumping itself is already disabled.
        KeyMapping jump = Minecraft.getInstance().options.keyJump;
        if (Restraints.clientRemaining() > 0) {
            while (jump.consumeClick()) {
                Platform.INSTANCE.sendToServer(new StrugglePayload());
            }
        }
    }
}
