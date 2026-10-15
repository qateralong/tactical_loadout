package tactical_loadout.mixin.armor;

//? if <1.21 {
/*import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.gear.EodSuit;
import tactical_loadout.gear.Helpless;

// Before 1.21 players have no jump strength attribute: tied and downed players can't jump, a full bomb suit jumps lower.
@Mixin(LivingEntity.class)
abstract class LivingEntityJumpMixin {
    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    private void tactical_loadout$jumpPower(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && Helpless.is(player)) {
            cir.setReturnValue(0.0F);
        } else if (EodSuit.wearsFullSet(self)) {
            cir.setReturnValue(cir.getReturnValueF() * (float) (1.0 - ServerConfig.eodJumpReduction()));
        }
    }
}
*///?}
