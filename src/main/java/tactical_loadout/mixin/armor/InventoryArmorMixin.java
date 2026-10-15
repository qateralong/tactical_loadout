package tactical_loadout.mixin.armor;

//? if <1.21 {
/*import java.util.function.Consumer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tactical_loadout.combat.BallisticHits;

// Before 1.21 only players wear their armor out, through the inventory; tactical pieces take zone wear instead.
@Mixin(Inventory.class)
abstract class InventoryArmorMixin {
    @Redirect(method = "hurtArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private <T extends LivingEntity> void tactical_loadout$zoneWearOnly(ItemStack stack, int amount, T entity, Consumer<T> onBroken,
                                                                        DamageSource source, float damage, int[] slots) {
        if (!BallisticHits.skipsVanillaWear(source, stack)) {
            stack.hurtAndBreak(amount, entity, onBroken);
        }
    }
}
*///?}
