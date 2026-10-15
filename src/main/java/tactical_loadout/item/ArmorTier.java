package tactical_loadout.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum ArmorTier {
    LIGHT("light", new int[]{1, 3, 2, 1}, 15, 12, SoundEvents.ARMOR_EQUIP_LEATHER),
    MEDIUM("medium", new int[]{2, 5, 4, 1}, 22, 10, SoundEvents.ARMOR_EQUIP_CHAIN),
    HEAVY("heavy", new int[]{2, 6, 5, 2}, 30, 9, SoundEvents.ARMOR_EQUIP_IRON);

    private final String id;
    private final int[] defenseByPiece;
    private final int durabilityMultiplier;
    private final int enchantmentValue;
    private final Holder<SoundEvent> equipSound;

    ArmorTier(String id, int[] defenseByPiece, int durabilityMultiplier, int enchantmentValue, Holder<SoundEvent> equipSound) {
        this.id = id;
        this.defenseByPiece = defenseByPiece;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
    }

    public String id() {
        return id;
    }

    public int defense(ArmorPiece piece) {
        return defenseByPiece[piece.ordinal()];
    }

    public int durabilityMultiplier() {
        return durabilityMultiplier;
    }

    public int enchantmentValue() {
        return enchantmentValue;
    }

    public Holder<SoundEvent> equipSound() {
        return equipSound;
    }
}
