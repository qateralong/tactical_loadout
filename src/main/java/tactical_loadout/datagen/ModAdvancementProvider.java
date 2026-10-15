package tactical_loadout.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tactical_loadout.TacticalLoadout;
import tactical_loadout.advancement.ModAdvancements;
import tactical_loadout.grenade.GrenadeKind;
import tactical_loadout.item.ArmorPiece;
import tactical_loadout.item.ArmorSet;
import tactical_loadout.item.ArmorTier;
import tactical_loadout.registry.ModItems;
import tactical_loadout.registry.ModTags;

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementProvider.AdvancementGenerator {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/block/moss_block.png");

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper fileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.fabric(ArmorSet.US_WOODLAND).get(), title(ModAdvancements.ROOT), description(ModAdvancements.ROOT),
                        BACKGROUND, AdvancementType.TASK, false, false, false)
                .addCriterion("has_fabric", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.FABRICS)))
                .save(saver, id(ModAdvancements.ROOT), fileHelper);

        AdvancementHolder suitUp = simple(saver, fileHelper, root, ModAdvancements.SUIT_UP,
                ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.HEAVY, ArmorPiece.PLATE_CARRIER).get(), AdvancementType.GOAL);
        simple(saver, fileHelper, suitUp, ModAdvancements.HELMET_SAVE,
                ModItems.armor(ArmorSet.US_WOODLAND, ArmorTier.HEAVY, ArmorPiece.HELMET).get(), AdvancementType.GOAL);
        simple(saver, fileHelper, root, ModAdvancements.GHILLIE,
                ModItems.armor(ArmorSet.GHILLIE, ArmorTier.LIGHT, ArmorPiece.HELMET).get(), AdvancementType.TASK);
        AdvancementHolder flash = simple(saver, fileHelper, root, ModAdvancements.FLASH_MOB,
                ModItems.grenade(GrenadeKind.FLASH).get(), AdvancementType.TASK);
        simple(saver, fileHelper, flash, ModAdvancements.EXTINGUISH, ModItems.grenade(GrenadeKind.SMOKE).get(), AdvancementType.GOAL);

        Advancement.Builder allGrenades = Advancement.Builder.advancement()
                .parent(flash)
                .display(ModItems.grenade(GrenadeKind.MOLOTOV).get(), title(ModAdvancements.ALL_GRENADES),
                        description(ModAdvancements.ALL_GRENADES), null, AdvancementType.CHALLENGE, true, true, false)
                .requirements(AdvancementRequirements.Strategy.AND);
        for (GrenadeKind kind : GrenadeKind.values()) {
            allGrenades.addCriterion(kind.id(), impossible());
        }
        allGrenades.save(saver, id(ModAdvancements.ALL_GRENADES), fileHelper);
    }

    private static AdvancementHolder simple(Consumer<AdvancementHolder> saver, ExistingFileHelper fileHelper, AdvancementHolder parent,
                                            String name, ItemLike icon, AdvancementType type) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(new ItemStack(icon), title(name), description(name), null, type, true, true, false)
                .addCriterion(ModAdvancements.DONE, impossible())
                .save(saver, id(name), fileHelper);
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }

    private static ResourceLocation id(String name) {
        return TacticalLoadout.id("main/" + name);
    }

    private static Component title(String name) {
        return Component.translatable("advancement." + TacticalLoadout.MOD_ID + "." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancement." + TacticalLoadout.MOD_ID + "." + name + ".description");
    }
}
