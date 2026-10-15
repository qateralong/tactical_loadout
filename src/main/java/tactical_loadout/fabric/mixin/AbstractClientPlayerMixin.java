package tactical_loadout.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tactical_loadout.client.ClientHooks;

@Mixin(AbstractClientPlayer.class)
abstract class AbstractClientPlayerMixin {
    @ModifyReturnValue(method = "getFieldOfViewModifier", at = @At("RETURN"))
    private float tactical_loadout$throwFov(float fov) {
        return ClientHooks.fovModifier((AbstractClientPlayer) (Object) this, fov);
    }
}
