package tacz_camo_armor.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.grenade.IncendiaryFire;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        CompletableFuture<TagsProvider.TagLookup<Block>> noBlockTags = CompletableFuture.completedFuture(TagsProvider.TagLookup.empty());

        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, fileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.Russian(output));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookup, noBlockTags, fileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(output, lookup, fileHelper));
        RegistrySetBuilder registries = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, context -> context.register(
                IncendiaryFire.DAMAGE, new DamageType(TaczCamoArmor.MOD_ID + ".incendiary",
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.BURNING)));
        DatapackBuiltinEntriesProvider builtin = generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, lookup, registries, Set.of(TaczCamoArmor.MOD_ID)));
        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(output, builtin.getRegistryProvider(), fileHelper));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, fileHelper,
                List.of(new ModAdvancementProvider())));
    }
}
