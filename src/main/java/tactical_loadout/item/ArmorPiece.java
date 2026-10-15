package tactical_loadout.item;

import net.minecraft.world.item.ArmorItem;

public enum ArmorPiece {
    HELMET("helmet", ArmorItem.Type.HELMET, 11),
    PLATE_CARRIER("plate_carrier", ArmorItem.Type.CHESTPLATE, 16),
    LEGGINGS("leggings", ArmorItem.Type.LEGGINGS, 15),
    BOOTS("boots", ArmorItem.Type.BOOTS, 13);

    private final String id;
    private final ArmorItem.Type type;
    private final int baseDurability;

    ArmorPiece(String id, ArmorItem.Type type, int baseDurability) {
        this.id = id;
        this.type = type;
        this.baseDurability = baseDurability;
    }

    public static ArmorPiece of(ArmorItem.Type type) {
        for (ArmorPiece piece : values()) {
            if (piece.type == type) {
                return piece;
            }
        }
        throw new IllegalArgumentException("No armor piece for " + type);
    }

    public String id() {
        return id;
    }

    public ArmorItem.Type type() {
        return type;
    }

    /** Vanilla armor durability: a per-slot base times the material multiplier. */
    public int durability(int multiplier) {
        return baseDurability * multiplier;
    }
}
