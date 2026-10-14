package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import tacz_camo_armor.TaczCamoArmor;

public final class ModTags {
    public static final TagKey<Item> FABRICS = ItemTags.create(TaczCamoArmor.id("fabrics"));
    public static final TagKey<Item> TACTICAL_ARMOR = ItemTags.create(TaczCamoArmor.id("tactical_armor"));
    public static final TagKey<DamageType> BALLISTIC = TagKey.create(Registries.DAMAGE_TYPE, TaczCamoArmor.id("ballistic"));

    public static final TagKey<Biome> WOODLAND_FAVORED = biome("camo/woodland_favored");
    public static final TagKey<Biome> WOODLAND_UNFAVORED = biome("camo/woodland_unfavored");
    public static final TagKey<Biome> DESERT_FAVORED = biome("camo/desert_favored");
    public static final TagKey<Biome> DESERT_UNFAVORED = biome("camo/desert_unfavored");
    public static final TagKey<Biome> MULTICAM_FAVORED = biome("camo/multicam_favored");
    public static final TagKey<Biome> MULTICAM_UNFAVORED = biome("camo/multicam_unfavored");
    public static final TagKey<Biome> SNOW_FAVORED = biome("camo/snow_favored");
    public static final TagKey<Biome> EMR_FAVORED = biome("camo/emr_favored");
    public static final TagKey<Biome> EMR_UNFAVORED = biome("camo/emr_unfavored");
    public static final TagKey<Biome> GHILLIE_FAVORED = biome("camo/ghillie_favored");
    public static final TagKey<Biome> GHILLIE_UNFAVORED = biome("camo/ghillie_unfavored");

    private ModTags() {
    }

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, TaczCamoArmor.id(path));
    }
}
