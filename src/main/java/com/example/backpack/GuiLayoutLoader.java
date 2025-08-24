package com.example.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GuiLayoutLoader {
    private static final Gson GSON = new Gson();

    public static GuiLayout load(ResourceLocation jsonPath) throws IOException, JsonParseException {
        Minecraft mc = Minecraft.getInstance();
        Optional<Resource> resOpt = mc.getResourceManager().getResource(jsonPath);
        if (resOpt.isEmpty()) {
            throw new IOException("Layout not found: " + jsonPath);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resOpt.get().open(), StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, GuiLayout.class);
        }
    }
}
