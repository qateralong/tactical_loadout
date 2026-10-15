package tactical_loadout.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.world.item.Item;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.device.DeviceItem;
import tactical_loadout.grenade.GrenadeItem;
import tactical_loadout.item.TacticalArmorItem;
import tactical_loadout.registry.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, TacticalLoadout.MOD_ID, fileHelper);
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
