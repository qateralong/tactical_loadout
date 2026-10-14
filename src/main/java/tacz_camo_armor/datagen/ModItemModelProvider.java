package tacz_camo_armor.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.registry.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, TaczCamoArmor.MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.ITEMS.getEntries().forEach(item -> basicItem(item.get()));
    }
}
