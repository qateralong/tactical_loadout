package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TaczCamoArmor.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TaczCamoArmor.MOD_ID))
                    .icon(() -> ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.HEAVY, ArmorPiece.PLATE_CARRIER).toStack())
                    .displayItems((parameters, output) -> {
                        for (ArmorSet set : ArmorSet.values()) {
                            output.accept(ModItems.fabric(set));
                        }
                        output.accept(ModItems.KEVLAR_INSERT);
                        output.accept(ModItems.RAW_CERAMIC_PLATE);
                        output.accept(ModItems.CERAMIC_PLATE);
                        output.accept(ModItems.BATTERY);
                        output.accept(ModItems.GAS_FILTER);
                        for (DeviceKind kind : DeviceKind.values()) {
                            output.accept(ModItems.device(kind));
                        }
                        ModItems.allArmor().forEach(output::accept);
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
