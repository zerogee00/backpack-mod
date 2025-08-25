package com.example.backpack;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "backpack");

    // Store an ARGB color (int). Null/absent means "no dye"
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BACKPACK_COLOR =
        COMPONENTS.register("backpack_color", () ->
            DataComponentType.<Integer>builder().persistent(Codec.INT).build());

    // Store whether the backpack is dyed (boolean)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BACKPACK_DYED =
        COMPONENTS.register("backpack_dyed", () ->
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    // Store the backpack inventory as a list of ItemStacks
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> BACKPACK_INVENTORY =
        COMPONENTS.register("backpack_inventory", () ->
            DataComponentType.<List<ItemStack>>builder().persistent(Codec.list(ItemStack.OPTIONAL_CODEC)).build());

    private ModDataComponents() {}
}
