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
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.WOODLAND_FAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP);
        tag(ModTags.WOODLAND_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.DESERT_FAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH);
        tag(ModTags.DESERT_UNFAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP);
        tag(ModTags.MULTICAM_FAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SAVANNA);
        tag(ModTags.MULTICAM_UNFAVORED).addTags(Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.SNOW_FAVORED).addTags(Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.EMR_FAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_PLAINS);
        tag(ModTags.EMR_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.GHILLIE_FAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_JUNGLE,
                Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SAVANNA);
        tag(ModTags.GHILLIE_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.UCP_FAVORED).addTags(Tags.Biomes.IS_MOUNTAIN, Tags.Biomes.IS_STONY_SHORES, Tags.Biomes.IS_UNDERGROUND,
                Tags.Biomes.IS_WINDSWEPT);
        tag(ModTags.UCP_UNFAVORED).addTags(Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS);
        tag(ModTags.TIGER_FAVORED).addTags(Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_LUSH);
        tag(ModTags.TIGER_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.FLORA_FAVORED).addTags(Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SAVANNA, Tags.Biomes.IS_FOREST);
        tag(ModTags.FLORA_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
    }
}
