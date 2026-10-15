package tactical_loadout.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.device.DeviceKind;
import tactical_loadout.grenade.GrenadeKind;
import tactical_loadout.item.ArmorForm;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorSet;
import tactical_loadout.item.ArmorTier;
import tactical_loadout.registry.ModItems;

public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, TacticalLoadout.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TacticalLoadout.MOD_ID, modName());
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
            case EOD -> "EOD";
        };
    }

    protected void tooltip(String name, String value) {
        add("tooltip." + TacticalLoadout.MOD_ID + "." + name, value);
    }

    protected void advancement(String name, String title, String description) {
        add("advancement." + TacticalLoadout.MOD_ID + "." + name + ".title", title);
        add("advancement." + TacticalLoadout.MOD_ID + "." + name + ".description", description);
    }

    protected static String configKey(String name) {
        return TacticalLoadout.MOD_ID + ".configuration." + name;
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
            return "Tactical Loadout";
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
                case EOD -> "Padded Fabric";
            };
        }

        @Override
        protected String armorName(ArmorSet set, ArmorTier tier, ArmorPiece piece) {
            String tierName = switch (tier) {
                case LIGHT -> "Light";
                case MEDIUM -> "Medium";
                case HEAVY -> "Heavy";
            };
            if (set == ArmorSet.EOD) {
                return switch (piece) {
                    case HELMET -> "EOD Helmet";
                    case PLATE_CARRIER -> "EOD Suit Jacket";
                    case LEGGINGS -> "EOD Suit Trousers";
                    case BOOTS -> "EOD Boots";
                };
            }
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
            add("jei.tactical_loadout.gpnvg18_green", "Four green-phosphor tubes with a wide panoramic view.");
            add(ModItems.device(DeviceKind.THERMAL).get(), "Thermal Imaging Monocular");
            add(ModItems.device(DeviceKind.THERMAL_PANO).get(), "Panoramic Thermal Imager");
            add("jei.tactical_loadout.thermal_pano", "Four-tube thermal goggles with a wide panoramic view. Made from two thermal monoculars.");
            add("jei.tactical_loadout.thermal", "White-hot thermal imager: living creatures in your line of sight glow, even through camouflage.");
            add(ModItems.device(DeviceKind.GAS_MASK).get(), "Gas Mask");
            add("key.tactical_loadout.nvg", "Flip NVG");
            add("key.categories.tactical_loadout", "Tactical Loadout");
            add("message.tactical_loadout.nvg_empty", "NVG battery is empty");
            add("tooltip.tactical_loadout.device.battery", "Battery: %s%%");
            add("tooltip.tactical_loadout.device.filter", "Filter: %s%%");
            add("tooltip.tactical_loadout.device.unlimited", "Battery: unlimited");
            add("tooltip.tactical_loadout.device.attach_hint", "Combine with a helmet in a crafting grid to attach");
            add("tooltip.tactical_loadout.device.nvg_helmets", "Fits US, Ratnik, SWAT and CT helmets");
            add("tooltip.tactical_loadout.device.mask_effect", "Blocks poison, wither and dragon's breath");
            add("tooltip.tactical_loadout.helmet.mounted", "Mounted: %s");
            add("tooltip.tactical_loadout.helmet.nvg_key", "Press %s to flip the NVG");
            add("tooltip.tactical_loadout.helmet.detach_hint", "Put the helmet alone in a crafting grid to detach");
            add("jei.tactical_loadout.protection", "Bullet protection in the hit zone: light %s%%, medium %s%%, heavy %s%%. Armor-piercing ammo lowers it.");
            add("jei.tactical_loadout.protection_single", "Bullet protection in the hit zone: %s%%. Armor-piercing ammo lowers it.");
            add("jei.tactical_loadout.heavy", "Each heavy piece slows you down by %s%%.");
            add("jei.tactical_loadout.zones", "Zones: helmet covers headshots, chest piece the torso, leggings the legs, boots the feet. Works against TaCZ bullets, arrows and other projectiles, including bullets from gun mods that count as projectiles.");
            add("jei.tactical_loadout.camo", "A full set of one color shrinks mob detection range by %s%% (%s%% while sneaking).");
            add("jei.tactical_loadout.no_camo", "This set gives no camouflage.");
            add("jei.tactical_loadout.nvg_fits", "The helmet accepts a night vision device and a gas mask.");
            add("jei.tactical_loadout.mask_fits", "The helmet accepts a gas mask.");
            add("jei.tactical_loadout.nvg", "Attach to a US, Ratnik, SWAT or CT helmet in a crafting grid. Press %s to flip it down for night vision. A battery lasts %s seconds; recharge it with a battery in a crafting grid.");
            add("jei.tactical_loadout.pvs14", "Single green-phosphor tube with a narrow field of view.");
            add("jei.tactical_loadout.gpnvg18", "Four white-phosphor tubes with a wide panoramic view.");
            add("jei.tactical_loadout.gas_mask", "Attach to any helmet in a crafting grid. Blocks poison, wither and dragon's breath. A filter lasts %s seconds of blocked effects; refill it with a filter.");
            add("jei.tactical_loadout.detach", "To remove an attachment, put the helmet alone in a crafting grid.");
            add("jei.tactical_loadout.battery", "Recharges a night vision device, loose or mounted on a helmet.");
            add("jei.tactical_loadout.gas_filter", "Refills a gas mask, loose or mounted on a helmet.");
            add("tactical_loadout.configuration.devices", "Devices");
            add("tactical_loadout.configuration.nvgUsesBattery", "NVG uses battery");
            add("tactical_loadout.configuration.nvgBatterySeconds", "NVG battery life (seconds)");
            add("tactical_loadout.configuration.gasFilterSeconds", "Gas mask filter capacity (seconds)");
            add(ModItems.grenade(GrenadeKind.FLASH).get(), "Flashbang");
            add("tooltip.tactical_loadout.grenade.flashbang", "Blinds everyone who looks at it; walls protect");
            add("jei.tactical_loadout.grenade.flashbang", "Goes off 1.5 s after the throw. The flash is strongest up close and when you look straight at it; anything behind a wall is safe. Players see a white screen, mobs lose their target and slow down.");
            add(ModItems.grenade(GrenadeKind.SMOKE).get(), "Smoke Grenade");
            add("tooltip.tactical_loadout.grenade.smoke_grenade", "Thick smoke that mobs can't see through");
            add("jei.tactical_loadout.grenade.smoke_grenade", "Starts smoking 2 s after the throw. Mobs can't see anyone inside the cloud or through it. A thermal imager still sees living things in the smoke.");
            add(ModItems.grenade(GrenadeKind.FRAG).get(), "Frag Grenade");
            add("tooltip.tactical_loadout.grenade.frag_grenade", "Explodes after 3 s without breaking blocks");
            add("jei.tactical_loadout.grenade.frag_grenade", "Explodes 3 s after the throw. Hurts everything nearby but never breaks blocks.");
            add(ModItems.grenade(GrenadeKind.HE).get(), "HE Grenade");
            add("tooltip.tactical_loadout.grenade.he_grenade", "Explodes after 3 s and breaks blocks");
            add("jei.tactical_loadout.grenade.he_grenade", "Explodes 3 s after the throw and destroys blocks like TNT.");
            add(ModItems.grenade(GrenadeKind.IMPACT).get(), "Impact Grenade");
            add("tooltip.tactical_loadout.grenade.impact_grenade", "Explodes on contact and breaks blocks");
            add("jei.tactical_loadout.grenade.impact_grenade", "Arms after flying 3.5 blocks, then explodes the moment it touches a block or anyone and destroys blocks like TNT. Dropped at your feet it stays safe and goes off after 5 s.");
            add("tactical_loadout.configuration.grenades", "Grenades");
            add("tactical_loadout.configuration.fragRadius", "Frag grenade power");
            add("tactical_loadout.configuration.heRadius", "HE grenade power");
            add("tactical_loadout.configuration.impactRadius", "Impact grenade power");
            add("tactical_loadout.configuration.flashRadius", "Flashbang range");
            add("tactical_loadout.configuration.flashSeconds", "Flashbang blindness (seconds)");
            add("tactical_loadout.configuration.smokeSeconds", "Smoke duration (seconds)");
            add("tactical_loadout.configuration.smokeRadius", "Smoke cloud radius");
            add("tactical_loadout.configuration.title", "Tactical Loadout Settings");
            add("tactical_loadout.configuration.section.tactical.loadout.client.toml", "Client Settings");
            add("tactical_loadout.configuration.section.tactical.loadout.client.toml.title", "Client Settings");
            add("tactical_loadout.configuration.section.tactical.loadout.server.toml", "Server Settings");
            add("tactical_loadout.configuration.section.tactical.loadout.server.toml.title", "Server Settings");
            add("tactical_loadout.configuration.visuals", "Visuals");
            add("tactical_loadout.configuration.throwFovEffect", "Widen view while winding up a throw");
            add("tactical_loadout.configuration.flashMaxOpacity", "Strongest flashbang white-out");
            add("tactical_loadout.configuration.nvgVignette", "Night vision tube vignette");
            add("entity.tactical_loadout.grenade", "Grenade");
            add("jei.tactical_loadout.grenade_throw", "Hold right-click to wind up and release to throw: a tap lobs it at your feet, a full second gives the longest throw. Grenades bounce off walls and floors and go off on a timer.");
            add(ModItems.grenade(GrenadeKind.MOLOTOV).get(), "Molotov Cocktail");
            add("tooltip.tactical_loadout.grenade.molotov", "Shatters on landing and spreads fire");
            add("jei.tactical_loadout.grenade.molotov", "Shatters on the first floor it lands on and covers it in fire for %s s; it bounces off walls. Anyone in the flames burns. Smoke puts the fire out and it never spreads into water. If it is still in the air after 2 s, it bursts without fire.");
            add(ModItems.grenade(GrenadeKind.INCENDIARY).get(), "Incendiary Grenade");
            add("tooltip.tactical_loadout.grenade.incendiary_grenade", "Covers the floor in fire on landing");
            add("jei.tactical_loadout.grenade.incendiary_grenade", "Works like the molotov: on the first floor it lands on, it covers the area in fire for %s s. Smoke puts the fire out and it never spreads into water. If it is still in the air after 2 s, it bursts without fire.");
            add(ModItems.grenade(GrenadeKind.DECOY).get(), "Decoy Grenade");
            add("tooltip.tactical_loadout.grenade.decoy_grenade", "Fakes gunfire to lure monsters");
            add("jei.tactical_loadout.grenade.decoy_grenade", "Two seconds after the throw it starts firing fake shots in bursts for %s s, then pops. Monsters without a target walk over to it; sculk sensors and wardens hear it too.");
            add("jei.tactical_loadout.dispenser", "Dispensers throw grenades too.");
            add("tactical_loadout.configuration.fireRadius", "Fire spread radius");
            add("tactical_loadout.configuration.fireSeconds", "Fire duration (seconds)");
            add("tactical_loadout.configuration.fireDamage", "Fire damage per hit");
            add("tactical_loadout.configuration.decoySeconds", "Decoy duration (seconds)");
            add("tactical_loadout.configuration.decoyLureRadius", "Decoy lure radius");
            add("tactical_loadout.configuration.repair", "Repair");
            add("tactical_loadout.configuration.repairKitFraction", "Durability restored per repair kit");
            add(ModItems.ARMOR_REPAIR_KIT.get(), "Armor Repair Kit");
            add("tooltip.tactical_loadout.repair_kit", "Restores %s%% durability of tactical armor in an anvil");
            add("jei.tactical_loadout.repair_kit", "Put a damaged armor piece and repair kits into an anvil. Each kit restores %s%% durability and costs one level, whatever the tier.");
            add("message.tactical_loadout.nvg_low", "NVG battery is running low");
            add("message.tactical_loadout.filter_low", "Gas mask filter is running out");
            add("message.tactical_loadout.filter_empty", "Gas mask filter is used up");
            add("death.attack.tactical_loadout.incendiary", "%1$s was burned alive by %2$s");
            add("death.attack.tactical_loadout.incendiary.item", "%1$s was burned alive by %2$s using %3$s");
            add("subtitles.tactical_loadout.grenade.decoy_shot", "Decoy fires");
            add("subtitles.tactical_loadout.grenade.pin", "Grenade pin pulled");
            add(ModItems.MEDKIT.get(), "Medkit");
            add("tooltip.tactical_loadout.medkit", "Hold right-click for %s s: heals %s hearts and stops bleeding");
            add("jei.tactical_loadout.medkit", "Hold right-click for 4 s to heal %s hearts and stop bleeding. Bullets and explosions have a %s%% chance to start bleeding. You move slowly while patching up.");
            add("message.tactical_loadout.medkit.not_needed", "You are not hurt");
            add(ModItems.TOURNIQUET.get(), "Tourniquet");
            add("tooltip.tactical_loadout.tourniquet", "Hold right-click for %s s to stop bleeding");
            add("jei.tactical_loadout.tourniquet", "Hold right-click for 1.5 s to stop bleeding. Bullets and explosions have a %s%% chance to start bleeding, which costs half a heart every 3 s for up to %s s and ignores armor.");
            add("message.tactical_loadout.tourniquet.not_needed", "You are not bleeding");
            add("effect.tactical_loadout.bleeding", "Bleeding");
            add("death.attack.tactical_loadout.bleeding", "%1$s bled out");
            add("death.attack.tactical_loadout.bleeding.player", "%1$s bled out while fighting %2$s");
            add("tactical_loadout.configuration.medical", "Medical");
            add("tactical_loadout.configuration.bleedChance", "Chance to start bleeding");
            add("tactical_loadout.configuration.bleedSeconds", "Bleeding duration (seconds)");
            add("tactical_loadout.configuration.medkitHealth", "Health restored by a medkit");
            add("message.tactical_loadout.downed.down_self", "You are down! Wait for someone to pick you up");
            add("message.tactical_loadout.downed.self_revived", "You got back up on your own");
            add("message.tactical_loadout.downed.reviving", "Picking up %s: %s%%");
            add("message.tactical_loadout.downed.being_revived", "%s is picking you up: %s%%");
            add("message.tactical_loadout.downed.revived_other", "%s is back on their feet");
            add("message.tactical_loadout.downed.revived_self", "%s picked you up");
            add("hud.tactical_loadout.downed.title", "YOU ARE DOWN");
            add("hud.tactical_loadout.downed.bleeding", "Bleeding out in %s s");
            add("hud.tactical_loadout.downed.hint", "A teammate can pick you up by holding right-click on you");
            add("tactical_loadout.configuration.downed", "Downed state");
            add("tactical_loadout.configuration.downedEnabled", "Go down instead of dying");
            add("tactical_loadout.configuration.downedSeconds", "Time before bleeding out (seconds)");
            add("tactical_loadout.configuration.reviveSeconds", "Time to pick someone up (seconds)");
            add("tactical_loadout.configuration.reviveHealth", "Health after being picked up");
            add("tactical_loadout.configuration.selfReviveChance", "Chance to get up alone");
            add("tooltip.tactical_loadout.eod", "Full set: blocks %s%% of explosion damage");
            add("jei.tactical_loadout.eod", "Bomb suit. A full set blocks %s%% of explosion damage, but it is very heavy: it slows you down by %s%% on top of the heavy armor slowdown and makes jumps lower. The sealed helmet takes no night vision, thermal imager or gas mask.");
            add("tactical_loadout.configuration.eod", "Bomb suit");
            add("tactical_loadout.configuration.eodBlastReduction", "Explosion damage blocked by a full suit");
            add("tactical_loadout.configuration.eodSlowdown", "Speed lost in a full suit");
            add("tactical_loadout.configuration.eodJumpReduction", "Jump strength lost in a full suit");
            add(ModItems.ZIP_TIES.get(), "Zip Ties");
            add("tooltip.tactical_loadout.zip_ties", "Right-click a player %s times quickly to tie them up; they need %s key presses to break free");
            add("jei.tactical_loadout.zip_ties", "Right-click another player %1$s times in a row to tie them up. A tied player can't move, jump, attack, shoot or use anything. To break free they press the jump key %2$s times; anyone can also cut them loose with shears, %1$s clicks in a row. Respawning frees them too.");
            add("message.tactical_loadout.zip_ties.tying", "Tying: %s/%s");
            add("message.tactical_loadout.zip_ties.tied_other", "%s is tied up");
            add("message.tactical_loadout.zip_ties.tied_self", "You have been tied up");
            add("message.tactical_loadout.zip_ties.already_tied", "Already tied up");
            add("message.tactical_loadout.zip_ties.cutting", "Cutting: %s/%s");
            add("message.tactical_loadout.zip_ties.cut_other", "%s is free");
            add("hud.tactical_loadout.zip_ties.title", "YOU ARE TIED UP");
            add("hud.tactical_loadout.zip_ties.struggle", "Press %s to break free: %s");
            add(ModItems.TRIPWIRE_ALARM.get(), "Tripwire Alarm");
            add("jei.tactical_loadout.tripwire_alarm", "Place it on the floor across a passage: the wire runs across the direction you face. When anyone walks through it, the tin cans rattle loudly enough to be heard far away. It rearms by itself a moment later.");
            add("subtitles.tactical_loadout.tripwire_alarm.rattle", "Tin cans rattle");
            add("tactical_loadout.configuration.restraints", "Zip ties");
            add("tactical_loadout.configuration.zipTieStruggles", "Presses needed to break free");
            advancement("root", "Tactical Loadout", "Weave your first camouflage fabric");
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
            return "Тактическое снаряжение";
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
                case EOD -> "Стёганая ткань";
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
            if (set == ArmorSet.EOD) {
                return switch (piece) {
                    case HELMET -> "Шлем сапёра";
                    case PLATE_CARRIER -> "Куртка костюма сапёра";
                    case LEGGINGS -> "Штаны костюма сапёра";
                    case BOOTS -> "Ботинки сапёра";
                };
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
            add("jei.tactical_loadout.gpnvg18_green", "Четыре трубки с зелёным люминофором и широким панорамным обзором.");
            add(ModItems.device(DeviceKind.THERMAL).get(), "Тепловизионный монокуляр");
            add(ModItems.device(DeviceKind.THERMAL_PANO).get(), "Панорамный тепловизор");
            add("jei.tactical_loadout.thermal_pano", "Четырёхтрубный тепловизор с широким панорамным обзором. Собирается из двух тепловизионных монокуляров.");
            add("jei.tactical_loadout.thermal", "Тепловизор в режиме «белое — горячее»: живые существа в прямой видимости подсвечиваются, даже в камуфляже.");
            add(ModItems.device(DeviceKind.GAS_MASK).get(), "Противогаз");
            add("key.tactical_loadout.nvg", "Опустить/поднять ПНВ");
            add("key.categories.tactical_loadout", "Тактическое снаряжение");
            add("message.tactical_loadout.nvg_empty", "Батарея ПНВ разряжена");
            add("tooltip.tactical_loadout.device.battery", "Заряд: %s%%");
            add("tooltip.tactical_loadout.device.filter", "Фильтр: %s%%");
            add("tooltip.tactical_loadout.device.unlimited", "Заряд: бесконечный");
            add("tooltip.tactical_loadout.device.attach_hint", "Объедините со шлемом в верстаке, чтобы установить");
            add("tooltip.tactical_loadout.device.nvg_helmets", "Подходит к шлемам US, Ратник, SWAT и CT");
            add("tooltip.tactical_loadout.device.mask_effect", "Защищает от яда, иссушения и драконьего дыхания");
            add("tooltip.tactical_loadout.helmet.mounted", "Установлено: %s");
            add("tooltip.tactical_loadout.helmet.nvg_key", "Нажмите %s, чтобы опустить или поднять ПНВ");
            add("tooltip.tactical_loadout.helmet.detach_hint", "Положите шлем в верстак отдельно, чтобы снять насадку");
            add("jei.tactical_loadout.protection", "Защита от пуль в зоне попадания: лёгкий %s%%, средний %s%%, тяжёлый %s%%. Бронебойные патроны её снижают.");
            add("jei.tactical_loadout.protection_single", "Защита от пуль в зоне попадания: %s%%. Бронебойные патроны её снижают.");
            add("jei.tactical_loadout.heavy", "Каждая тяжёлая деталь замедляет на %s%%.");
            add("jei.tactical_loadout.zones", "Зоны: шлем — хедшоты, нагрудная часть — торс, поножи — ноги, ботинки — ступни. Защищает от пуль TaCZ, стрел и других снарядов, в том числе от пуль оружейных модов, если они считаются снарядами.");
            add("jei.tactical_loadout.camo", "Полный комплект одной расцветки уменьшает дальность обнаружения мобами на %s%% (%s%% крадучись).");
            add("jei.tactical_loadout.no_camo", "У этого комплекта нет камуфляжа.");
            add("jei.tactical_loadout.nvg_fits", "На шлем ставятся ПНВ и противогаз.");
            add("jei.tactical_loadout.mask_fits", "На шлем ставится противогаз.");
            add("jei.tactical_loadout.nvg", "Устанавливается на шлем US, Ратник, SWAT или CT в верстаке. Нажмите %s, чтобы опустить и включить ночное видение. Заряда хватает на %s секунд, заряжается батарейкой в верстаке.");
            add("jei.tactical_loadout.pvs14", "Одна трубка с зелёным люминофором и узким полем зрения.");
            add("jei.tactical_loadout.gpnvg18", "Четыре трубки с белым люминофором и широким панорамным обзором.");
            add("jei.tactical_loadout.gas_mask", "Устанавливается на любой шлем в верстаке. Защищает от яда, иссушения и драконьего дыхания. Фильтра хватает на %s секунд заблокированных эффектов, меняется новым фильтром.");
            add("jei.tactical_loadout.detach", "Чтобы снять насадку, положите шлем в верстак отдельно.");
            add("jei.tactical_loadout.battery", "Заряжает ПНВ, отдельный или установленный на шлем.");
            add("jei.tactical_loadout.gas_filter", "Меняет фильтр противогаза, отдельного или установленного на шлем.");
            add("tactical_loadout.configuration.devices", "Приборы");
            add("tactical_loadout.configuration.nvgUsesBattery", "ПНВ расходует заряд");
            add("tactical_loadout.configuration.nvgBatterySeconds", "Время работы ПНВ (секунды)");
            add("tactical_loadout.configuration.gasFilterSeconds", "Ёмкость фильтра противогаза (секунды)");
            add(ModItems.grenade(GrenadeKind.FLASH).get(), "Светошумовая граната");
            add("tooltip.tactical_loadout.grenade.flashbang", "Ослепляет всех, кто на неё смотрит; стены защищают");
            add("jei.tactical_loadout.grenade.flashbang", "Срабатывает через 1,5 с после броска. Вспышка сильнее вблизи и если смотреть прямо на неё; за стеной безопасно. Игрок видит белый экран, мобы теряют цель и замедляются.");
            add(ModItems.grenade(GrenadeKind.SMOKE).get(), "Дымовая граната");
            add("tooltip.tactical_loadout.grenade.smoke_grenade", "Густой дым, сквозь который мобы не видят");
            add("jei.tactical_loadout.grenade.smoke_grenade", "Начинает дымить через 2 с после броска. Мобы не видят никого внутри облака и сквозь него. Тепловизор видит живых и в дыму.");
            add(ModItems.grenade(GrenadeKind.FRAG).get(), "Осколочная граната");
            add("tooltip.tactical_loadout.grenade.frag_grenade", "Взрывается через 3 с, блоки не ломает");
            add("jei.tactical_loadout.grenade.frag_grenade", "Взрывается через 3 с после броска. Ранит всех рядом, но блоки не разрушает.");
            add(ModItems.grenade(GrenadeKind.HE).get(), "Фугасная граната");
            add("tooltip.tactical_loadout.grenade.he_grenade", "Взрывается через 3 с и ломает блоки");
            add("jei.tactical_loadout.grenade.he_grenade", "Взрывается через 3 с после броска и разрушает блоки как динамит.");
            add(ModItems.grenade(GrenadeKind.IMPACT).get(), "Ударная граната");
            add("tooltip.tactical_loadout.grenade.impact_grenade", "Взрывается от удара и ломает блоки");
            add("jei.tactical_loadout.grenade.impact_grenade", "Взводится, пролетев 3,5 блока, и взрывается в момент касания блока или любого существа, разрушая блоки как динамит. Брошенная под ноги не взводится и взрывается через 5 с.");
            add("tactical_loadout.configuration.grenades", "Гранаты");
            add("tactical_loadout.configuration.fragRadius", "Мощность осколочной гранаты");
            add("tactical_loadout.configuration.heRadius", "Мощность фугасной гранаты");
            add("tactical_loadout.configuration.impactRadius", "Мощность ударной гранаты");
            add("tactical_loadout.configuration.flashRadius", "Радиус светошумовой");
            add("tactical_loadout.configuration.flashSeconds", "Ослепление светошумовой (секунды)");
            add("tactical_loadout.configuration.smokeSeconds", "Длительность дыма (секунды)");
            add("tactical_loadout.configuration.smokeRadius", "Радиус облака дыма");
            add("tactical_loadout.configuration.title", "Настройки Tactical Loadout");
            add("tactical_loadout.configuration.section.tactical.loadout.client.toml", "Настройки клиента");
            add("tactical_loadout.configuration.section.tactical.loadout.client.toml.title", "Настройки клиента");
            add("tactical_loadout.configuration.section.tactical.loadout.server.toml", "Настройки сервера");
            add("tactical_loadout.configuration.section.tactical.loadout.server.toml.title", "Настройки сервера");
            add("tactical_loadout.configuration.visuals", "Изображение");
            add("tactical_loadout.configuration.throwFovEffect", "Расширять обзор при замахе гранатой");
            add("tactical_loadout.configuration.flashMaxOpacity", "Максимальная засветка от светошумовой");
            add("tactical_loadout.configuration.nvgVignette", "Затемнение вокруг окуляров ПНВ");
            add("entity.tactical_loadout.grenade", "Граната");
            add("jei.tactical_loadout.grenade_throw", "Зажмите правую кнопку мыши для замаха и отпустите для броска: короткое нажатие — бросок под ноги, секунда замаха — самый дальний бросок. Гранаты отскакивают от стен и пола и срабатывают по таймеру.");
            add(ModItems.grenade(GrenadeKind.MOLOTOV).get(), "Коктейль Молотова");
            add("tooltip.tactical_loadout.grenade.molotov", "Разбивается при падении и разливает огонь");
            add("jei.tactical_loadout.grenade.molotov", "Разбивается о первый пол, на который упадёт, и заливает его огнём на %s с; от стен отскакивает. Все, кто стоит в пламени, горят. Дым тушит огонь, в воду пламя не растекается. Если через 2 с бутылка ещё в воздухе, она разрывается без огня.");
            add(ModItems.grenade(GrenadeKind.INCENDIARY).get(), "Зажигательная граната");
            add("tooltip.tactical_loadout.grenade.incendiary_grenade", "Заливает пол огнём при падении");
            add("jei.tactical_loadout.grenade.incendiary_grenade", "Работает как коктейль Молотова: на первом полу, куда упадёт, заливает всё огнём на %s с. Дым тушит огонь, в воду пламя не растекается. Если через 2 с граната ещё в воздухе, она разрывается без огня.");
            add(ModItems.grenade(GrenadeKind.DECOY).get(), "Ложная граната");
            add("tooltip.tactical_loadout.grenade.decoy_grenade", "Имитирует стрельбу и приманивает монстров");
            add("jei.tactical_loadout.grenade.decoy_grenade", "Через 2 с после броска начинает очередями имитировать выстрелы в течение %s с, затем хлопает. Монстры без цели идут к ней; скалк-сенсоры и хранители тоже её слышат.");
            add("jei.tactical_loadout.dispenser", "Раздатчик тоже бросает гранаты.");
            add("tactical_loadout.configuration.fireRadius", "Радиус огня");
            add("tactical_loadout.configuration.fireSeconds", "Длительность огня (секунды)");
            add("tactical_loadout.configuration.fireDamage", "Урон огня за удар");
            add("tactical_loadout.configuration.decoySeconds", "Длительность ложной гранаты (секунды)");
            add("tactical_loadout.configuration.decoyLureRadius", "Радиус приманки ложной гранаты");
            add("tactical_loadout.configuration.repair", "Ремонт");
            add("tactical_loadout.configuration.repairKitFraction", "Прочность за один ремкомплект");
            add(ModItems.ARMOR_REPAIR_KIT.get(), "Ремкомплект брони");
            add("tooltip.tactical_loadout.repair_kit", "Восстанавливает %s%% прочности тактической брони в наковальне");
            add("jei.tactical_loadout.repair_kit", "Положите повреждённую деталь брони и ремкомплекты в наковальню. Каждый комплект восстанавливает %s%% прочности и стоит один уровень, для любого тира.");
            add("message.tactical_loadout.nvg_low", "Батарея ПНВ садится");
            add("message.tactical_loadout.filter_low", "Фильтр противогаза почти израсходован");
            add("message.tactical_loadout.filter_empty", "Фильтр противогаза израсходован");
            add("death.attack.tactical_loadout.incendiary", "%1$s сгорел(а) в пламени (%2$s)");
            add("death.attack.tactical_loadout.incendiary.item", "%1$s сгорел(а) в пламени (%2$s, %3$s)");
            add("subtitles.tactical_loadout.grenade.decoy_shot", "Ложная граната стреляет");
            add("subtitles.tactical_loadout.grenade.pin", "Выдернута чека");
            add(ModItems.MEDKIT.get(), "Аптечка");
            add("tooltip.tactical_loadout.medkit", "Удерживайте ПКМ %s с: лечит %s сердец и останавливает кровотечение");
            add("jei.tactical_loadout.medkit", "Удерживайте ПКМ 4 с, чтобы вылечить %s сердец и остановить кровотечение. Пули и взрывы с шансом %s%% вызывают кровотечение. Пока перевязываетесь, вы двигаетесь медленно.");
            add("message.tactical_loadout.medkit.not_needed", "Вы не ранены");
            add(ModItems.TOURNIQUET.get(), "Жгут");
            add("tooltip.tactical_loadout.tourniquet", "Удерживайте ПКМ %s с, чтобы остановить кровотечение");
            add("jei.tactical_loadout.tourniquet", "Удерживайте ПКМ 1,5 с, чтобы остановить кровотечение. Пули и взрывы с шансом %s%% вызывают кровотечение: оно отнимает полсердца каждые 3 с до %s с и не останавливается бронёй.");
            add("message.tactical_loadout.tourniquet.not_needed", "У вас нет кровотечения");
            add("effect.tactical_loadout.bleeding", "Кровотечение");
            add("death.attack.tactical_loadout.bleeding", "%1$s истёк(ла) кровью");
            add("death.attack.tactical_loadout.bleeding.player", "%1$s истёк(ла) кровью в бою с %2$s");
            add("tactical_loadout.configuration.medical", "Медицина");
            add("tactical_loadout.configuration.bleedChance", "Шанс кровотечения");
            add("tactical_loadout.configuration.bleedSeconds", "Длительность кровотечения (секунды)");
            add("tactical_loadout.configuration.medkitHealth", "Лечение аптечки");
            add("message.tactical_loadout.downed.down_self", "Вы ранены! Ждите, пока вас поднимут");
            add("message.tactical_loadout.downed.self_revived", "Вы поднялись сами");
            add("message.tactical_loadout.downed.reviving", "Поднимаю %s: %s%%");
            add("message.tactical_loadout.downed.being_revived", "%s поднимает вас: %s%%");
            add("message.tactical_loadout.downed.revived_other", "%s снова на ногах");
            add("message.tactical_loadout.downed.revived_self", "%s поднял(а) вас");
            add("hud.tactical_loadout.downed.title", "ВЫ РАНЕНЫ");
            add("hud.tactical_loadout.downed.bleeding", "Истечёте кровью через %s с");
            add("hud.tactical_loadout.downed.hint", "Товарищ может поднять вас, удерживая на вас ПКМ");
            add("tactical_loadout.configuration.downed", "Состояние «ранен»");
            add("tactical_loadout.configuration.downedEnabled", "Падать раненым вместо смерти");
            add("tactical_loadout.configuration.downedSeconds", "Время до смерти от кровопотери (секунды)");
            add("tactical_loadout.configuration.reviveSeconds", "Время, чтобы поднять (секунды)");
            add("tactical_loadout.configuration.reviveHealth", "Здоровье после подъёма");
            add("tactical_loadout.configuration.selfReviveChance", "Шанс подняться самому");
            add("tooltip.tactical_loadout.eod", "Полный комплект: поглощает %s%% урона от взрывов");
            add("jei.tactical_loadout.eod", "Костюм сапёра. Полный комплект поглощает %s%% урона от взрывов, но он очень тяжёлый: замедляет ещё на %s%% сверх тяжёлой брони и снижает прыжок. На герметичный шлем нельзя поставить ни ПНВ, ни тепловизор, ни противогаз.");
            add("tactical_loadout.configuration.eod", "Костюм сапёра");
            add("tactical_loadout.configuration.eodBlastReduction", "Защита полного костюма от взрывов");
            add("tactical_loadout.configuration.eodSlowdown", "Замедление полного костюма");
            add("tactical_loadout.configuration.eodJumpReduction", "Снижение прыжка в полном костюме");
            add(ModItems.ZIP_TIES.get(), "Стяжки");
            add("tooltip.tactical_loadout.zip_ties", "Быстро кликните ПКМ по игроку %s раза, чтобы связать; чтобы освободиться, ему нужно %s нажатий");
            add("jei.tactical_loadout.zip_ties", "Кликните ПКМ по другому игроку %1$s раза подряд, чтобы связать его. Связанный не может ходить, прыгать, бить, стрелять и пользоваться предметами. Чтобы освободиться, он жмёт клавишу прыжка %2$s раз; любой может разрезать стяжки ножницами, тоже %1$s клика подряд. Возрождение тоже освобождает.");
            add("message.tactical_loadout.zip_ties.tying", "Связываю: %s/%s");
            add("message.tactical_loadout.zip_ties.tied_other", "%s связан(а)");
            add("message.tactical_loadout.zip_ties.tied_self", "Вас связали");
            add("message.tactical_loadout.zip_ties.already_tied", "Уже связан(а)");
            add("message.tactical_loadout.zip_ties.cutting", "Разрезаю: %s/%s");
            add("message.tactical_loadout.zip_ties.cut_other", "%s свободен(на)");
            add("hud.tactical_loadout.zip_ties.title", "ВЫ СВЯЗАНЫ");
            add("hud.tactical_loadout.zip_ties.struggle", "Жмите %s, чтобы освободиться: %s");
            add(ModItems.TRIPWIRE_ALARM.get(), "Сигнальная растяжка");
            add("jei.tactical_loadout.tripwire_alarm", "Поставьте на пол поперёк прохода: проволока натягивается поперёк направления взгляда. Когда кто-то проходит через неё, консервные банки громко гремят, это слышно издалека. Через мгновение растяжка снова взводится.");
            add("subtitles.tactical_loadout.tripwire_alarm.rattle", "Гремят консервные банки");
            add("tactical_loadout.configuration.restraints", "Стяжки");
            add("tactical_loadout.configuration.zipTieStruggles", "Нажатий, чтобы освободиться");
            advancement("root", "Тактическое снаряжение", "Сплетите первую камуфляжную ткань");
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
