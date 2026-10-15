package tactical_loadout.fabric.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tactical_loadout.item.RepairKitItem;

/** Stands in for the NeoForge anvil event: repair kits fix tactical armor. */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;
    @Shadow
    private int repairItemCountCost;
    @Shadow
    @Nullable
    private String itemName;

    private AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(type, containerId, inventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void tactical_loadout$repairKit(CallbackInfo ci) {
        RepairKitItem.AnvilResult result = RepairKitItem.repair(inputSlots.getItem(0), inputSlots.getItem(1), itemName);
        if (result != null) {
            resultSlots.setItem(0, result.output());
            cost.set(result.cost());
            repairItemCountCost = result.kitsUsed();
            broadcastChanges();
            ci.cancel();
        }
    }
}
