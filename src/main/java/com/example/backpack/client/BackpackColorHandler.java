package com.example.backpack.client;

import com.example.backpack.BackpackMod;
import com.example.backpack.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = "backpack", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BackpackColorHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            // Only show and tint the overlay layer (layer1 = tintIndex 1) when dyed
            if (tintIndex == 1) {
                // Check if the backpack is dyed
                if (stack.has(ModDataComponents.BACKPACK_DYED.get()) &&
                    stack.get(ModDataComponents.BACKPACK_DYED.get()) != null &&
                    stack.get(ModDataComponents.BACKPACK_DYED.get())) {

                    // If dyed, return the stored color for tinting
                    if (stack.has(ModDataComponents.BACKPACK_COLOR.get())) {
                        Integer color = stack.get(ModDataComponents.BACKPACK_COLOR.get());
                        if (color != null) {
                            return color;
                        }
                    }
                    // Fallback to white if dyed but no color
                    return 0xFFFFFFFF;
                } else {
                    // If not dyed, return transparent (invisible) for the overlay
                    return 0x00FFFFFF;
                }
            }
            // No tint for base layer (layer0 = tintIndex 0)
            return 0xFFFFFFFF;
        }, BackpackMod.BACKPACK_ITEM.get());
    }
}
