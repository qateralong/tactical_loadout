package tacz_camo_armor.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tacz_camo_armor.client.ThermalVision;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void tacz_camo_armor$thermalGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ThermalVision.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}
