package tacz_camo_armor.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorTier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, TaczCamoArmor.MOD_ID);

    private static final Map<ArmorTier, Holder<ArmorMaterial>> BY_TIER = new EnumMap<>(ArmorTier.class);

    static {
        for (ArmorTier tier : ArmorTier.values()) {
            BY_TIER.put(tier, ARMOR_MATERIALS.register(tier.id(), () -> create(tier)));
        }
    }

    private ModArmorMaterials() {
    }

    public static Holder<ArmorMaterial> get(ArmorTier tier) {
        return BY_TIER.get(tier);
    }

    private static ArmorMaterial create(ArmorTier tier) {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorPiece piece : ArmorPiece.values()) {
            defense.put(piece.type(), tier.defense(piece));
        }
        return new ArmorMaterial(
                defense,
                tier.enchantmentValue(),
                tier.equipSound(),
                () -> Ingredient.of(ModItems.repairComponent(tier)),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("iron"))),
                0.0F,
                0.0F);
    }
}
