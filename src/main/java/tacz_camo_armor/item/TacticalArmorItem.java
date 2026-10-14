package tacz_camo_armor.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.registry.ModArmorMaterials;

public class TacticalArmorItem extends ArmorItem {
    private final ArmorSet set;
    private final ArmorTier tier;
    private final ArmorPiece piece;

    public TacticalArmorItem(ArmorSet set, ArmorTier tier, ArmorPiece piece, Properties properties) {
        super(ModArmorMaterials.get(tier), piece.type(),
                properties.durability(piece.type().getDurability(tier.durabilityMultiplier())));
        this.set = set;
        this.tier = tier;
        this.piece = piece;
    }

    public ArmorSet set() {
        return set;
    }

    public ArmorTier tier() {
        return tier;
    }

    public ArmorPiece piece() {
        return piece;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);
        if (tier != ArmorTier.HEAVY) {
            return modifiers;
        }
        double slowdown = ServerConfig.heavySlowdownPerPiece();
        if (slowdown <= 0) {
            return modifiers;
        }
        AttributeModifier modifier = new AttributeModifier(
                TaczCamoArmor.id("heavy_slowdown_" + piece.id()), -slowdown,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        return modifiers.withModifierAdded(Attributes.MOVEMENT_SPEED, modifier, EquipmentSlotGroup.bySlot(getEquipmentSlot()));
    }
}
