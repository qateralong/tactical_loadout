package tacz_camo_armor.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.ArmorTier;
import tacz_camo_armor.registry.ModItems;

public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, TaczCamoArmor.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TaczCamoArmor.MOD_ID, modName());
        for (ArmorSet set : ArmorSet.values()) {
            add(ModItems.fabric(set).get(), fabricName(set));
            for (ArmorTier tier : ArmorTier.values()) {
                for (ArmorPiece piece : ArmorPiece.values()) {
                    add(ModItems.armor(set, tier, piece).get(), armorName(set, tier, piece));
                }
            }
        }
        addComponents();
        addConfig();
    }

    protected static String setName(ArmorSet set) {
        return switch (set) {
            case CT -> "CT";
            case T -> "T";
            case US_WOODLAND -> "US Woodland";
            case US_DESERT -> "US Desert";
            case US_GREEN -> "US Green";
        };
    }

    protected static String configKey(String name) {
        return TaczCamoArmor.MOD_ID + ".configuration." + name;
    }

    protected abstract String modName();

    protected abstract String fabricName(ArmorSet set);

    protected abstract String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece);

    protected abstract void addComponents();

    protected abstract void addConfig();

    public static final class English extends ModLanguageProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected String modName() {
            return "Tactical Camo Armor";
        }

        @Override
        protected String fabricName(ArmorSet set) {
            return switch (set) {
                case CT -> "Blue Fabric";
                case T -> "Beige Fabric";
                case US_WOODLAND -> "Woodland Camo Fabric";
                case US_DESERT -> "Sand Fabric";
                case US_GREEN -> "Olive Drab Fabric";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            String tierName = switch (tier) {
                case LIGHT -> "Light";
                case MEDIUM -> "Medium";
                case HEAVY -> "Heavy";
            };
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "Balaclava" : "Helmet";
                case PLATE_CARRIER -> "Plate Carrier";
                case LEGGINGS -> "Leggings with Knee Pads";
                case BOOTS -> "Boots";
            };
            return setName(set) + " " + tierName + " " + pieceName;
        }

        @Override
        protected void addComponents() {
            add(ModItems.KEVLAR_INSERT.get(), "Kevlar Insert");
            add(ModItems.RAW_CERAMIC_PLATE.get(), "Unfired Ceramic Plate");
            add(ModItems.CERAMIC_PLATE.get(), "Ceramic Plate");
        }

        @Override
        protected void addConfig() {
            add(configKey("mobility"), "Mobility");
            add(configKey("heavySlowdownPerPiece"), "Heavy armor slowdown per piece");
        }
    }

    public static final class Russian extends ModLanguageProvider {
        public Russian(PackOutput output) {
            super(output, "ru_ru");
        }

        @Override
        protected String modName() {
            return "Тактическая камуфляжная броня";
        }

        @Override
        protected String fabricName(ArmorSet set) {
            return switch (set) {
                case CT -> "Синяя ткань";
                case T -> "Бежевая ткань";
                case US_WOODLAND -> "Камуфляжная ткань Woodland";
                case US_DESERT -> "Песочная ткань";
                case US_GREEN -> "Оливковая ткань";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            boolean feminine = piece == ArmorPiece.PLATE_CARRIER || (piece == ArmorPiece.HELMET && set == ArmorSet.T);
            boolean plural = piece == ArmorPiece.LEGGINGS || piece == ArmorPiece.BOOTS;
            String tierName = switch (tier) {
                case LIGHT -> plural ? "Лёгкие" : feminine ? "Лёгкая" : "Лёгкий";
                case MEDIUM -> plural ? "Средние" : feminine ? "Средняя" : "Средний";
                case HEAVY -> plural ? "Тяжёлые" : feminine ? "Тяжёлая" : "Тяжёлый";
            };
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "балаклава" : "шлем";
                case PLATE_CARRIER -> "плитоноска";
                case LEGGINGS -> "поножи с наколенниками";
                case BOOTS -> "ботинки";
            };
            return tierName + " " + pieceName + " " + setName(set);
        }

        @Override
        protected void addComponents() {
            add(ModItems.KEVLAR_INSERT.get(), "Кевларовая вставка");
            add(ModItems.RAW_CERAMIC_PLATE.get(), "Необожжённая керамическая пластина");
            add(ModItems.CERAMIC_PLATE.get(), "Керамическая пластина");
        }

        @Override
        protected void addConfig() {
            add(configKey("mobility"), "Подвижность");
            add(configKey("heavySlowdownPerPiece"), "Замедление за каждую тяжёлую деталь");
        }
    }
}
