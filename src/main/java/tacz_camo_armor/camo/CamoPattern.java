package tacz_camo_armor.camo;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.config.ServerConfig;
import tacz_camo_armor.registry.ModTags;

public enum CamoPattern {
    WOODLAND(ModTags.WOODLAND_FAVORED, ModTags.WOODLAND_UNFAVORED, false),
    DESERT(ModTags.DESERT_FAVORED, ModTags.DESERT_UNFAVORED, false),
    OLIVE(null, null, false),
    MULTICAM(ModTags.MULTICAM_FAVORED, ModTags.MULTICAM_UNFAVORED, false),
    SNOW(ModTags.SNOW_FAVORED, null, true),
    EMR(ModTags.EMR_FAVORED, ModTags.EMR_UNFAVORED, false),
    GHILLIE(ModTags.GHILLIE_FAVORED, ModTags.GHILLIE_UNFAVORED, false);

    @Nullable
    private final TagKey<Biome> favored;
    @Nullable
    private final TagKey<Biome> unfavored;
    private final boolean unfavoredElsewhere;

    CamoPattern(@Nullable TagKey<Biome> favored, @Nullable TagKey<Biome> unfavored, boolean unfavoredElsewhere) {
        this.favored = favored;
        this.unfavored = unfavored;
        this.unfavoredElsewhere = unfavoredElsewhere;
    }

    public double strength() {
        return this == GHILLIE ? ServerConfig.ghillieStrength() : 1.0;
    }

    public double biomeFactor(Holder<Biome> biome) {
        if (favored != null && biome.is(favored)) {
            return ServerConfig.favoredBiomeFactor();
        }
        if (unfavoredElsewhere || (unfavored != null && biome.is(unfavored))) {
            return ServerConfig.unfavoredBiomeFactor();
        }
        return 1.0;
    }
}
