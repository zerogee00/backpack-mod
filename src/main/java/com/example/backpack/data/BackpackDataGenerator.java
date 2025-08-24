package com.example.backpack.data;

import com.example.backpack.BackpackMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BackpackMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BackpackDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        System.out.println("BackpackDataGenerator: Data generation started");

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        System.out.println("BackpackDataGenerator: Adding recipe provider");
        generator.addProvider(event.includeServer(), new BackpackRecipeProvider(output, lookupProvider));

        System.out.println("BackpackDataGenerator: Data generation completed");
    }
}
