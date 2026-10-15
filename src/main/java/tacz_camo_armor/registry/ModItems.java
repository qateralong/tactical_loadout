package tacz_camo_armor.registry;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import tacz_camo_armor.device.DeviceItem;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.grenade.GrenadeItem;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.item.RepairKitItem;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.platform.ItemEntry;
import tacz_camo_armor.platform.Platform;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    private static final List<ItemEntry<?>> ALL = new ArrayList<>();

    public static final ItemEntry<Item> KEVLAR_INSERT = simple("kevlar_insert");
    public static final ItemEntry<Item> RAW_CERAMIC_PLATE = simple("raw_ceramic_plate");
    public static final ItemEntry<Item> CERAMIC_PLATE = simple("ceramic_plate");
    public static final ItemEntry<Item> BATTERY = simple("battery");
    public static final ItemEntry<Item> GAS_FILTER = simple("gas_filter");
    public static final ItemEntry<RepairKitItem> ARMOR_REPAIR_KIT =
            register("armor_repair_kit", () -> new RepairKitItem(new Item.Properties()));

    private static final Map<DeviceKind, ItemEntry<DeviceItem>> DEVICES = new EnumMap<>(DeviceKind.class);
    private static final Map<GrenadeKind, ItemEntry<GrenadeItem>> GRENADES = new EnumMap<>(GrenadeKind.class);

    private static final Map<ArmorSet, ItemEntry<Item>> FABRICS = new EnumMap<>(ArmorSet.class);
    private static final Map<ArmorSet, Map<ArmorTier, Map<ArmorPiece, ItemEntry<TacticalArmorItem>>>> ARMOR =
            new EnumMap<>(ArmorSet.class);
    private static final List<ItemEntry<TacticalArmorItem>> ALL_ARMOR = new ArrayList<>();

    static {
        for (GrenadeKind kind : GrenadeKind.values()) {
            GRENADES.put(kind, register(kind.id(), () -> new GrenadeItem(kind, new Item.Properties())));
        }
        for (DeviceKind kind : DeviceKind.values()) {
            DEVICES.put(kind, register(kind.id(), () -> new DeviceItem(kind, new Item.Properties())));
        }
        for (ArmorSet set : ArmorSet.values()) {
            FABRICS.put(set, simple(set.fabricId()));
        }
        for (ArmorSet set : ArmorSet.values()) {
            Map<ArmorTier, Map<ArmorPiece, ItemEntry<TacticalArmorItem>>> byTier = new EnumMap<>(ArmorTier.class);
            for (ArmorTier tier : set.tiers()) {
                Map<ArmorPiece, ItemEntry<TacticalArmorItem>> byPiece = new EnumMap<>(ArmorPiece.class);
                for (ArmorPiece piece : ArmorPiece.values()) {
                    String name = set.id() + "_" + tier.id() + "_" + piece.id();
                    ItemEntry<TacticalArmorItem> item = register(name,
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

    /** Loads the class so its entries get registered. */
    public static void init() {
    }

    private static <T extends Item> ItemEntry<T> register(String name, Supplier<T> factory) {
        ItemEntry<T> entry = Platform.INSTANCE.registerItem(name, factory);
        ALL.add(entry);
        return entry;
    }

    private static ItemEntry<Item> simple(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    /** Every item of the mod in registration order. */
    public static List<ItemEntry<?>> all() {
        return ALL;
    }

    public static ItemEntry<GrenadeItem> grenade(GrenadeKind kind) {
        return GRENADES.get(kind);
    }

    public static ItemEntry<DeviceItem> device(DeviceKind kind) {
        return DEVICES.get(kind);
    }

    public static ItemEntry<Item> fabric(ArmorSet set) {
        return FABRICS.get(set);
    }

    @Nullable
    public static ItemEntry<TacticalArmorItem> armor(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
        Map<ArmorPiece, ItemEntry<TacticalArmorItem>> byPiece = ARMOR.get(set).get(tier);
        return byPiece == null ? null : byPiece.get(piece);
    }

    public static List<ItemEntry<TacticalArmorItem>> allArmor() {
        return ALL_ARMOR;
    }

    public static Item repairComponent(ArmorTier tier) {
        return tier == ArmorTier.HEAVY ? CERAMIC_PLATE.get() : KEVLAR_INSERT.get();
    }
}
