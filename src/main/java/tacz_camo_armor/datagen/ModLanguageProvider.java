package tacz_camo_armor.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.device.DeviceKind;
import tacz_camo_armor.grenade.GrenadeKind;
import tacz_camo_armor.item.ArmorForm;
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
        addDevices();
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
            case US_UCP -> "US UCP";
            case US_TIGER -> "US Tiger Stripe";
            case RU_EMR -> "Ratnik EMR";
            case RU_FLORA -> "Flora";
            case GHILLIE -> "Ghillie";
            case GHILLIE_SNOW -> "Snow Ghillie";
            case GHILLIE_DESERT -> "Desert Ghillie";
            case SWAT -> "SWAT";
        };
    }

    protected void tooltip(String name, String value) {
        add("tooltip." + TaczCamoArmor.MOD_ID + "." + name, value);
    }

    protected void advancement(String name, String title, String description) {
        add("advancement." + TaczCamoArmor.MOD_ID + "." + name + ".title", title);
        add("advancement." + TaczCamoArmor.MOD_ID + "." + name + ".description", description);
    }

    protected static String configKey(String name) {
        return TaczCamoArmor.MOD_ID + ".configuration." + name;
    }

    protected abstract String modName();

    protected abstract String fabricName(ArmorSet set);

    protected abstract String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece);

    protected abstract void addComponents();

    protected abstract void addTooltips();

    protected abstract void addDevices();

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
                case US_UCP -> "UCP Digital Fabric";
                case US_TIGER -> "Tiger Stripe Fabric";
                case RU_EMR -> "EMR Digital Fabric";
                case RU_FLORA -> "Flora Fabric";
                case GHILLIE -> "Ghillie Fabric";
                case GHILLIE_SNOW -> "Snow Ghillie Fabric";
                case GHILLIE_DESERT -> "Desert Ghillie Fabric";
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
            boolean ghillie = set.form() == ArmorForm.GHILLIE;
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "Balaclava" : ghillie ? "Hood" : "Helmet";
                case PLATE_CARRIER -> set.form() == ArmorForm.RU ? "Body Armor" : ghillie ? "Jacket" : "Plate Carrier";
                case LEGGINGS -> ghillie ? "Pants" : "Leggings with Knee Pads";
                case BOOTS -> "Boots";
            };
            if (ghillie) {
                return setName(set) + " " + pieceName;
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
        protected void addDevices() {
            add(ModItems.BATTERY.get(), "Battery");
            add(ModItems.GAS_FILTER.get(), "Gas Mask Filter");
            add(ModItems.device(DeviceKind.PVS14).get(), "PVS-14 Night Vision Monocular");
            add(ModItems.device(DeviceKind.GPNVG18).get(), "GPNVG-18 Panoramic NVG (White Phosphor)");
            add(ModItems.device(DeviceKind.GPNVG18_GREEN).get(), "GPNVG-18 Panoramic NVG (Green Phosphor)");
            add("jei.tacz_camo_armor.gpnvg18_green", "Four green-phosphor tubes with a wide panoramic view.");
            add(ModItems.device(DeviceKind.THERMAL).get(), "Thermal Imaging Monocular");
            add("jei.tacz_camo_armor.thermal", "White-hot thermal imager: living creatures in your line of sight glow, even through camouflage.");
            add(ModItems.device(DeviceKind.GAS_MASK).get(), "Gas Mask");
            add("key.tacz_camo_armor.nvg", "Flip NVG");
            add("key.categories.tacz_camo_armor", "Tactical Camo Armor");
            add("message.tacz_camo_armor.nvg_empty", "NVG battery is empty");
            add("tooltip.tacz_camo_armor.device.battery", "Battery: %s%%");
            add("tooltip.tacz_camo_armor.device.filter", "Filter: %s%%");
            add("tooltip.tacz_camo_armor.device.unlimited", "Battery: unlimited");
            add("tooltip.tacz_camo_armor.device.attach_hint", "Combine with a helmet in a crafting grid to attach");
            add("tooltip.tacz_camo_armor.device.nvg_helmets", "Fits US, Ratnik, SWAT and CT helmets");
            add("tooltip.tacz_camo_armor.device.mask_effect", "Blocks poison, wither and dragon's breath");
            add("tooltip.tacz_camo_armor.helmet.mounted", "Mounted: %s");
            add("tooltip.tacz_camo_armor.helmet.nvg_key", "Press %s to flip the NVG");
            add("tooltip.tacz_camo_armor.helmet.detach_hint", "Put the helmet alone in a crafting grid to detach");
            add("jei.tacz_camo_armor.protection", "Bullet protection in the hit zone: light %s%%, medium %s%%, heavy %s%%. Armor-piercing ammo lowers it.");
            add("jei.tacz_camo_armor.protection_single", "Bullet protection in the hit zone: %s%%. Armor-piercing ammo lowers it.");
            add("jei.tacz_camo_armor.heavy", "Each heavy piece slows you down by %s%%.");
            add("jei.tacz_camo_armor.zones", "Zones: helmet covers headshots, chest piece the torso, leggings the legs, boots the feet. Works against TaCZ bullets, arrows and other projectiles, including bullets from gun mods that count as projectiles.");
            add("jei.tacz_camo_armor.camo", "A full set of one color shrinks mob detection range by %s%% (%s%% while sneaking).");
            add("jei.tacz_camo_armor.no_camo", "This set gives no camouflage.");
            add("jei.tacz_camo_armor.nvg_fits", "The helmet accepts a night vision device and a gas mask.");
            add("jei.tacz_camo_armor.mask_fits", "The helmet accepts a gas mask.");
            add("jei.tacz_camo_armor.nvg", "Attach to a US, Ratnik, SWAT or CT helmet in a crafting grid. Press %s to flip it down for night vision. A battery lasts %s seconds; recharge it with a battery in a crafting grid.");
            add("jei.tacz_camo_armor.pvs14", "Single green-phosphor tube with a narrow field of view.");
            add("jei.tacz_camo_armor.gpnvg18", "Four white-phosphor tubes with a wide panoramic view.");
            add("jei.tacz_camo_armor.gas_mask", "Attach to any helmet in a crafting grid. Blocks poison, wither and dragon's breath. A filter lasts %s seconds of blocked effects; refill it with a filter.");
            add("jei.tacz_camo_armor.detach", "To remove an attachment, put the helmet alone in a crafting grid.");
            add("jei.tacz_camo_armor.battery", "Recharges a night vision device, loose or mounted on a helmet.");
            add("jei.tacz_camo_armor.gas_filter", "Refills a gas mask, loose or mounted on a helmet.");
            add("tacz_camo_armor.configuration.devices", "Devices");
            add("tacz_camo_armor.configuration.nvgUsesBattery", "NVG uses battery");
            add("tacz_camo_armor.configuration.nvgBatterySeconds", "NVG battery life (seconds)");
            add("tacz_camo_armor.configuration.gasFilterSeconds", "Gas mask filter capacity (seconds)");
            add(ModItems.grenade(GrenadeKind.FLASH).get(), "Flashbang");
            add("tooltip.tacz_camo_armor.grenade.flashbang", "Blinds everyone who looks at it; walls protect");
            add("jei.tacz_camo_armor.grenade.flashbang", "Goes off 1.5 s after the throw. The flash is strongest up close and when you look straight at it; anything behind a wall is safe. Players see a white screen, mobs lose their target and slow down.");
            add(ModItems.grenade(GrenadeKind.SMOKE).get(), "Smoke Grenade");
            add("tooltip.tacz_camo_armor.grenade.smoke_grenade", "Thick smoke that mobs can't see through");
            add("jei.tacz_camo_armor.grenade.smoke_grenade", "Starts smoking 2 s after the throw. Mobs can't see anyone inside the cloud or through it. A thermal imager still sees living things in the smoke.");
            add(ModItems.grenade(GrenadeKind.FRAG).get(), "Frag Grenade");
            add("tooltip.tacz_camo_armor.grenade.frag_grenade", "Explodes after 3 s without breaking blocks");
            add("jei.tacz_camo_armor.grenade.frag_grenade", "Explodes 3 s after the throw. Hurts everything nearby but never breaks blocks.");
            add(ModItems.grenade(GrenadeKind.HE).get(), "HE Grenade");
            add("tooltip.tacz_camo_armor.grenade.he_grenade", "Explodes after 3 s and breaks blocks");
            add("jei.tacz_camo_armor.grenade.he_grenade", "Explodes 3 s after the throw and destroys blocks like TNT.");
            add("tacz_camo_armor.configuration.grenades", "Grenades");
            add("tacz_camo_armor.configuration.fragRadius", "Frag grenade power");
            add("tacz_camo_armor.configuration.heRadius", "HE grenade power");
            add("tacz_camo_armor.configuration.flashRadius", "Flashbang range");
            add("tacz_camo_armor.configuration.flashSeconds", "Flashbang blindness (seconds)");
            add("tacz_camo_armor.configuration.smokeSeconds", "Smoke duration (seconds)");
            add("tacz_camo_armor.configuration.smokeRadius", "Smoke cloud radius");
            add("tacz_camo_armor.configuration.title", "Tactical Camo Armor Settings");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.client.toml", "Client Settings");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.client.toml.title", "Client Settings");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.server.toml", "Server Settings");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.server.toml.title", "Server Settings");
            add("tacz_camo_armor.configuration.visuals", "Visuals");
            add("tacz_camo_armor.configuration.throwFovEffect", "Widen view while winding up a throw");
            add("tacz_camo_armor.configuration.flashMaxOpacity", "Strongest flashbang white-out");
            add("tacz_camo_armor.configuration.nvgVignette", "Night vision tube vignette");
            add("entity.tacz_camo_armor.grenade", "Grenade");
            add("jei.tacz_camo_armor.grenade_throw", "Hold right-click to wind up and release to throw: a tap lobs it at your feet, a full second gives the longest throw. Grenades bounce off walls and floors and go off on a timer.");
            add(ModItems.grenade(GrenadeKind.MOLOTOV).get(), "Molotov Cocktail");
            add("tooltip.tacz_camo_armor.grenade.molotov", "Shatters on landing and spreads fire");
            add("jei.tacz_camo_armor.grenade.molotov", "Shatters on the first floor it lands on and covers it in fire for %s s; it bounces off walls. Anyone in the flames burns. Smoke puts the fire out and it never spreads into water. If it is still in the air after 2 s, it bursts without fire.");
            add(ModItems.grenade(GrenadeKind.INCENDIARY).get(), "Incendiary Grenade");
            add("tooltip.tacz_camo_armor.grenade.incendiary_grenade", "Covers the floor in fire on landing");
            add("jei.tacz_camo_armor.grenade.incendiary_grenade", "Works like the molotov: on the first floor it lands on, it covers the area in fire for %s s. Smoke puts the fire out and it never spreads into water. If it is still in the air after 2 s, it bursts without fire.");
            add(ModItems.grenade(GrenadeKind.DECOY).get(), "Decoy Grenade");
            add("tooltip.tacz_camo_armor.grenade.decoy_grenade", "Fakes gunfire to lure monsters");
            add("jei.tacz_camo_armor.grenade.decoy_grenade", "Two seconds after the throw it starts firing fake shots in bursts for %s s, then pops. Monsters without a target walk over to it; sculk sensors and wardens hear it too.");
            add("jei.tacz_camo_armor.dispenser", "Dispensers throw grenades too.");
            add("tacz_camo_armor.configuration.fireRadius", "Fire spread radius");
            add("tacz_camo_armor.configuration.fireSeconds", "Fire duration (seconds)");
            add("tacz_camo_armor.configuration.fireDamage", "Fire damage per hit");
            add("tacz_camo_armor.configuration.decoySeconds", "Decoy duration (seconds)");
            add("tacz_camo_armor.configuration.decoyLureRadius", "Decoy lure radius");
            add("tacz_camo_armor.configuration.repair", "Repair");
            add("tacz_camo_armor.configuration.repairKitFraction", "Durability restored per repair kit");
            add(ModItems.ARMOR_REPAIR_KIT.get(), "Armor Repair Kit");
            add("tooltip.tacz_camo_armor.repair_kit", "Restores %s%% durability of tactical armor in an anvil");
            add("jei.tacz_camo_armor.repair_kit", "Put a damaged armor piece and repair kits into an anvil. Each kit restores %s%% durability and costs one level, whatever the tier.");
            add("message.tacz_camo_armor.nvg_low", "NVG battery is running low");
            add("message.tacz_camo_armor.filter_low", "Gas mask filter is running out");
            add("message.tacz_camo_armor.filter_empty", "Gas mask filter is used up");
            add("death.attack.tacz_camo_armor.incendiary", "%1$s was burned alive by %2$s");
            add("death.attack.tacz_camo_armor.incendiary.item", "%1$s was burned alive by %2$s using %3$s");
            add("subtitles.tacz_camo_armor.grenade.decoy_shot", "Decoy fires");
            add("subtitles.tacz_camo_armor.grenade.pin", "Grenade pin pulled");
            advancement("root", "Tactical Camo Armor", "Weave your first camouflage fabric");
            advancement("suit_up", "Suit Up", "Wear four heavy armor pieces at once");
            advancement("ghillie", "Part of the Scenery", "Wear a full ghillie suit");
            advancement("helmet_save", "Not Today", "Survive a shot to the head thanks to your helmet");
            advancement("flash_mob", "Look Away!", "Blind a mob with a flashbang");
            advancement("extinguish", "Fire Department", "Put out a molotov or incendiary fire with smoke");
            advancement("all_grenades", "Full Utility", "Throw every kind of grenade");
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
            tooltip("camo_biome.ghillie_snow", "Strongest camo in snowy biomes, weak elsewhere");
            tooltip("camo_biome.ghillie_desert", "Strongest camo in deserts and badlands, weak in forests");
            tooltip("camo_biome.us_ucp", "Best in mountains, on stony shores and underground");
            tooltip("camo_biome.us_tiger", "Best in jungles, swamps and lush caves");
            tooltip("camo_biome.ru_flora", "Best on plains, in savannas and forests");
        }

        @Override
        protected void addConfig() {
            add(configKey("protection"), "Bullet protection");
            add(configKey("lightReduction"), "Light tier damage reduction");
            add(configKey("mediumReduction"), "Medium tier damage reduction");
            add(configKey("heavyReduction"), "Heavy tier damage reduction");
            add(configKey("durabilityPerDamage"), "Durability loss per damage point");
            add(configKey("hitZones"), "Hit zones");
            add(configKey("headZoneBottom"), "Head zone bottom (fraction of height)");
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
            return switch (set) {
                case RU_EMR -> "Ратник";
                case RU_FLORA -> "Флора";
                default -> super.setName(set);
            };
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
                case US_UCP -> "Цифровая ткань UCP";
                case US_TIGER -> "Ткань «Тигровая полоса»";
                case RU_EMR -> "Цифровая ткань ЕМР";
                case RU_FLORA -> "Ткань «Флора»";
                case GHILLIE -> "Маскировочная ткань гилли";
                case GHILLIE_SNOW -> "Снежная маскировочная ткань гилли";
                case GHILLIE_DESERT -> "Пустынная маскировочная ткань гилли";
                case SWAT -> "Чёрная ткань";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            if (set.form() == ArmorForm.GHILLIE) {
                String suffix = switch (set) {
                    case GHILLIE_SNOW -> " снежного гилли";
                    case GHILLIE_DESERT -> " пустынного гилли";
                    default -> " гилли";
                };
                return switch (piece) {
                    case HELMET -> "Капюшон";
                    case PLATE_CARRIER -> "Куртка";
                    case LEGGINGS -> "Штаны";
                    case BOOTS -> "Ботинки";
                } + suffix;
            }
            boolean feminine = (piece == ArmorPiece.PLATE_CARRIER && set.form() != ArmorForm.RU)
                    || (piece == ArmorPiece.HELMET && set == ArmorSet.T);
            boolean plural = piece == ArmorPiece.LEGGINGS || piece == ArmorPiece.BOOTS;
            String tierName = switch (tier) {
                case LIGHT -> plural ? "Лёгкие" : feminine ? "Лёгкая" : "Лёгкий";
                case MEDIUM -> plural ? "Средние" : feminine ? "Средняя" : "Средний";
                case HEAVY -> plural ? "Тяжёлые" : feminine ? "Тяжёлая" : "Тяжёлый";
            };
            String pieceName = switch (piece) {
                case HELMET -> set == ArmorSet.T ? "балаклава" : "шлем";
                case PLATE_CARRIER -> set.form() == ArmorForm.RU ? "бронежилет" : "плитоноска";
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
        protected void addDevices() {
            add(ModItems.BATTERY.get(), "Батарейка");
            add(ModItems.GAS_FILTER.get(), "Фильтр противогаза");
            add(ModItems.device(DeviceKind.PVS14).get(), "Монокуляр ночного видения PVS-14");
            add(ModItems.device(DeviceKind.GPNVG18).get(), "Панорамный ПНВ GPNVG-18 (белый фосфор)");
            add(ModItems.device(DeviceKind.GPNVG18_GREEN).get(), "Панорамный ПНВ GPNVG-18 (зелёный фосфор)");
            add("jei.tacz_camo_armor.gpnvg18_green", "Четыре трубки с зелёным люминофором и широким панорамным обзором.");
            add(ModItems.device(DeviceKind.THERMAL).get(), "Тепловизионный монокуляр");
            add("jei.tacz_camo_armor.thermal", "Тепловизор в режиме «белое — горячее»: живые существа в прямой видимости подсвечиваются, даже в камуфляже.");
            add(ModItems.device(DeviceKind.GAS_MASK).get(), "Противогаз");
            add("key.tacz_camo_armor.nvg", "Опустить/поднять ПНВ");
            add("key.categories.tacz_camo_armor", "Тактическая камуфляжная броня");
            add("message.tacz_camo_armor.nvg_empty", "Батарея ПНВ разряжена");
            add("tooltip.tacz_camo_armor.device.battery", "Заряд: %s%%");
            add("tooltip.tacz_camo_armor.device.filter", "Фильтр: %s%%");
            add("tooltip.tacz_camo_armor.device.unlimited", "Заряд: бесконечный");
            add("tooltip.tacz_camo_armor.device.attach_hint", "Объедините со шлемом в верстаке, чтобы установить");
            add("tooltip.tacz_camo_armor.device.nvg_helmets", "Подходит к шлемам US, Ратник, SWAT и CT");
            add("tooltip.tacz_camo_armor.device.mask_effect", "Защищает от яда, иссушения и драконьего дыхания");
            add("tooltip.tacz_camo_armor.helmet.mounted", "Установлено: %s");
            add("tooltip.tacz_camo_armor.helmet.nvg_key", "Нажмите %s, чтобы опустить или поднять ПНВ");
            add("tooltip.tacz_camo_armor.helmet.detach_hint", "Положите шлем в верстак отдельно, чтобы снять насадку");
            add("jei.tacz_camo_armor.protection", "Защита от пуль в зоне попадания: лёгкий %s%%, средний %s%%, тяжёлый %s%%. Бронебойные патроны её снижают.");
            add("jei.tacz_camo_armor.protection_single", "Защита от пуль в зоне попадания: %s%%. Бронебойные патроны её снижают.");
            add("jei.tacz_camo_armor.heavy", "Каждая тяжёлая деталь замедляет на %s%%.");
            add("jei.tacz_camo_armor.zones", "Зоны: шлем — хедшоты, нагрудная часть — торс, поножи — ноги, ботинки — ступни. Защищает от пуль TaCZ, стрел и других снарядов, в том числе от пуль оружейных модов, если они считаются снарядами.");
            add("jei.tacz_camo_armor.camo", "Полный комплект одной расцветки уменьшает дальность обнаружения мобами на %s%% (%s%% крадучись).");
            add("jei.tacz_camo_armor.no_camo", "У этого комплекта нет камуфляжа.");
            add("jei.tacz_camo_armor.nvg_fits", "На шлем ставятся ПНВ и противогаз.");
            add("jei.tacz_camo_armor.mask_fits", "На шлем ставится противогаз.");
            add("jei.tacz_camo_armor.nvg", "Устанавливается на шлем US, Ратник, SWAT или CT в верстаке. Нажмите %s, чтобы опустить и включить ночное видение. Заряда хватает на %s секунд, заряжается батарейкой в верстаке.");
            add("jei.tacz_camo_armor.pvs14", "Одна трубка с зелёным люминофором и узким полем зрения.");
            add("jei.tacz_camo_armor.gpnvg18", "Четыре трубки с белым люминофором и широким панорамным обзором.");
            add("jei.tacz_camo_armor.gas_mask", "Устанавливается на любой шлем в верстаке. Защищает от яда, иссушения и драконьего дыхания. Фильтра хватает на %s секунд заблокированных эффектов, меняется новым фильтром.");
            add("jei.tacz_camo_armor.detach", "Чтобы снять насадку, положите шлем в верстак отдельно.");
            add("jei.tacz_camo_armor.battery", "Заряжает ПНВ, отдельный или установленный на шлем.");
            add("jei.tacz_camo_armor.gas_filter", "Меняет фильтр противогаза, отдельного или установленного на шлем.");
            add("tacz_camo_armor.configuration.devices", "Приборы");
            add("tacz_camo_armor.configuration.nvgUsesBattery", "ПНВ расходует заряд");
            add("tacz_camo_armor.configuration.nvgBatterySeconds", "Время работы ПНВ (секунды)");
            add("tacz_camo_armor.configuration.gasFilterSeconds", "Ёмкость фильтра противогаза (секунды)");
            add(ModItems.grenade(GrenadeKind.FLASH).get(), "Светошумовая граната");
            add("tooltip.tacz_camo_armor.grenade.flashbang", "Ослепляет всех, кто на неё смотрит; стены защищают");
            add("jei.tacz_camo_armor.grenade.flashbang", "Срабатывает через 1,5 с после броска. Вспышка сильнее вблизи и если смотреть прямо на неё; за стеной безопасно. Игрок видит белый экран, мобы теряют цель и замедляются.");
            add(ModItems.grenade(GrenadeKind.SMOKE).get(), "Дымовая граната");
            add("tooltip.tacz_camo_armor.grenade.smoke_grenade", "Густой дым, сквозь который мобы не видят");
            add("jei.tacz_camo_armor.grenade.smoke_grenade", "Начинает дымить через 2 с после броска. Мобы не видят никого внутри облака и сквозь него. Тепловизор видит живых и в дыму.");
            add(ModItems.grenade(GrenadeKind.FRAG).get(), "Осколочная граната");
            add("tooltip.tacz_camo_armor.grenade.frag_grenade", "Взрывается через 3 с, блоки не ломает");
            add("jei.tacz_camo_armor.grenade.frag_grenade", "Взрывается через 3 с после броска. Ранит всех рядом, но блоки не разрушает.");
            add(ModItems.grenade(GrenadeKind.HE).get(), "Фугасная граната");
            add("tooltip.tacz_camo_armor.grenade.he_grenade", "Взрывается через 3 с и ломает блоки");
            add("jei.tacz_camo_armor.grenade.he_grenade", "Взрывается через 3 с после броска и разрушает блоки как динамит.");
            add("tacz_camo_armor.configuration.grenades", "Гранаты");
            add("tacz_camo_armor.configuration.fragRadius", "Мощность осколочной гранаты");
            add("tacz_camo_armor.configuration.heRadius", "Мощность фугасной гранаты");
            add("tacz_camo_armor.configuration.flashRadius", "Радиус светошумовой");
            add("tacz_camo_armor.configuration.flashSeconds", "Ослепление светошумовой (секунды)");
            add("tacz_camo_armor.configuration.smokeSeconds", "Длительность дыма (секунды)");
            add("tacz_camo_armor.configuration.smokeRadius", "Радиус облака дыма");
            add("tacz_camo_armor.configuration.title", "Настройки Tactical Camo Armor");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.client.toml", "Настройки клиента");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.client.toml.title", "Настройки клиента");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.server.toml", "Настройки сервера");
            add("tacz_camo_armor.configuration.section.tacz.camo.armor.server.toml.title", "Настройки сервера");
            add("tacz_camo_armor.configuration.visuals", "Изображение");
            add("tacz_camo_armor.configuration.throwFovEffect", "Расширять обзор при замахе гранатой");
            add("tacz_camo_armor.configuration.flashMaxOpacity", "Максимальная засветка от светошумовой");
            add("tacz_camo_armor.configuration.nvgVignette", "Затемнение вокруг окуляров ПНВ");
            add("entity.tacz_camo_armor.grenade", "Граната");
            add("jei.tacz_camo_armor.grenade_throw", "Зажмите правую кнопку мыши для замаха и отпустите для броска: короткое нажатие — бросок под ноги, секунда замаха — самый дальний бросок. Гранаты отскакивают от стен и пола и срабатывают по таймеру.");
            add(ModItems.grenade(GrenadeKind.MOLOTOV).get(), "Коктейль Молотова");
            add("tooltip.tacz_camo_armor.grenade.molotov", "Разбивается при падении и разливает огонь");
            add("jei.tacz_camo_armor.grenade.molotov", "Разбивается о первый пол, на который упадёт, и заливает его огнём на %s с; от стен отскакивает. Все, кто стоит в пламени, горят. Дым тушит огонь, в воду пламя не растекается. Если через 2 с бутылка ещё в воздухе, она разрывается без огня.");
            add(ModItems.grenade(GrenadeKind.INCENDIARY).get(), "Зажигательная граната");
            add("tooltip.tacz_camo_armor.grenade.incendiary_grenade", "Заливает пол огнём при падении");
            add("jei.tacz_camo_armor.grenade.incendiary_grenade", "Работает как коктейль Молотова: на первом полу, куда упадёт, заливает всё огнём на %s с. Дым тушит огонь, в воду пламя не растекается. Если через 2 с граната ещё в воздухе, она разрывается без огня.");
            add(ModItems.grenade(GrenadeKind.DECOY).get(), "Ложная граната");
            add("tooltip.tacz_camo_armor.grenade.decoy_grenade", "Имитирует стрельбу и приманивает монстров");
            add("jei.tacz_camo_armor.grenade.decoy_grenade", "Через 2 с после броска начинает очередями имитировать выстрелы в течение %s с, затем хлопает. Монстры без цели идут к ней; скалк-сенсоры и хранители тоже её слышат.");
            add("jei.tacz_camo_armor.dispenser", "Раздатчик тоже бросает гранаты.");
            add("tacz_camo_armor.configuration.fireRadius", "Радиус огня");
            add("tacz_camo_armor.configuration.fireSeconds", "Длительность огня (секунды)");
            add("tacz_camo_armor.configuration.fireDamage", "Урон огня за удар");
            add("tacz_camo_armor.configuration.decoySeconds", "Длительность ложной гранаты (секунды)");
            add("tacz_camo_armor.configuration.decoyLureRadius", "Радиус приманки ложной гранаты");
            add("tacz_camo_armor.configuration.repair", "Ремонт");
            add("tacz_camo_armor.configuration.repairKitFraction", "Прочность за один ремкомплект");
            add(ModItems.ARMOR_REPAIR_KIT.get(), "Ремкомплект брони");
            add("tooltip.tacz_camo_armor.repair_kit", "Восстанавливает %s%% прочности тактической брони в наковальне");
            add("jei.tacz_camo_armor.repair_kit", "Положите повреждённую деталь брони и ремкомплекты в наковальню. Каждый комплект восстанавливает %s%% прочности и стоит один уровень, для любого тира.");
            add("message.tacz_camo_armor.nvg_low", "Батарея ПНВ садится");
            add("message.tacz_camo_armor.filter_low", "Фильтр противогаза почти израсходован");
            add("message.tacz_camo_armor.filter_empty", "Фильтр противогаза израсходован");
            add("death.attack.tacz_camo_armor.incendiary", "%1$s сгорел(а) в пламени (%2$s)");
            add("death.attack.tacz_camo_armor.incendiary.item", "%1$s сгорел(а) в пламени (%2$s, %3$s)");
            add("subtitles.tacz_camo_armor.grenade.decoy_shot", "Ложная граната стреляет");
            add("subtitles.tacz_camo_armor.grenade.pin", "Выдернута чека");
            advancement("root", "Тактическая камуфляжная броня", "Сплетите первую камуфляжную ткань");
            advancement("suit_up", "Полная выкладка", "Наденьте сразу четыре тяжёлые детали брони");
            advancement("ghillie", "Часть пейзажа", "Наденьте полный костюм гилли");
            advancement("helmet_save", "Не сегодня", "Переживите выстрел в голову благодаря шлему");
            advancement("flash_mob", "Отвернись!", "Ослепите моба светошумовой гранатой");
            advancement("extinguish", "Пожарная команда", "Потушите огонь молотова или зажигательной гранаты дымом");
            advancement("all_grenades", "Полный набор", "Бросьте гранату каждого вида");
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
            tooltip("camo_biome.ghillie_snow", "Самый сильный камуфляж в снегу, слабый в остальных биомах");
            tooltip("camo_biome.ghillie_desert", "Самый сильный камуфляж в пустынях и бэдлендах, слабый в лесах");
            tooltip("camo_biome.us_ucp", "Лучше всего в горах, на каменистых берегах и под землёй");
            tooltip("camo_biome.us_tiger", "Лучше всего в джунглях, на болотах и в пышных пещерах");
            tooltip("camo_biome.ru_flora", "Лучше всего на равнинах, в саваннах и лесах");
        }

        @Override
        protected void addConfig() {
            add(configKey("protection"), "Защита от пуль");
            add(configKey("lightReduction"), "Снижение урона: лёгкий тир");
            add(configKey("mediumReduction"), "Снижение урона: средний тир");
            add(configKey("heavyReduction"), "Снижение урона: тяжёлый тир");
            add(configKey("durabilityPerDamage"), "Потеря прочности за единицу урона");
            add(configKey("hitZones"), "Зоны попадания");
            add(configKey("headZoneBottom"), "Низ зоны головы (доля роста)");
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
