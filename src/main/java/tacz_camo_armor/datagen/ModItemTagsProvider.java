package tacz_camo_armor.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tacz_camo_armor.TaczCamoArmor;
import tacz_camo_armor.item.ArmorPiece;
import tacz_camo_armor.item.ArmorSet;
import tacz_camo_armor.item.TacticalArmorItem;
import tacz_camo_armor.registry.ModItems;
import tacz_camo_armor.registry.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper fileHelper) {
        super(output, lookup, blockTags, TaczCamoArmor.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ArmorSet set : ArmorSet.values()) {
            tag(ModTags.FABRICS).add(ModItems.fabric(set).get());
        }
        for (var holder : ModItems.allArmor()) {
            TacticalArmorItem item = holder.get();
            tag(ModTags.TACTICAL_ARMOR).add(item);
            tag(slotTag(item.piece())).add(item);
        }
    }

    private static TagKey<Item> slotTag(ArmorPiece piece) {
        return switch (piece) {
            case HELMET -> ItemTags.HEAD_ARMOR;
            case PLATE_CARRIER -> ItemTags.CHEST_ARMOR;
            case LEGGINGS -> ItemTags.LEG_ARMOR;
            case BOOTS -> ItemTags.FOOT_ARMOR;
        };
    }
}
