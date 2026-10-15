package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import tactical_loadout.TacticalLoadout;

public final class ModTags {
    public static final TagKey<Item> FABRICS = TagKey.create(Registries.ITEM, TacticalLoadout.id("fabrics"));
    public static final TagKey<Item> TACTICAL_ARMOR = TagKey.create(Registries.ITEM, TacticalLoadout.id("tactical_armor"));
    public static final TagKey<DamageType> BALLISTIC = TagKey.create(Registries.DAMAGE_TYPE, TacticalLoadout.id("ballistic"));

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
    public static final TagKey<Biome> UCP_FAVORED = biome("camo/ucp_favored");
    public static final TagKey<Biome> UCP_UNFAVORED = biome("camo/ucp_unfavored");
    public static final TagKey<Biome> TIGER_FAVORED = biome("camo/tiger_favored");
    public static final TagKey<Biome> TIGER_UNFAVORED = biome("camo/tiger_unfavored");
    public static final TagKey<Biome> FLORA_FAVORED = biome("camo/flora_favored");
    public static final TagKey<Biome> FLORA_UNFAVORED = biome("camo/flora_unfavored");

    private ModTags() {
    }

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, TacticalLoadout.id(path));
    }
}
