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
  private BackpackRecipeBookPanel recipeBookPanel;

  public BackpackScreen(BackpackMenu menu, Inventory inv, Component title) {
    super(menu, inv, title);

    // Match the background texture size you’re using
    this.imageWidth = TEX_W;
    this.imageHeight = TEX_H;

    // Title & inventory labels are not shown so hide them offscreen
    this.titleLabelX = 20000;
    this.inventoryLabelX = 20000;

    // Center the GUI initially
    int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
    this.leftPos = (screenWidth - this.imageWidth) / 2;
  }

  @Override
  protected void init() {
    super.init();

    // Create recipe book panel
    this.recipeBookPanel = new BackpackRecipeBookPanel(this);

    // Create invisible but functional recipe book button (16x16 pixels)
    this.recipeBookButton =
        Button
            .builder(Component.translatable(""),
                                          button -> {
                       // Toggle recipe book panel visibility
                       boolean isVisible = !this.recipeBookPanel.isVisible();
                       this.recipeBookPanel.setVisible(isVisible);

                       // Adjust GUI positioning when recipe book is open
                       if (isVisible) {
                         // When recipe book is open, position backpack GUI further right to make room
                         int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                         int screenCenter = screenWidth / 2;

                         // Backpack GUI: move right to make room for recipe book
                         // Recipe book is 200px wide + 16px spacing = 216px total space needed
                         this.leftPos = screenCenter - (this.imageWidth / 2) + 68;

                         System.out.println("Backpack Positioning Debug:");
                         System.out.println("  Screen width: " + screenWidth);
                         System.out.println("  Screen center: " + screenCenter);
                         System.out.println("  Backpack width: " + this.imageWidth);
                         System.out.println("  Target leftPos (68px right of center for recipe book): " + this.leftPos);
                       } else {
                         // Center the backpack GUI normally
                         int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                         this.leftPos = (screenWidth - this.imageWidth) / 2;
                       }

                       // Update button position after GUI movement
                       this.recipeBookButton.setX(this.leftPos + RECIPE_X);
                       this.recipeBookButton.setY(this.topPos + RECIPE_Y);
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

    // Render recipe book panel on top
    if (this.recipeBookPanel != null) {
      this.recipeBookPanel.render(gg, mouseX, mouseY, partialTick);
    }

    this.renderTooltip(gg, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    // Handle recipe book panel clicks first
    if (this.recipeBookPanel != null && this.recipeBookPanel.isVisible()) {
      if (this.recipeBookPanel.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
    // Handle recipe book panel scrolling first
    if (this.recipeBookPanel != null && this.recipeBookPanel.isVisible()) {
      if (this.recipeBookPanel.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
        return true;
      }
    }

    return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    // Handle recipe book panel key input first
    if (this.recipeBookPanel != null && this.recipeBookPanel.isVisible()) {
      if (this.recipeBookPanel.keyPressed(keyCode, scanCode, modifiers)) {
        return true;
      }
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    // Handle recipe book panel character input first
    if (this.recipeBookPanel != null && this.recipeBookPanel.isVisible()) {
      if (this.recipeBookPanel.charTyped(codePoint, modifiers)) {
        return true;
      }
    }

    return super.charTyped(codePoint, modifiers);
  }

  // Getter for image width to allow recipe book panel to position itself relative to backpack
  public int getImageWidth() {
    return this.imageWidth;
  }
}
