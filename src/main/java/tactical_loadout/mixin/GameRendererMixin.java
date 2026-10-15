package tactical_loadout.mixin;

//? if <1.21 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tactical_loadout.client.NvgOverlay;

// Before 1.21 the HUD is skipped entirely with F1, so the night vision tint is drawn just before that check, under the HUD.
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void tactical_loadout$nightVision(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        GuiGraphics graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        NvgOverlay.render(graphics, partialTick);
        graphics.flush();
    }
}
*///?}
