package com.example.backpack.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backpack.menu.BackpackMenu;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class BackpackRecipeBookPanel extends AbstractWidget {

  private static final ResourceLocation RECIPE_BOOK_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-book.png");

      private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 200;

  // Recipe button area starts below search box
  private static final int RECIPE_BUTTON_START_Y = 30;

  private final BackpackScreen parentScreen;
  private final List<RecipeHolder<CraftingRecipe>> availableRecipes;
  private final List<RecipeButton> recipeButtons;
  private EditBox searchBox;
  private String searchQuery = "";
  private boolean isVisible = false;
  private int currentPage = 0;
      private static final int RECIPES_PER_PAGE = 20; // 5 columns × 4 rows
      private static final int GRID_COLUMNS = 5;
    private static final int GRID_ROWS = 4;

  // UI Elements
  private Button craftButton;
  private Button prevPageButton;
  private Button nextPageButton;

  public BackpackRecipeBookPanel(BackpackScreen parentScreen) {
    super(0, 0, PANEL_WIDTH, PANEL_HEIGHT,
          Component.translatable("gui.recipe_book"));
    this.parentScreen = parentScreen;
    this.availableRecipes = new ArrayList<>();
    this.recipeButtons = new ArrayList<>();

    // Initialize search box
    this.searchBox =
        new EditBox(Minecraft.getInstance().font, 0, 0, PANEL_WIDTH - 20, 12,
                    Component.translatable("gui.recipe_book.search"));

    // Initialize buttons
    this.craftButton =
        Button
            .builder(Component.translatable("gui.recipe_book.craft"),
                     button -> {
                       // TODO: Implement crafting logic
                       System.out.println("Craft button clicked!");
                     })
            .bounds(0, 0, 40, 16)
            .build();

    this.prevPageButton = Button
                              .builder(Component.literal("←"),
                                       button -> {
                                         if (currentPage > 0) {
                                           currentPage--;
                                           createRecipeButtons();
                                         }
                                       })
                              .bounds(0, 0, 20, 16)
                              .build();

    this.nextPageButton =
        Button
            .builder(Component.literal("→"),
                     button -> {
                       int totalPages = (int)Math.ceil(
                           (double)availableRecipes.size() / RECIPES_PER_PAGE);
                       if (currentPage < totalPages - 1) {
                         currentPage++;
                         createRecipeButtons();
                       }
                     })
            .bounds(0, 0, 20, 16)
            .build();

    // Load available recipes
    loadRecipes();
  }

  private void loadRecipes() {
    Level level = Minecraft.getInstance().level;
    if (level != null) {
      RecipeManager recipeManager = level.getRecipeManager();

      // Get all crafting recipes
      availableRecipes.clear();
      availableRecipes.addAll(
          recipeManager.getAllRecipesFor(RecipeType.CRAFTING));

      System.out.println("Loaded " + availableRecipes.size() +
                         " crafting recipes");

      // Create recipe buttons
      createRecipeButtons();
    }
  }

  private void createRecipeButtons() {
    recipeButtons.clear();

    List<RecipeHolder<CraftingRecipe>> filteredRecipes =
        availableRecipes.stream()
            .filter(
                recipeHolder
                -> searchQuery.isEmpty() ||
                       recipeHolder.value()
                           .getResultItem(
                               Minecraft.getInstance().level.registryAccess())
                           .getDisplayName()
                           .getString()
                           .toLowerCase()
                           .contains(searchQuery.toLowerCase()))
            .collect(Collectors.toList());

    System.out.println("Filtered recipes: " + filteredRecipes.size() +
                       " (search: '" + searchQuery + "')");

    int startIndex = currentPage * RECIPES_PER_PAGE;
    int endIndex =
        Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

    // Create grid layout: 3 columns x 4 rows
    // Use fixed 16x16 button size to match icon dimensions
    int buttonWidth = 16; // Fixed width to match icon size
    int totalMargins = 40; // 20px left + 20px right (adjusted for 200px panel)
    int totalSpacing = (GRID_COLUMNS - 1) * 9; // 9px between each column for 5-column layout (increased by 1px)
    int buttonHeight = 16; // Changed from 20 to 16 to match icon size (16x16)
    int buttonSpacing = 5; // Reduced from 5px to 2px for tighter vertical spacing

    // Adjust for the visual offset we observed
    int iconOffsetX = 40; // Increased from 25 to 40 (moved right by 15px more)
    int iconOffsetY = 16; // Changed from 14 to 16 (moved down by 2px)

    for (int i = startIndex; i < endIndex; i++) {
      int row = (i - startIndex) / GRID_COLUMNS;
      int col = (i - startIndex) % GRID_COLUMNS;

      // Position buttons relative to the panel, accounting for the visual
      // offset and centering the grid
      int totalGridWidth = (GRID_COLUMNS * buttonWidth) + ((GRID_COLUMNS - 1) * buttonSpacing);
      int gridStartX = (PANEL_WIDTH - totalGridWidth) / 2;
      int buttonX = gridStartX + (col * (buttonWidth + buttonSpacing)) + iconOffsetX;
      int buttonY = RECIPE_BUTTON_START_Y +
                    (row * (buttonHeight + buttonSpacing)) + iconOffsetY;

      RecipeHolder<CraftingRecipe> recipeHolder = filteredRecipes.get(i);
      RecipeButton button = new RecipeButton(buttonX, buttonY, buttonWidth,
                                             buttonHeight, recipeHolder, 0, 0);
      recipeButtons.add(button);

      // Debug: Show first few button positions
      if (i < 5) {
        System.out.println("Button " + i + " - Row: " + row + ", Col: " + col +
                          " - Position: (" + buttonX + ", " + buttonY + ")");
      }
    }

    System.out.println("Showing recipes " + (startIndex + 1) + " to " +
                       endIndex + " of " + filteredRecipes.size());

            System.out.println("Created " + recipeButtons.size() + " recipe buttons");
        System.out.println("Button dimensions - Width: " + buttonWidth +
                           ", Height: " + buttonHeight +
                           ", Spacing: " + buttonSpacing);
        System.out.println("Panel dimensions - Width: " + PANEL_WIDTH +
                           ", Height: " + PANEL_HEIGHT);
        System.out.println(
            "Total width needed: " +
            (buttonWidth * GRID_COLUMNS + buttonSpacing * (GRID_COLUMNS - 1) + 20));
        System.out.println("Grid layout - Columns: " + GRID_COLUMNS + ", Rows: " + GRID_ROWS);
        System.out.println("Column spacing: " + buttonSpacing + "px");
        System.out.println("Icon offset X: " + iconOffsetX + ", Icon offset Y: " + iconOffsetY);
  }

  public void setVisible(boolean visible) {
    this.isVisible = visible;
    if (visible) {
      // Calculate the center of the screen
      int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      int screenHeight =
          Minecraft.getInstance().getWindow().getGuiScaledHeight();

                  // Position the recipe book panel relative to the backpack's position
      // The backpack is positioned 8px right of center, so we want the recipe book
      // to be positioned to the left of the backpack with some spacing
      int screenCenter = screenWidth / 2;
      int backpackCenter = screenCenter + 8; // Backpack is 8px right of center
      int backpackLeftEdge = backpackCenter - (parentScreen.getImageWidth() / 2);
      
      // Position recipe book to the left of the backpack's left edge with minimal spacing
      // Ensure it's visible on screen by positioning it at least 10px from left edge
      int targetX = Math.max(10, backpackLeftEdge - PANEL_WIDTH - 8);
      this.setX(targetX);
      this.setY((screenHeight - PANEL_HEIGHT) / 2);

      System.out.println("Recipe Book Positioning Debug:");
      System.out.println("  Screen width: " + screenWidth);
      System.out.println("  Screen center: " + screenCenter);
      System.out.println("  Backpack center: " + backpackCenter);
      System.out.println("  Backpack left edge: " + backpackLeftEdge);
      System.out.println("  Panel width: " + PANEL_WIDTH);
      System.out.println("  Target X (left of backpack left edge with 8px spacing): " + targetX);
      System.out.println("  Final X: " + getX());

      // Update search box position
      searchBox.setX(getX() + 20);
      searchBox.setY(getY() + 20);

      // Update button positions
      craftButton.setX(getX() + 20);
      craftButton.setY(getY() + PANEL_HEIGHT - 30);

      prevPageButton.setX(getX() + PANEL_WIDTH - 60);
      prevPageButton.setY(getY() + PANEL_HEIGHT - 30);

      nextPageButton.setX(getX() + PANEL_WIDTH - 35);
      nextPageButton.setY(getY() + PANEL_HEIGHT - 30);

      // Refresh recipes when opening
      loadRecipes();
    }
  }

  public boolean isVisible() { return isVisible; }

  public int getWidth() { return PANEL_WIDTH; }

  @Override
  public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                           float partialTick) {
    if (!isVisible)
      return;

    RenderSystem.enableBlend();

    // Render recipe book background
    graphics.blit(RECIPE_BOOK_TEXTURE, getX(), getY(), 0, 0, PANEL_WIDTH,
                  PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);

    // Render search box
    searchBox.render(graphics, mouseX, mouseY, partialTick);

    // Render buttons
    craftButton.render(graphics, mouseX, mouseY, partialTick);
    prevPageButton.render(graphics, mouseX, mouseY, partialTick);
    nextPageButton.render(graphics, mouseX, mouseY, partialTick);

    // Render recipe buttons
    for (RecipeButton button : recipeButtons) {
      // Temporarily adjust button position for rendering
      int originalX = button.getX();
      int originalY = button.getY();
      button.setX(getX() + originalX);
      button.setY(getY() + originalY);

      button.render(graphics, mouseX, mouseY, partialTick);

      // Restore original position
      button.setX(originalX);
      button.setY(originalY);
    }

    // Render navigation info
    int totalPages =
        (int)Math.ceil((double)availableRecipes.size() / RECIPES_PER_PAGE);
    if (totalPages > 1) {
      String pageInfo = "Page " + (currentPage + 1) + " of " + totalPages;
      graphics.drawString(Minecraft.getInstance().font, pageInfo, getX() + 20,
                          getY() + PANEL_HEIGHT - 25, 0xFFFFFF);
    }

    RenderSystem.disableBlend();
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!isVisible)
      return false;

    // Check if click is within panel bounds
    if (mouseX >= getX() && mouseX < getX() + PANEL_WIDTH && mouseY >= getY() &&
        mouseY < getY() + PANEL_HEIGHT) {

      // Handle search box clicks
      if (searchBox.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }

      // Handle button clicks
      if (craftButton.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
      if (prevPageButton.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
      if (nextPageButton.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }

      // Handle recipe button clicks
      for (RecipeButton recipeButton : recipeButtons) {
        // Adjust mouse coordinates to be relative to the button
        double adjustedMouseX = mouseX - getX();
        double adjustedMouseY = mouseY - getY();
        if (recipeButton.mouseClicked(adjustedMouseX, adjustedMouseY, button)) {
          return true;
        }
      }

      return true;
    }

    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double deltaX,
                               double deltaY) {
    if (!isVisible)
      return false;

    if (mouseX >= getX() && mouseX < getX() + PANEL_WIDTH && mouseY >= getY() &&
        mouseY < getY() + PANEL_HEIGHT) {
      // Handle scrolling through recipes
      int totalPages =
          (int)Math.ceil((double)availableRecipes.size() / RECIPES_PER_PAGE);
      if (deltaY > 0 && currentPage > 0) {
        currentPage--;
        createRecipeButtons();
        return true;
      } else if (deltaY < 0 && currentPage < totalPages - 1) {
        currentPage++;
        createRecipeButtons();
        return true;
      }
      return true;
    }

    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (!isVisible)
      return false;

    // Handle search box key input
    if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
      searchQuery = searchBox.getValue();
      createRecipeButtons();
      return true;
    }

    return false;
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (!isVisible)
      return false;

    // Handle search box character input
    if (searchBox.charTyped(codePoint, modifiers)) {
      searchQuery = searchBox.getValue();
      currentPage = 0; // Reset to first page when searching
      createRecipeButtons();
      return true;
    }

    return false;
  }

  @Override
  public void setFocused(boolean focused) {
    super.setFocused(focused);
    searchBox.setFocused(focused);
  }

  @Override
  protected void
  updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    // Narration support for accessibility
  }

  private void fillCraftingGrid(CraftingRecipe recipe) {
    System.out.println("fillCraftingGrid called with recipe: " +
                       recipe.toString());

    // Get the crafting matrix from the parent screen's menu
    BackpackMenu menu = parentScreen.getMenu();
    System.out.println("Got menu: " + menu);

    // Clear the current crafting grid
    for (int i = 0; i < 9; i++) {
      menu.getSlot(i + 1).set(
          ItemStack.EMPTY); // +1 because slot 0 is the result
    }

    // Get the recipe ingredients
    List<Ingredient> ingredients = recipe.getIngredients();
    System.out.println("Recipe has " + ingredients.size() + " ingredients");

    // Fill the crafting grid with ingredients
    for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
      Ingredient ingredient = ingredients.get(i);
      if (ingredient != null && !ingredient.isEmpty()) {
        // Get the first matching item stack for this ingredient
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length > 0) {
          ItemStack ingredientStack = matchingStacks[0].copy();
          ingredientStack.setCount(1); // Set count to 1 for crafting
          menu.getSlot(i + 1).set(ingredientStack);
          System.out.println("Placed ingredient " +
                             ingredientStack.getDisplayName().getString() +
                             " in slot " + (i + 1));
        }
      }
    }

    // Also place the result item in the result slot (slot 0)
    ItemStack result =
        recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
    if (!result.isEmpty()) {
      // Try to set the result in the result slot
      try {
        menu.getSlot(0).set(result.copy());
        System.out.println("Placed result " +
                           result.getDisplayName().getString() +
                           " in result slot");
      } catch (Exception e) {
        System.out.println("Failed to place result in slot 0: " +
                           e.getMessage());
        // Fallback: place in first crafting slot
        menu.getSlot(1).set(result.copy());
        System.out.println("Placed result in crafting slot 1 as fallback");
      }
    }

    System.out.println(
        "Filled crafting grid with recipe ingredients and result");
  }

  private class RecipeButton extends AbstractWidget {
    private final RecipeHolder<CraftingRecipe> recipeHolder;
    private final int iconOffsetX;
    private final int iconOffsetY;

    public RecipeButton(int x, int y, int width, int height,
                        RecipeHolder<CraftingRecipe> recipeHolder,
                        int iconOffsetX, int iconOffsetY) {
      super(x, y, width, height, Component.empty());
      this.recipeHolder = recipeHolder;
      this.iconOffsetX = iconOffsetX;
      this.iconOffsetY = iconOffsetY;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                             float partialTick) {
      // Render recipe button background with a more visible border for
      // debugging
      int color = isHovered() ? 0x80FFFFFF : 0x40FFFFFF;
      graphics.fill(getX(), getY(), getX() + width, getY() + height, color);

      // Draw a border around the button to make the size visible
      // Make borders 1px bigger than the 16x16 squares (17x17) and only 1px thick
      graphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(),
                    0xFFFF0000); // Top border (1px thick, 1px above)
      graphics.fill(getX() - 1, getY() + height, getX() + width + 1,
                    getY() + height + 1, 0xFFFF0000); // Bottom border (1px thick, 1px below)
      graphics.fill(getX() - 1, getY() - 1, getX(),
                    getY() + height + 1, 0xFFFF0000); // Left border (1px thick, 1px left)
      graphics.fill(getX() + width, getY() - 1, getX() + width + 1,
                    getY() + height + 1, 0xFFFF0000); // Right border (1px thick, 1px right)

      // Render recipe result item (icon only, no text)
      Level level = Minecraft.getInstance().level;
      if (level != null) {
        ItemStack result =
            recipeHolder.value().getResultItem(level.registryAccess());
        if (!result.isEmpty()) {
          // Center the item icon in the button
          // Since button is now exactly 16x16, icon is perfectly centered
          int iconX = getX();
          int iconY = getY();

          // Debug: Log the button and icon dimensions
          if (recipeButtons.indexOf(this) ==
              0) { // Only log for first button to avoid spam
            System.out.println("First button - Button: " + getX() + "," +
                               getY() + " " + width + "x" + height +
                               ", Icon: " + iconX + "," + iconY + " 16x16");
          }

          // Draw a background for the icon area to make it visible
          // Green background is now exactly 16x16 to match button size
          graphics.fill(iconX, iconY, iconX + 16, iconY + 16, 0x8000FF00);

          // Render the item at the calculated position
          graphics.renderItem(result, iconX, iconY);
          graphics.renderItemDecorations(Minecraft.getInstance().font, result,
                                         iconX, iconY);
        }
      }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
      // Fill the crafting grid with the recipe ingredients
      System.out.println("Recipe button clicked! Recipe: " + recipeHolder.id());
      System.out.println("Button position: " + getX() + ", " + getY());
      System.out.println("Mouse position: " + mouseX + ", " + mouseY);
      fillCraftingGrid(recipeHolder.value());
    }

    @Override
    protected void
    updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      // Narration support for accessibility
    }
  }
}
