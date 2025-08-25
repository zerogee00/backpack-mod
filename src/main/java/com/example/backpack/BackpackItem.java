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
                    // Apply dye color using the new data component system
                    DyeItem dye = (DyeItem) otherHand.getItem();

                    // Consume one dye
                    if (!player.getAbilities().instabuild) {
                        otherHand.shrink(1);
                    }

                    // Get the vanilla dye RGB color and store it in the data component
                    int rgb = dye.getDyeColor().getTextureDiffuseColor();
                    stack.set(ModDataComponents.BACKPACK_COLOR.get(), rgb);

                    // Set the dyed flag to true
                    stack.set(ModDataComponents.BACKPACK_DYED.get(), true);

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
                    // Clear dye color by removing the data component
                    if (stack.has(ModDataComponents.BACKPACK_COLOR.get())) {
                        stack.remove(ModDataComponents.BACKPACK_COLOR.get());

                        // Clear the dyed flag
                        stack.remove(ModDataComponents.BACKPACK_DYED.get());

                        player.sendSystemMessage(Component.literal("Backpack dye color cleared!"));
                    } else {
                        player.sendSystemMessage(Component.literal("Backpack is not dyed!"));
                    }
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
            NetworkHandler.openScreen(sp, new BackpackMenu.Provider(stack, hand), buf -> {});
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

        // Show current dye color if dyed
        if (stack.has(ModDataComponents.BACKPACK_COLOR.get())) {
            Integer color = stack.get(ModDataComponents.BACKPACK_COLOR.get());
            if (color != null) {
                // Convert RGB to a readable color name
                String colorName = getColorNameFromRGB(color);
                tooltipComponents.add(Component.literal("Dyed: " + colorName)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    // Helper method to get color name from RGB value
    private String getColorNameFromRGB(int rgb) {
        // This is a simple mapping - you could make this more sophisticated
        switch (rgb) {
            case 0xFFFFFF: return "White";
            case 0xFF8F00: return "Orange";
            case 0xC74EBD: return "Magenta";
            case 0x3F7FBF: return "Light Blue";
            case 0xFED83D: return "Yellow";
            case 0x80C71F: return "Lime";
            case 0xF38BAA: return "Pink";
            case 0x474F52: return "Gray";
            case 0x9D9D97: return "Light Gray";
            case 0x169C9C: return "Cyan";
            case 0x9932CC: return "Purple";
            case 0x3C44AA: return "Blue";
            case 0x825432: return "Brown";
            case 0x5E7C16: return "Green";
            case 0xB02E26: return "Red";
            case 0x1D1C21: return "Black";
            default: return "Custom";
        }
    }

    // Helper method to get backpack storage from data components
    public static net.minecraft.world.SimpleContainer getBackpackStorage(ItemStack stack) {
        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(27);

        // Load items from data component if they exist
        if (stack.has(ModDataComponents.BACKPACK_INVENTORY.get())) {
            List<ItemStack> items = stack.get(ModDataComponents.BACKPACK_INVENTORY.get());
            if (items != null) {
                for (int i = 0; i < Math.min(items.size(), 27); i++) {
                    ItemStack item = items.get(i);
                    if (item != null && !item.isEmpty()) {
                        container.setItem(i, item);
                    }
                }
            }
        }

        return container;
    }

    // Helper method to save backpack storage to data components
    public static void saveBackpackStorage(ItemStack stack, net.minecraft.world.Container container) {
        List<ItemStack> items = new java.util.ArrayList<>();

        // Convert container items to list
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            items.add(item.copy());
        }

        // Save to data component
        stack.set(ModDataComponents.BACKPACK_INVENTORY.get(), items);
    }

    // Helper method to check if backpack is dyed
    public static boolean isDyed(ItemStack stack) {
        return stack.has(ModDataComponents.BACKPACK_COLOR.get());
    }

    // Helper method to get dye color
    public static Integer getDyeColor(ItemStack stack) {
        return stack.get(ModDataComponents.BACKPACK_COLOR.get());
    }

    // Helper method to clear dye color
    public static void clearDyeColor(ItemStack stack) {
        stack.remove(ModDataComponents.BACKPACK_COLOR.get());
    }
}
