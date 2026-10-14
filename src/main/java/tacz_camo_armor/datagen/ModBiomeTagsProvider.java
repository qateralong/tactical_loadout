package tacz_camo_armor.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.registry.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, TaczCamoArmor.MOD_ID, fileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.WOODLAND_FAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP);
        tag(ModTags.WOODLAND_UNFAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH,
                Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY);
        tag(ModTags.DESERT_FAVORED).addTags(Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_BEACH);
        tag(ModTags.DESERT_UNFAVORED).addTags(Tags.Biomes.IS_FOREST, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_SWAMP);
    }
}
