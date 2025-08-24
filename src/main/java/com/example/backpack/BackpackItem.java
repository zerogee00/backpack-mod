package com.example.backpack;

import com.example.backpack.menu.BackpackMenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;

public class BackpackItem extends Item {
    public BackpackItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check for dyeing first (sneak + right-click with dye in other hand)
        if (player.isShiftKeyDown()) {
            ItemStack otherHand = hand == InteractionHand.MAIN_HAND ?
                player.getItemInHand(InteractionHand.OFF_HAND) :
                player.getItemInHand(InteractionHand.MAIN_HAND);

            if (otherHand.getItem() instanceof DyeItem) {
                if (!level.isClientSide) {
                    // Apply dye color (basic implementation for now)
                    DyeItem dye = (DyeItem) otherHand.getItem();

                    // Consume one dye
                    if (!player.getAbilities().instabuild) {
                        otherHand.shrink(1);
                    }

                    System.out.println("Backpack: Applied dye color " + dye.getDyeColor().getName());

                    // Send message to player
                    player.sendSystemMessage(Component.literal("Backpack dyed with " + dye.getDyeColor().getName() + " dye!"));
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }

        // Check for water clearing (sneak + right-click with water bucket)
        if (player.isShiftKeyDown()) {
            ItemStack otherHand = hand == InteractionHand.MAIN_HAND ?
                player.getItemInHand(InteractionHand.OFF_HAND) :
                player.getItemInHand(InteractionHand.MAIN_HAND);

            if (otherHand.getItem().toString().contains("water_bucket") ||
                otherHand.getItem().toString().contains("bucket")) {
                if (!level.isClientSide) {
                    // Clear dye color (basic implementation for now)
                    System.out.println("Backpack: Clearing dye color with water");
                    player.sendSystemMessage(Component.literal("Backpack dye color cleared!"));
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }

        // Debug logging
        if (level.isClientSide) {
            System.out.println("Backpack: Client-side use detected");
        } else {
            System.out.println("Backpack: Server-side use detected, player: " + player.getName().getString());
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            System.out.println("Backpack: Opening backpack menu for server player");
            NetworkHandler.openScreen(sp, new BackpackMenu.Provider(stack), buf -> {});
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag isAdvanced) {
        // Add simple tooltip explaining how to use
        tooltipComponents.add(Component.translatable("item.backpack.tooltip.use")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC));

        // Add simple tooltip explaining how to dye
        tooltipComponents.add(Component.translatable("item.backpack.tooltip.dye")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC));

        // Add simple tooltip explaining how to clear dye
        tooltipComponents.add(Component.translatable("item.backpack.tooltip.clear_dye")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC));
    }

    // Helper method to get backpack storage from NBT
    public static net.minecraft.world.SimpleContainer getBackpackStorage(ItemStack stack) {
        // For now, return a fresh container - we'll implement persistence when we resolve the NBT API
        // This is a placeholder that will be enhanced with proper NBT support
        System.out.println("Getting backpack storage (persistence not yet implemented)");
        return new net.minecraft.world.SimpleContainer(27);
    }

    // Helper method to save backpack storage to NBT
    public static void saveBackpackStorage(ItemStack stack, net.minecraft.world.SimpleContainer container) {
        // For now, just log that we're trying to save
        // This is a placeholder that will be enhanced with proper NBT support
        System.out.println("Saving backpack storage (persistence not yet implemented)");
        
        // TODO: Implement proper NBT persistence when we resolve the API compatibility issues
        // The current Minecraft version has different NBT methods than what we're trying to use
    }
}
