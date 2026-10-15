package tactical_loadout.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tactical_loadout.client.ThermalVision;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void tactical_loadout$thermalGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ThermalVision.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}
