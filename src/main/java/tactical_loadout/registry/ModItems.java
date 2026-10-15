package tactical_loadout.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import tactical_loadout.device.DeviceItem;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.gear.ZipTiesItem;
import tactical_loadout.grenade.GrenadeItem;
import tactical_loadout.grenade.GrenadeKind;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorSet;
import tactical_loadout.item.ArmorTier;
import tactical_loadout.item.RepairKitItem;
import tactical_loadout.item.TacticalArmorItem;
import tactical_loadout.platform.ItemEntry;
import tactical_loadout.platform.Platform;

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
    public static final ItemEntry<ZipTiesItem> ZIP_TIES = register("zip_ties", () -> new ZipTiesItem(new Item.Properties()));
    public static final ItemEntry<BlockItem> TRIPWIRE_ALARM =
            register("tripwire_alarm", () -> new BlockItem(ModBlocks.TRIPWIRE_ALARM.get(), new Item.Properties()));

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
