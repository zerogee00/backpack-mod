package com.example.backpack.data;

import com.example.backpack.BackpackMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class BackpackRecipeProvider extends RecipeProvider {

    public BackpackRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@javax.annotation.Nonnull RecipeOutput output) {
        System.out.println("BackpackRecipeProvider: Building recipes");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BackpackMod.BACKPACK_ITEM.get())
            .pattern("LLL")
            .pattern("NGN")
            .pattern("LLL")
            .define('L', Items.LEATHER)
            .define('N', Items.IRON_NUGGET)
            .define('G', Items.GOLD_INGOT)
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(output);

        System.out.println("BackpackRecipeProvider: Backpack recipe saved");
    }
}
