package com.example.backpack;

import com.example.backpack.menu.BackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;

/** Opens menus using the 1.20.4+ approach (no NetworkHooks). */
public class NetworkHandler {
    public static void openScreen(ServerPlayer player, BackpackMenu.Provider provider, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(provider, buf -> {
            if (extraDataWriter != null) extraDataWriter.accept(buf);
            // No need to write anything for basic menu opening
        });
    }
}
