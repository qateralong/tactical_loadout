package tacz_camo_armor.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.platform.Platform;
import tacz_camo_armor.platform.RegistryEntry;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;

public final class ModCreativeTabs {
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN =
            Platform.INSTANCE.register(Registries.CREATIVE_MODE_TAB, "main", () ->
                Platform.INSTANCE.creativeTab()
                        .title(Component.translatable("itemGroup." + TaczCamoArmor.MOD_ID))
                        .icon(() -> ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.HEAVY, ArmorPiece.PLATE_CARRIER).toStack())
                        .displayItems((parameters, output) -> {
                            for (ArmorSet set : ArmorSet.values()) {
                                output.accept(ModItems.fabric(set));
                            }
                            output.accept(ModItems.KEVLAR_INSERT);
                            output.accept(ModItems.RAW_CERAMIC_PLATE);
                            output.accept(ModItems.CERAMIC_PLATE);
                            output.accept(ModItems.ARMOR_REPAIR_KIT);
                            output.accept(ModItems.BATTERY);
                            output.accept(ModItems.GAS_FILTER);
                            for (DeviceKind kind : DeviceKind.values()) {
                                output.accept(ModItems.device(kind));
                            }
                            for (GrenadeKind kind : GrenadeKind.values()) {
                                output.accept(ModItems.grenade(kind));
                            }
                            ModItems.allArmor().forEach(output::accept);
                        })
                        .build());

    private ModCreativeTabs() {
    }

    /** Loads the class so its entries get registered. */
    public static void init() {
    }
}
