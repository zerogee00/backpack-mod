package com.example.backpack;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class DyeOnUseHandler {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        if (main.getItem() instanceof BackpackItem && off.getItem() instanceof DyeItem) {
            dyeBackpack(main, (DyeItem) off.getItem());
            off.shrink(1);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        } else if (off.getItem() instanceof BackpackItem && main.getItem() instanceof DyeItem) {
            dyeBackpack(off, (DyeItem) main.getItem());
            main.shrink(1);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static void dyeBackpack(ItemStack backpack, DyeItem dye) {
        // Basic dyeing implementation - this will be enhanced when we implement proper color persistence
        System.out.println("Dyeing backpack with " + dye.getDyeColor().getName() + " dye");
        // TODO: Implement actual color setting when we resolve the NBT API issues
    }
}
