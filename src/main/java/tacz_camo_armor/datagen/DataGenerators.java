package tacz_camo_armor.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import tacz_camo_armor.TaczCamoArmor;

@EventBusSubscriber(modid = TaczCamoArmor.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new ModLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.Russian(output));
    }
}
