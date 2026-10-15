package tactical_loadout.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.platform.Platform;
import tactical_loadout.platform.RegistryEntry;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.grenade.GrenadeKind;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorSet;
import tactical_loadout.item.ArmorTier;

public final class ModCreativeTabs {
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN =
            Platform.INSTANCE.register(Registries.CREATIVE_MODE_TAB, "main", () ->
                Platform.INSTANCE.creativeTab()
                        .title(Component.translatable("itemGroup." + TacticalLoadout.MOD_ID))
                        .icon(() -> ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.HEAVY, ArmorPiece.PLATE_CARRIER).toStack())
                        .displayItems((parameters, output) -> {
                            for (ArmorSet set : ArmorSet.values()) {
                                output.accept(ModItems.fabric(set));
                            }
                            output.accept(ModItems.KEVLAR_INSERT);
                            output.accept(ModItems.RAW_CERAMIC_PLATE);
                            output.accept(ModItems.CERAMIC_PLATE);
                            output.accept(ModItems.ARMOR_REPAIR_KIT);
                            output.accept(ModItems.ZIP_TIES);
                            output.accept(ModItems.TRIPWIRE_ALARM);
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
