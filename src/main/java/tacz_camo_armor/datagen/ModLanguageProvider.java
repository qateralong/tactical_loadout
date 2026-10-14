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
            for (ArmorTier tier : set.tiers()) {
                for (ArmorPiece piece : ArmorPiece.values()) {
                    add(ModItems.armor(set, tier, piece).get(), armorName(set, tier, piece));
                }
            }
        }
        addComponents();
        addTooltips();
        addConfig();
    }

    protected String setName(ArmorSet set) {
        return switch (set) {
            case CT -> "CT";
            case T -> "T";
            case US_WOODLAND -> "US Woodland";
            case US_DESERT -> "US Desert";
            case US_GREEN -> "US Green";
            case US_MULTICAM -> "US Multicam";
            case US_SNOW -> "US Snow";
            case RU_EMR -> "Ratnik EMR";
            case GHILLIE -> "Ghillie";
            case SWAT -> "SWAT";
        };
    }

    protected void tooltip(String name, String value) {
        add("tooltip." + TaczCamoArmor.MOD_ID + "." + name, value);
    }

    protected static String configKey(String name) {
        return TaczCamoArmor.MOD_ID + ".configuration." + name;
    }

    protected abstract String modName();

    protected abstract String fabricName(ArmorSet set);

    protected abstract String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece);

    protected abstract void addComponents();

    protected abstract void addTooltips();

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
                case US_MULTICAM -> "Multicam Fabric";
                case US_SNOW -> "Snow Camo Fabric";
                case RU_EMR -> "EMR Digital Fabric";
                case GHILLIE -> "Ghillie Fabric";
                case SWAT -> "Black Fabric";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            String tierName = switch (tier) {
                case LIGHT -> "Light";
                case MEDIUM -> "Medium";
                case HEAVY -> "Heavy";
            };
            boolean ghillie = set == ArmorSet.GHILLIE;
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "Balaclava" : ghillie ? "Hood" : "Helmet";
                case PLATE_CARRIER -> set == ArmorSet.RU_EMR ? "Body Armor" : ghillie ? "Jacket" : "Plate Carrier";
                case LEGGINGS -> ghillie ? "Pants" : "Leggings with Knee Pads";
                case BOOTS -> "Boots";
            };
            if (ghillie) {
                return "Ghillie " + pieceName;
            }
            return setName(set) + " " + tierName + " " + pieceName;
        }

        @Override
        protected void addComponents() {
            add(ModItems.KEVLAR_INSERT.get(), "Kevlar Insert");
            add(ModItems.RAW_CERAMIC_PLATE.get(), "Unfired Ceramic Plate");
            add(ModItems.CERAMIC_PLATE.get(), "Ceramic Plate");
        }

        @Override
        protected void addTooltips() {
            tooltip("bullet_protection", "Bullet protection: %s%% (%s)");
            tooltip("zone.helmet", "head");
            tooltip("zone.plate_carrier", "torso");
            tooltip("zone.leggings", "legs");
            tooltip("zone.boots", "feet");
            tooltip("camo", "Full set camouflage: -%s%% detection range (-%s%% sneaking)");
            tooltip("camo_biome.us_woodland", "Best in forests, jungles and swamps");
            tooltip("camo_biome.us_desert", "Best in deserts, badlands and beaches");
            tooltip("camo_biome.us_green", "Works the same in every biome");
            tooltip("camo_biome.us_multicam", "Best in forests, plains and savannas");
            tooltip("camo_biome.us_snow", "Only works well in snowy biomes");
            tooltip("camo_biome.ru_emr", "Best in forests, taiga and plains");
            tooltip("camo_biome.ghillie", "Strongest camo, best among vegetation");
        }

        @Override
        protected void addConfig() {
            add(configKey("protection"), "Bullet protection");
            add(configKey("lightReduction"), "Light tier damage reduction");
            add(configKey("mediumReduction"), "Medium tier damage reduction");
            add(configKey("heavyReduction"), "Heavy tier damage reduction");
            add(configKey("durabilityPerDamage"), "Durability loss per damage point");
            add(configKey("hitZones"), "Hit zones");
            add(configKey("bootsZoneTop"), "Boots zone top (fraction of height)");
            add(configKey("legsZoneTop"), "Leggings zone top (fraction of height)");
            add(configKey("mobility"), "Mobility");
            add(configKey("heavySlowdownPerPiece"), "Heavy armor slowdown per piece");
            add(configKey("camouflage"), "Camouflage");
            add(configKey("detectionReduction"), "Detection range reduction");
            add(configKey("sneakingDetectionReduction"), "Detection range reduction while sneaking");
            add(configKey("favoredBiomeFactor"), "Matching biome multiplier");
            add(configKey("unfavoredBiomeFactor"), "Clashing biome multiplier");
            add(configKey("ghillieStrength"), "Ghillie suit camo multiplier");
        }
    }

    public static final class Russian extends ModLanguageProvider {
        public Russian(PackOutput output) {
            super(output, "ru_ru");
        }

        @Override
        protected String setName(ArmorSet set) {
            return set == ArmorSet.RU_EMR ? "Ратник" : super.setName(set);
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
                case US_MULTICAM -> "Ткань Multicam";
                case US_SNOW -> "Снежная камуфляжная ткань";
                case RU_EMR -> "Цифровая ткань ЕМР";
                case GHILLIE -> "Маскировочная ткань гилли";
                case SWAT -> "Чёрная ткань";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            if (set == ArmorSet.GHILLIE) {
                return switch (piece) {
                    case HELMET -> "Капюшон гилли";
                    case PLATE_CARRIER -> "Куртка гилли";
                    case LEGGINGS -> "Штаны гилли";
                    case BOOTS -> "Ботинки гилли";
                };
            }
            boolean feminine = (piece == ArmorPiece.PLATE_CARRIER && set != ArmorSet.RU_EMR)
                    || (piece == ArmorPiece.HELMET && set == ArmorSet.T);
            boolean plural = piece == ArmorPiece.LEGGINGS || piece == ArmorPiece.BOOTS;
            String tierName = switch (tier) {
                case LIGHT -> plural ? "Лёгкие" : feminine ? "Лёгкая" : "Лёгкий";
                case MEDIUM -> plural ? "Средние" : feminine ? "Средняя" : "Средний";
                case HEAVY -> plural ? "Тяжёлые" : feminine ? "Тяжёлая" : "Тяжёлый";
            };
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "балаклава" : "шлем";
                case PLATE_CARRIER -> set == ArmorSet.RU_EMR ? "бронежилет" : "плитоноска";
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
        protected void addTooltips() {
            tooltip("bullet_protection", "Защита от пуль: %s%% (%s)");
            tooltip("zone.helmet", "голова");
            tooltip("zone.plate_carrier", "торс");
            tooltip("zone.leggings", "ноги");
            tooltip("zone.boots", "ступни");
            tooltip("camo", "Камуфляж полного комплекта: дальность обнаружения -%s%% (-%s%% крадучись)");
            tooltip("camo_biome.us_woodland", "Лучше всего в лесах, джунглях и болотах");
            tooltip("camo_biome.us_desert", "Лучше всего в пустынях, бэдлендах и на пляжах");
            tooltip("camo_biome.us_green", "Одинаково во всех биомах");
            tooltip("camo_biome.us_multicam", "Лучше всего в лесах, на равнинах и в саваннах");
            tooltip("camo_biome.us_snow", "Хорош только в снежных биомах");
            tooltip("camo_biome.ru_emr", "Лучше всего в лесах, тайге и на равнинах");
            tooltip("camo_biome.ghillie", "Самый сильный камуфляж, лучше всего среди растительности");
        }

        @Override
        protected void addConfig() {
            add(configKey("protection"), "Защита от пуль");
            add(configKey("lightReduction"), "Снижение урона: лёгкий тир");
            add(configKey("mediumReduction"), "Снижение урона: средний тир");
            add(configKey("heavyReduction"), "Снижение урона: тяжёлый тир");
            add(configKey("durabilityPerDamage"), "Потеря прочности за единицу урона");
            add(configKey("hitZones"), "Зоны попадания");
            add(configKey("bootsZoneTop"), "Верх зоны ботинок (доля роста)");
            add(configKey("legsZoneTop"), "Верх зоны поножей (доля роста)");
            add(configKey("mobility"), "Подвижность");
            add(configKey("heavySlowdownPerPiece"), "Замедление за каждую тяжёлую деталь");
            add(configKey("camouflage"), "Камуфляж");
            add(configKey("detectionReduction"), "Снижение дальности обнаружения");
            add(configKey("sneakingDetectionReduction"), "Снижение дальности обнаружения крадучись");
            add(configKey("favoredBiomeFactor"), "Множитель в подходящем биоме");
            add(configKey("unfavoredBiomeFactor"), "Множитель в неподходящем биоме");
            add(configKey("ghillieStrength"), "Множитель камуфляжа гилли");
        }
    }
}
