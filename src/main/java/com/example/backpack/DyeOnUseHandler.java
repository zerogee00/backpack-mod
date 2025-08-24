package com.example.backpack;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class DyeOnUseHandler {
    public DyeOnUseHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
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

    private void dyeBackpack(ItemStack backpack, DyeItem dye) {
        int color = dye.getDyeColor().getFireworkColor();
        ((BackpackItem) backpack.getItem()).setColor(backpack, color);
    }
}
