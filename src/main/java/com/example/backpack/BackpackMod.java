package com.example.backpack;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@Mod(BackpackMod.MODID)
public class BackpackMod {
    public static final String MODID = "backpack";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<Item, Item> BACKPACK_ITEM =
            ITEMS.register("backpack", () -> new BackpackItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<MenuType<?>, MenuType<com.example.backpack.menu.BackpackMenu>> BACKPACK_MENU =
            MENUS.register("backpack", () -> new MenuType<>(com.example.backpack.menu.BackpackMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BACKPACK_TAB = CREATIVE_TABS.register("backpack",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.backpack"))
                    .icon(() -> new net.minecraft.world.item.ItemStack(BACKPACK_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(BACKPACK_ITEM.get());
                    })
                    .build());

    public BackpackMod(IEventBus modBus) {
        ITEMS.register(modBus);
        MENUS.register(modBus);
        CREATIVE_TABS.register(modBus);

        // Add backpack to TOOLS creative tab
        modBus.addListener(this::addToCreativeTabs);
    }

    private void addToCreativeTabs(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BACKPACK_ITEM.get());
        }
    }
}
