package tactical_loadout.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tactical_loadout.camo.Camouflage;
import tactical_loadout.combat.BallisticHits;
import tactical_loadout.device.DeviceEvents;
import tactical_loadout.grenade.SmokeClouds;
import tactical_loadout.gear.GearEvents;
import tactical_loadout.medical.Downed;
import tactical_loadout.medical.Medical;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stands in for the NeoForge living entity events. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float tactical_loadout$zoneProtection(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        return BallisticHits.onIncomingDamage(self, source, GearEvents.incomingDamage(self, source, amount));
    }

    // Downed players don't regenerate.
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void tactical_loadout$noHealingWhileDown(float amount, CallbackInfo ci) {
        if ((Object) this instanceof Player player && Downed.blocksHealing(player)) {
            ci.cancel();
        }
    }

    //? if <1.21 {
    /*// Fabric API for these versions has no after-damage event; hurt returns false when shields or invulnerability stopped the hit.
    @Inject(method = "hurt", at = @At("RETURN"))
    private void tactical_loadout$afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        BallisticHits.onDamageDealt((LivingEntity) (Object) this, source, cir.getReturnValueZ() ? 0.0F : 1.0F);
        Medical.onDamageDealt((LivingEntity) (Object) this, source, cir.getReturnValueZ() ? amount : 0.0F);
    }

    *///?}
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void tactical_loadout$gasMask(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (DeviceEvents.blocksEffect((LivingEntity) (Object) this, effect)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "getVisibilityPercent", at = @At("RETURN"))
    private double tactical_loadout$camouflage(double visibility, @Nullable Entity looker) {
        if (looker == null) {
            return visibility;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        return visibility * Camouflage.visibility(self) * SmokeClouds.visibility(self, looker);
    }
}
