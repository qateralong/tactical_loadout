package tactical_loadout.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorTier;
import tactical_loadout.platform.Platform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {
    private static final Map<ArmorTier, Holder<ArmorMaterial>> BY_TIER = new EnumMap<>(ArmorTier.class);

    static {
        for (ArmorTier tier : ArmorTier.values()) {
            BY_TIER.put(tier, Platform.INSTANCE.register(Registries.ARMOR_MATERIAL, tier.id(), () -> create(tier)).holder());
        }
    }

    private ModArmorMaterials() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
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
