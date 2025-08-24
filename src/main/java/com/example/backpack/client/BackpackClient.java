package com.example.backpack.client;

import com.example.backpack.BackpackMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BackpackMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BackpackClient {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(BackpackMod.BACKPACK_MENU.get(), BackpackScreen::new);
    }
}
