package tacz_camo_armor.registry;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import tacz_camo_armor.TaczCamoArmor;

public final class ModTags {
    public static final TagKey<Item> FABRICS = ItemTags.create(TaczCamoArmor.id("fabrics"));
    public static final TagKey<Item> TACTICAL_ARMOR = ItemTags.create(TaczCamoArmor.id("tactical_armor"));

    private ModTags() {
    }
}
