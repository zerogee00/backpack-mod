package com.example.backpack;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.mojang.serialization.Codec;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "backpack");

    // Store an ARGB color (int). Null/absent means "no dye"
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BACKPACK_COLOR =
        COMPONENTS.register("backpack_color", () ->
            DataComponentType.<Integer>builder().persistent(Codec.INT).build());

    // Store a boolean flag indicating if the backpack is dyed
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BACKPACK_DYED =
        COMPONENTS.register("backpack_dyed", () ->
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    private ModDataComponents() {}
}
