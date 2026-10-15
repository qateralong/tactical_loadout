package tacz_camo_armor.camo;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.TacticalArmorItem;

public final class Camouflage {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final double MAX_REDUCTION = 0.95;

    private Camouflage() {
    }

    /** Multiplier for how far away mobs notice the entity. */
    public static double visibility(LivingEntity entity) {
        ArmorSet set = fullCamoSet(entity);
        if (set == null) {
            return 1.0;
        }
        Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
        return 1.0 - reduction(set, entity.isDiscrete(), biome);
    }

    @Nullable
    public static ArmorSet fullCamoSet(LivingEntity entity) {
        ArmorSet set = null;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof TacticalArmorItem armor)
                    || armor.getEquipmentSlot() != slot || !armor.set().hasCamouflage()
                    || (set != null && set != armor.set())) {
                return null;
            }
            set = armor.set();
        }
        return set;
    }

    public static double reduction(ArmorSet set, boolean sneaking, Holder<Biome> biome) {
        CamoPattern camo = set.camo();
        if (camo == null) {
            return 0.0;
        }
        double base = sneaking ? ServerConfig.sneakingDetectionReduction() : ServerConfig.detectionReduction();
        return Mth.clamp(base * camo.strength() * camo.biomeFactor(biome), 0.0, MAX_REDUCTION);
    }
}
