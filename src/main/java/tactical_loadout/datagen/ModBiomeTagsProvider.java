package tactical_loadout.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.registry.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, TacticalLoadout.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.WOODLAND_FAVORED).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_TAIGA).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP);
        tag(ModTags.WOODLAND_UNFAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.DESERT_FAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH);
        tag(ModTags.DESERT_UNFAVORED).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_TAIGA).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP);
        tag(ModTags.MULTICAM_FAVORED).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_SAVANNA);
        tag(ModTags.MULTICAM_UNFAVORED).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.SNOW_FAVORED).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.EMR_FAVORED).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_TAIGA).addOptionalTag(Tags.Biomes.IS_PLAINS);
        tag(ModTags.EMR_UNFAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.GHILLIE_FAVORED).addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_TAIGA).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_SAVANNA);
        tag(ModTags.GHILLIE_UNFAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.UCP_FAVORED).addOptionalTag(Tags.Biomes.IS_MOUNTAIN).addOptionalTag(Tags.Biomes.IS_STONY_SHORES).addOptionalTag(Tags.Biomes.IS_UNDERGROUND).addOptionalTag(Tags.Biomes.IS_WINDSWEPT);
        tag(ModTags.UCP_UNFAVORED).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS);
        tag(ModTags.TIGER_FAVORED).addOptionalTag(Tags.Biomes.IS_JUNGLE).addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_LUSH);
        tag(ModTags.TIGER_UNFAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
        tag(ModTags.FLORA_FAVORED).addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_SAVANNA).addOptionalTag(Tags.Biomes.IS_FOREST);
        tag(ModTags.FLORA_UNFAVORED).addOptionalTag(Tags.Biomes.IS_DESERT).addOptionalTag(Tags.Biomes.IS_BADLANDS).addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_ICY);
    }
}
