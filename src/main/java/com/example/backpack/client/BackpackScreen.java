package com.example.backpack.client;

import com.example.backpack.menu.BackpackMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {

  // --- Textures (put these under: resources/assets/backpack/textures/gui/) ---
  private static final ResourceLocation BG_TEX =
      ResourceLocation.fromNamespaceAndPath(
          "backpack", "textures/gui/backpack.png"); // your stitched background

  // --- Background size
  private static final int TEX_W = 230, TEX_H = 230;

  // --- Slot geometry ---
  private static final int SLOT = 16;    // plate size
  private static final int SPACING = 17; // 16 + 1px gap

  // --- Layout (all coords are relative to the top-left of the GUI background)
  // --- These values line up with the stitched PNG you shared.
  private static final int CRAFT_X = 57; // 3x3 crafting grid (top-left)
  private static final int CRAFT_Y = 19;

  private static final int RESULT_X = 159; // result slot (top-right stack)
  private static final int RESULT_Y = 30;

  private static final int RECIPE_X = 159; // recipe button icon (below result)
  private static final int RECIPE_Y = RESULT_Y + SPACING + 9;

  private static final int PACK_X = 40; // 3x9 backpack storage
  private static final int PACK_Y = 80;

  private static final int INV_X = 40; // 3x9 player inventory
  private static final int INV_Y =
      PACK_Y + (3 * SPACING) + 8; // 8px space between sections

  private static final int HOT_X = 40; // 1x9 hotbar
  private static final int HOT_Y = INV_Y + (3 * SPACING) + 9;

  // Recipe book button
  private Button recipeBookButton;

  public BackpackScreen(BackpackMenu menu, Inventory inv, Component title) {
    super(menu, inv, title);

    // Match the background texture size you’re using
    this.imageWidth = TEX_W;
    this.imageHeight = TEX_H;

    // Title & inventory labels are not shown so hide them offscreen
    this.titleLabelX = 20000;
    this.inventoryLabelX = 20000;
  }

  @Override
  protected void init() {
    super.init();

    // Create invisible but functional recipe book button (16x16 pixels)
    this.recipeBookButton =
        Button
            .builder(Component.translatable(""),
                     button -> {
                       // TODO: Implement recipe book functionality
                       System.out.println("Recipe book button clicked!");
                     })
            .bounds(this.leftPos + RECIPE_X, this.topPos + RECIPE_Y, 16, 16)
            .build();

    // Make the button completely transparent
    this.recipeBookButton.setAlpha(0.0f);

    this.addRenderableWidget(this.recipeBookButton);
  }

  @Override
  protected void renderBg(GuiGraphics gg, float partialTick, int mouseX,
                          int mouseY) {
    RenderSystem.enableBlend();

    // 1) Background
    gg.blit(BG_TEX, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight,
            TEX_W, TEX_H);

    // 2) Recipe book button highlight (when hovering)
    // Manually check if mouse is over the button area
    int buttonX = this.leftPos + RECIPE_X;
    int buttonY = this.topPos + RECIPE_Y;
    boolean isHovering = mouseX >= buttonX && mouseY >= buttonY &&
                         mouseX < buttonX + 16 && mouseY < buttonY + 16;

    if (isHovering) {
      // Semi-transparent white highlight (you can adjust the color and alpha)
      gg.fill(buttonX, buttonY, buttonX + 16, buttonY + 16,
              0x80FFFFFF); // 0x80 = 50% opacity white
    }

    RenderSystem.disableBlend();
  }

  @Override
  public void render(GuiGraphics gg, int mouseX, int mouseY,
                     float partialTick) {
    this.renderBackground(gg, mouseX, mouseY,
                          partialTick); // vanilla dim backdrop
    super.render(gg, mouseX, mouseY, partialTick);
    this.renderTooltip(gg, mouseX, mouseY);
  }
}
