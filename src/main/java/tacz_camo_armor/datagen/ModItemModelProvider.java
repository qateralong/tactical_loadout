package tacz_camo_armor.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.world.item.Item;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.DeviceItem;
import tacz_camo_armor.grenade.GrenadeItem;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.registry.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, TaczCamoArmor.MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.all().stream().map(item -> (Item) item.get()).filter(item -> !hasBlockModel(item)).forEach(this::basicItem);
    }

    // These items use 3D element models written by tools/items.py into the main resources.
    private static boolean hasBlockModel(Item item) {
        return item instanceof TacticalArmorItem || item instanceof GrenadeItem || item instanceof DeviceItem
                || item == ModItems.BATTERY.get() || item == ModItems.GAS_FILTER.get();
    }
}
