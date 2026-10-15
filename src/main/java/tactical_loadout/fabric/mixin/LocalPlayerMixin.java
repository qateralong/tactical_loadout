package tactical_loadout.fabric.mixin;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tactical_loadout.client.ClientHooks;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Shadow
    public Input input;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
    private void tactical_loadout$throwMovement(CallbackInfo ci) {
        ClientHooks.onMovementInput((LocalPlayer) (Object) this, input);
    }
}
