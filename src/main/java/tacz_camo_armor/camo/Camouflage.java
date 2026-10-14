package tacz_camo_armor.camo;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.registry.ModTags;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class Camouflage {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final double MAX_REDUCTION = 0.95;

    private Camouflage() {
    }

    @SubscribeEvent
    static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() == null) {
            return;
        }
        LivingEntity entity = event.getEntity();
        ArmorSet set = fullCamoSet(entity);
        if (set != null) {
            Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
            event.modifyVisibility(1.0 - reduction(set, entity.isDiscrete(), biome));
        }
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
        double base = sneaking ? ServerConfig.sneakingDetectionReduction() : ServerConfig.detectionReduction();
        return Mth.clamp(base * biomeFactor(set, biome), 0.0, MAX_REDUCTION);
    }

    public static double biomeFactor(ArmorSet set, Holder<Biome> biome) {
        TagKey<Biome> favored;
        TagKey<Biome> unfavored;
        switch (set) {
            case US_WOODLAND -> {
                favored = ModTags.WOODLAND_FAVORED;
                unfavored = ModTags.WOODLAND_UNFAVORED;
            }
            case US_DESERT -> {
                favored = ModTags.DESERT_FAVORED;
                unfavored = ModTags.DESERT_UNFAVORED;
            }
            default -> {
                return 1.0;
            }
        }
        if (biome.is(favored)) {
            return ServerConfig.favoredBiomeFactor();
        }
        return biome.is(unfavored) ? ServerConfig.unfavoredBiomeFactor() : 1.0;
    }
}
