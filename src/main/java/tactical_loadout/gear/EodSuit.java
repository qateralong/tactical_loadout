package tactical_loadout.gear;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import tactical_loadout.config.ServerConfig;
import tactical_loadout.item.ArmorSet;
import tactical_loadout.item.TacticalArmorItem;

/** The bomb suit: a full set shrugs off most of any explosion. */
public final class EodSuit {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private EodSuit() {
    }

    public static boolean wearsFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof TacticalArmorItem armor) || armor.set() != ArmorSet.EOD) {
                return false;
            }
        }
        return true;
    }

    public static float blastDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.IS_EXPLOSION) || !wearsFullSet(entity)) {
            return amount;
        }
        return amount * (float) (1.0 - ServerConfig.eodBlastReduction());
    }
}
