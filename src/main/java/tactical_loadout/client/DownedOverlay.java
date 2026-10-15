package tactical_loadout.client;

import net.minecraft.ChatFormatting;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.medical.Downed;

/** A red haze and the bleed-out countdown for a downed player. */
public final class DownedOverlay {
    private DownedOverlay() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        int ticks = Downed.clientTicks();
        Minecraft mc = Minecraft.getInstance();
        if (ticks <= 0 || mc.player == null) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        graphics.fillGradient(0, 0, width, height / 2, 0x40600000, 0x10600000);
        graphics.fillGradient(0, height / 2, width, height, 0x10600000, 0x70600000);
        if (mc.options.hideGui) {
            return;
        }
        String prefix = "hud." + TacticalLoadout.MOD_ID + ".downed.";
        int x = width / 2;
        int y = height / 2 - 40;
        graphics.drawCenteredString(mc.font, Component.translatable(prefix + "title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), x, y,
                0xFFFFFF);
        graphics.drawCenteredString(mc.font, Component.translatable(prefix + "bleeding", (ticks + 19) / 20), x, y + 12, 0xFFFFFF);
        graphics.drawCenteredString(mc.font, Component.translatable(prefix + "hint").withStyle(ChatFormatting.GRAY), x, y + 24, 0xFFFFFF);
    }
}
