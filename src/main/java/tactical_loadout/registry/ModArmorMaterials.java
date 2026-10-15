package tactical_loadout.registry;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorTier;

import java.util.EnumMap;
import java.util.Map;
//? if >=1.21 {
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;

import java.util.List;
//?} else {
/*import net.minecraft.sounds.SoundEvent;
import tactical_loadout.TacticalLoadout;
*///?}

public final class ModArmorMaterials {
    //? if >=1.21 {
    private static final Map<ArmorTier, RegistryEntry<ArmorMaterial, ArmorMaterial>> BY_TIER = new EnumMap<>(ArmorTier.class);

    static {
        for (ArmorTier tier : ArmorTier.values()) {
            BY_TIER.put(tier, Platform.INSTANCE.register(Registries.ARMOR_MATERIAL, tier.id(), () -> create(tier)));
        }
    }
    //?} else {
    /*private static final Map<ArmorTier, ArmorMaterial> BY_TIER = new EnumMap<>(ArmorTier.class);

    static {
        for (ArmorTier tier : ArmorTier.values()) {
            BY_TIER.put(tier, new TierMaterial(tier));
        }
    }
    *///?}

    private ModArmorMaterials() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    //? if >=1.21 {
    public static Holder<ArmorMaterial> get(ArmorTier tier) {
        return BY_TIER.get(tier).holder();
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
                List.of(new ArmorMaterial.Layer(TacticalLoadout.location("minecraft", "iron"))),
                0.0F,
                0.0F);
    }
    //?} else {
    /*public static ArmorMaterial get(ArmorTier tier) {
        return BY_TIER.get(tier);
    }

    private record TierMaterial(ArmorTier tier) implements ArmorMaterial {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return ArmorPiece.of(type).durability(tier.durabilityMultiplier());
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return tier.defense(ArmorPiece.of(type));
        }

        @Override
        public int getEnchantmentValue() {
            return tier.enchantmentValue();
        }

        @Override
        public SoundEvent getEquipSound() {
            return tier.equipSound();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.repairComponent(tier));
        }

        @Override
        public String getName() {
            return TacticalLoadout.MOD_ID + ":" + tier.id();
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    }
    *///?}
}
