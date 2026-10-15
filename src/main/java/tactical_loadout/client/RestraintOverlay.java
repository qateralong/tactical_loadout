package tactical_loadout.client;

import net.minecraft.ChatFormatting;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.gear.Restraints;

/** Tells a zip-tied player how to break free. */
public final class RestraintOverlay {
    private RestraintOverlay() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        int remaining = Restraints.clientRemaining();
        Minecraft mc = Minecraft.getInstance();
        if (remaining <= 0 || mc.player == null || mc.options.hideGui) {
            return;
        }
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 24;
        String prefix = "hud." + TacticalLoadout.MOD_ID + ".zip_ties.";
        graphics.drawCenteredString(mc.font, Component.translatable(prefix + "title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), x, y,
                0xFFFFFF);
        graphics.drawCenteredString(mc.font, Component.translatable(prefix + "struggle", mc.options.keyJump.getTranslatedKeyMessage(), remaining),
                x, y + 12, 0xFFFFFF);
    }
}
