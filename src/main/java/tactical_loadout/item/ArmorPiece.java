package tactical_loadout.item;

import net.minecraft.world.item.ArmorItem;

public enum ArmorPiece {
    HELMET("helmet", ArmorItem.Type.HELMET),
    PLATE_CARRIER("plate_carrier", ArmorItem.Type.CHESTPLATE),
    LEGGINGS("leggings", ArmorItem.Type.LEGGINGS),
    BOOTS("boots", ArmorItem.Type.BOOTS);

    private final String id;
    private final ArmorItem.Type type;

    ArmorPiece(String id, ArmorItem.Type type) {
        this.id = id;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public ArmorItem.Type type() {
        return type;
    }
}
