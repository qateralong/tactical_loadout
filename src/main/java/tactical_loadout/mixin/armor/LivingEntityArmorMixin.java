package tactical_loadout.mixin.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.21 {
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tactical_loadout.combat.BallisticHits;

/** On loaders without armor events: tactical pieces neither count twice against bullets nor take the vanilla per-hit wear. */
@Mixin(LivingEntity.class)
abstract class LivingEntityArmorMixin {
    @Redirect(method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorValue()I"))
    private int tactical_loadout$withoutOwnArmor(LivingEntity self, DamageSource source, float damage) {
        return BallisticHits.armorWithoutOwn(self, source, self.getArmorValue());
    }

    //? if >=1.21 {
    @Redirect(method = "doHurtEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private void tactical_loadout$zoneWearOnly(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot,
                                               DamageSource source, float damage, EquipmentSlot[] slots) {
        if (!BallisticHits.skipsVanillaWear(source, stack)) {
            stack.hurtAndBreak(amount, entity, slot);
        }
    }
    //?}
}
