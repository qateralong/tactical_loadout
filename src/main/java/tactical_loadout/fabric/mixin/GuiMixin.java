package tactical_loadout.fabric.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tactical_loadout.client.NvgOverlay;

/** Draws the night vision tint before the vanilla HUD layers: below the hotbar, and still shown with F1 like on NeoForge. */
@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void tactical_loadout$nightVision(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        NvgOverlay.render(graphics, delta);
    }
}
