package tacz_camo_armor.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.DeviceItem;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.item.TacticalArmorItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TaczCamoArmor.MOD_ID);

    public static final DeferredItem<Item> KEVLAR_INSERT = ITEMS.registerSimpleItem("kevlar_insert");
    public static final DeferredItem<Item> RAW_CERAMIC_PLATE = ITEMS.registerSimpleItem("raw_ceramic_plate");
    public static final DeferredItem<Item> CERAMIC_PLATE = ITEMS.registerSimpleItem("ceramic_plate");
    public static final DeferredItem<Item> BATTERY = ITEMS.registerSimpleItem("battery");
    public static final DeferredItem<Item> GAS_FILTER = ITEMS.registerSimpleItem("gas_filter");

    private static final Map<DeviceKind, DeferredItem<DeviceItem>> DEVICES = new EnumMap<>(DeviceKind.class);

    private static final Map<ArmorSet, DeferredItem<Item>> FABRICS = new EnumMap<>(ArmorSet.class);
    private static final Map<ArmorSet, Map<ArmorTier, Map<ArmorPiece, DeferredItem<TacticalArmorItem>>>> ARMOR =
            new EnumMap<>(ArmorSet.class);
    private static final List<DeferredItem<TacticalArmorItem>> ALL_ARMOR = new ArrayList<>();

    static {
        for (DeviceKind kind : DeviceKind.values()) {
            DEVICES.put(kind, ITEMS.register(kind.id(), () -> new DeviceItem(kind, new Item.Properties())));
        }
        for (ArmorSet set : ArmorSet.values()) {
            FABRICS.put(set, ITEMS.registerSimpleItem(set.fabricId()));
        }
        for (ArmorSet set : ArmorSet.values()) {
            Map<ArmorTier, Map<ArmorPiece, DeferredItem<TacticalArmorItem>>> byTier = new EnumMap<>(ArmorTier.class);
            for (ArmorTier tier : set.tiers()) {
                Map<ArmorPiece, DeferredItem<TacticalArmorItem>> byPiece = new EnumMap<>(ArmorPiece.class);
                for (ArmorPiece piece : ArmorPiece.values()) {
                    String name = set.id() + "_" + tier.id() + "_" + piece.id();
                    DeferredItem<TacticalArmorItem> item = ITEMS.register(name,
                            () -> new TacticalArmorItem(set, tier, piece, new Item.Properties()));
                    byPiece.put(piece, item);
                    ALL_ARMOR.add(item);
                }
                byTier.put(tier, byPiece);
            }
            ARMOR.put(set, byTier);
        }
    }

    private ModItems() {
    }

    public static DeferredItem<DeviceItem> device(DeviceKind kind) {
        return DEVICES.get(kind);
    }

    public static DeferredItem<Item> fabric(ArmorSet set) {
        return FABRICS.get(set);
    }

    @Nullable
    public static DeferredItem<TacticalArmorItem> armor(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
        Map<ArmorPiece, DeferredItem<TacticalArmorItem>> byPiece = ARMOR.get(set).get(tier);
        return byPiece == null ? null : byPiece.get(piece);
    }

    public static List<DeferredItem<TacticalArmorItem>> allArmor() {
        return ALL_ARMOR;
    }

    public static Item repairComponent(ArmorTier tier) {
        return tier == ArmorTier.HEAVY ? CERAMIC_PLATE.get() : KEVLAR_INSERT.get();
    }
}
