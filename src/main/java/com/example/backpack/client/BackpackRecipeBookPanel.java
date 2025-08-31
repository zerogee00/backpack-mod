package com.example.backpack.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.backpack.menu.BackpackMenu;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class BackpackRecipeBookPanel extends AbstractWidget {

  private static final ResourceLocation RECIPE_BOOK_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-book.png");

  // Custom craftable overlay textures
  private static final ResourceLocation CRAFTABLE_OVERLAY_CHECK =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/craftable-overlay_check.png");
  private static final ResourceLocation CRAFTABLE_OVERLAY_X =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/craftable-overlay_x.png");

  // Custom tab background textures
  private static final ResourceLocation TAB_BACKGROUND_2 =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-tabs-2.png");
  private static final ResourceLocation TAB_BACKGROUND_3 =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-tabs-3.png");
  private static final ResourceLocation TAB_BACKGROUND_4 =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-tabs-4.png");
  private static final ResourceLocation TAB_BACKGROUND_5 =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/recipe-tabs-5.png");

  // Navigation and search assets
  private static final ResourceLocation LEFT_ARROW_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/left-arrow.png");
  private static final ResourceLocation RIGHT_ARROW_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/right-arrow.png");
  private static final ResourceLocation MAGNIFYING_GLASS_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("backpack",
                                            "textures/gui/magnifying_glass.png");

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

  // Tab system
  private List<TabButton> tabButtons;
  private int selectedTab = 0; // 0 = All, 1 = Weapons, 2 = Building, 3 = Equipment, 4 = Redstone

  public BackpackRecipeBookPanel(BackpackScreen parentScreen) {
    super(0, 0, PANEL_WIDTH, PANEL_HEIGHT,
          Component.translatable("gui.recipe_book"));
    this.parentScreen = parentScreen;
    this.availableRecipes = new ArrayList<>();
    this.recipeButtons = new ArrayList<>();

    // Initialize search box
    this.searchBox =
        new EditBox(Minecraft.getInstance().font, 0, 0, 70, 10,
                    Component.translatable("gui.recipe_book.search"));
    this.searchBox.setValue(""); // Start with empty value
    this.searchBox.setBordered(false); // Remove the grey border/background
    this.searchBox.setMaxLength(50); // Set max length for search queries
    this.searchBox.setCanLoseFocus(false); // Keep focus when clicking elsewhere

    // Initialize buttons
    this.craftButton =
        Button
            .builder(Component.empty(), // No text, we'll render the icon manually
                     button -> {
                       // Toggle craftable filter
                       toggleCraftableFilter();
                     })
            .bounds(0, 0, 20, 20) // Square button for icon
            .build();
    this.craftButton.setAlpha(0.0f); // Make completely invisible like other buttons

    this.prevPageButton = Button
                              .builder(Component.empty(), // No text, we'll render the icon manually
                                       button -> {
                                         if (currentPage > 0) {
                                           currentPage--;
                                           createRecipeButtons();
                                         }
                                       })
                              .bounds(0, 0, 20, 20)
                              .build();
    this.prevPageButton.setAlpha(0.0f); // Make completely invisible

    this.nextPageButton =
        Button
            .builder(Component.empty(), // No text, we'll render the icon manually
                     button -> {
                       // Calculate total pages based on filtered recipes
                       List<RecipeHolder<CraftingRecipe>> filteredRecipes = getFilteredRecipes();
                       int totalPages = (int)Math.ceil((double)filteredRecipes.size() / RECIPES_PER_PAGE);
                       if (currentPage < totalPages - 1) {
                         currentPage++;
                         createRecipeButtons();
                       }
                     })
            .bounds(0, 0, 20, 20)
            .build();
    this.nextPageButton.setAlpha(0.0f); // Make completely invisible

    // Initialize tab buttons
    this.tabButtons = new ArrayList<>();
    createTabButtons();

    // Load available recipes
    loadRecipes();
  }

  private boolean showOnlyCraftable = false;

  private List<RecipeHolder<CraftingRecipe>> getFilteredRecipes() {
    return availableRecipes.stream()
        .filter(
            recipeHolder
            -> (searchQuery.isEmpty() ||
                   recipeHolder.value()
                       .getResultItem(
                           Minecraft.getInstance().level.registryAccess())
                       .getDisplayName()
                       .getString()
                       .toLowerCase()
                       .contains(searchQuery.toLowerCase())) &&
               (!showOnlyCraftable || isRecipeCraftable(recipeHolder.value())) &&
               isRecipeInSelectedCategory(recipeHolder.value()))
        .collect(Collectors.toList());
  }

  private boolean isRecipeInSelectedCategory(CraftingRecipe recipe) {
    if (selectedTab == 0) return true; // Search/All tab shows everything

    ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
    if (result.isEmpty()) return false;

    String itemId = result.getItem().toString().toLowerCase();

    switch (selectedTab) {
      case 1: // Building tab - tools, weapons, armor (matches axe and sword icons)
        return itemId.contains("axe") || itemId.contains("pickaxe") || itemId.contains("shovel") ||
               itemId.contains("hoe") || itemId.contains("sword") || itemId.contains("bow") ||
               itemId.contains("crossbow") || itemId.contains("arrow") || itemId.contains("helmet") ||
               itemId.contains("chestplate") || itemId.contains("leggings") || itemId.contains("boots") ||
               itemId.contains("shield") || itemId.contains("fishing_rod") || itemId.contains("shears") ||
               itemId.contains("flint_and_steel") || itemId.contains("compass") || itemId.contains("clock");

      case 2: // Tools tab - blocks, building materials (matches brick block icon)
        return itemId.contains("block") || itemId.contains("planks") || itemId.contains("stone") ||
               itemId.contains("brick") || itemId.contains("glass") || itemId.contains("wool") ||
               itemId.contains("carpet") || itemId.contains("stairs") || itemId.contains("slab") ||
               itemId.contains("fence") || itemId.contains("wall") || itemId.contains("door") ||
               itemId.contains("trapdoor") || itemId.contains("gate") || itemId.contains("bed") ||
               itemId.contains("chest") || itemId.contains("bookshelf") || itemId.contains("ladder");

      case 3: // Equipment tab - food, buckets, containers (matches lava bucket and apple icons)
        return itemId.contains("apple") || itemId.contains("bread") || itemId.contains("cake") ||
               itemId.contains("cookie") || itemId.contains("bucket") || itemId.contains("bowl") ||
               itemId.contains("bottle") || itemId.contains("potion") || itemId.contains("splash") ||
               itemId.contains("lingering") || itemId.contains("saddle") || itemId.contains("name_tag") ||
               itemId.contains("lead") || itemId.contains("minecart") || itemId.contains("boat") ||
               itemId.contains("furnace_minecart") || itemId.contains("chest_minecart") || itemId.contains("hopper_minecart");

      case 4: // Redstone tab - redstone components, mechanisms (matches redstone dust icon)
        return itemId.contains("redstone") || itemId.contains("repeater") || itemId.contains("comparator") ||
               itemId.contains("dispenser") || itemId.contains("dropper") || itemId.contains("hopper") ||
               itemId.contains("piston") || itemId.contains("sticky_piston") || itemId.contains("observer") ||
               itemId.contains("detector_rail") || itemId.contains("activator_rail") || itemId.contains("powered_rail") ||
               itemId.contains("rail") || itemId.contains("lever") || itemId.contains("button") ||
               itemId.contains("pressure_plate") || itemId.contains("tripwire_hook") || itemId.contains("daylight_detector") ||
               itemId.contains("note_block") || itemId.contains("jukebox") || itemId.contains("tnt") ||
               itemId.contains("tnt_minecart") || itemId.contains("command_block") || itemId.contains("structure_block");

      default:
        return true;
    }
  }

  private void createTabButtons() {
    tabButtons.clear();

    // Create tab buttons with vanilla recipe book icons
    // Search/All tab (Compass)
    tabButtons.add(new TabButton(0, 0, 0, 20, 20,
        new ItemStack(net.minecraft.world.item.Items.COMPASS), "Search"));

    // Building tab (Axe and Gold Sword)
    tabButtons.add(new TabButton(1, 0, 0, 20, 20,
        new ItemStack(net.minecraft.world.item.Items.IRON_AXE),
        new ItemStack(net.minecraft.world.item.Items.GOLDEN_SWORD), "Building"));

    // Tools tab (Brick Block)
    tabButtons.add(new TabButton(2, 0, 0, 20, 20,
        new ItemStack(net.minecraft.world.item.Items.BRICKS), "Tools"));

    // Equipment tab (Bucket of Lava and Apple)
    tabButtons.add(new TabButton(3, 0, 0, 20, 20,
        new ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET),
        new ItemStack(net.minecraft.world.item.Items.APPLE), "Equipment"));

    // Redstone tab (Redstone Dust)
    tabButtons.add(new TabButton(4, 0, 0, 20, 20,
        new ItemStack(net.minecraft.world.item.Items.REDSTONE), "Redstone"));
  }

  private void toggleCraftableFilter() {
    showOnlyCraftable = !showOnlyCraftable;
    System.out.println("Craftable filter: " + (showOnlyCraftable ? "ON" : "OFF"));
    currentPage = 0; // Reset to first page when filtering
    createRecipeButtons();
  }

    private boolean isRecipeCraftable(CraftingRecipe recipe) {
    // Check if the recipe can be crafted with available materials in inventory and backpack
    BackpackMenu menu = parentScreen.getMenu();
    if (menu == null) return false;

    // Get all available items from inventory and backpack storage with quantities
    Map<ItemStack, Integer> availableItems = new HashMap<>();

    // Add items from player inventory (slots 37+ are player inventory)
    for (int i = 37; i < menu.slots.size(); i++) {
      ItemStack stack = menu.getSlot(i).getItem();
      if (!stack.isEmpty()) {
        // Use a key that ignores stack size for counting
        ItemStack key = stack.copy();
        key.setCount(1);
        availableItems.merge(key, stack.getCount(), Integer::sum);
      }
    }

        // Check if we have all required ingredients with sufficient quantities
    // Count how many of each ingredient type we need
    Map<Ingredient, Integer> requiredIngredients = new HashMap<>();

    for (Ingredient ingredient : recipe.getIngredients()) {
      if (ingredient.isEmpty()) continue;

      // Count how many times this ingredient appears in the recipe
      requiredIngredients.merge(ingredient, 1, Integer::sum);
    }

    // Check if we have enough of each required ingredient
    for (Map.Entry<Ingredient, Integer> required : requiredIngredients.entrySet()) {
      Ingredient ingredient = required.getKey();
      int requiredCount = required.getValue();

      // Count how many of this ingredient type we have
      int availableCount = 0;
      for (Map.Entry<ItemStack, Integer> entry : availableItems.entrySet()) {
        if (ingredient.test(entry.getKey())) {
          availableCount += entry.getValue();
        }
      }

      if (availableCount < requiredCount) {
        return false; // Not enough of this ingredient
      }
    }

    return true; // All ingredients available in sufficient quantities
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

    List<RecipeHolder<CraftingRecipe>> filteredRecipes = getFilteredRecipes();

    System.out.println("Filtered recipes: " + filteredRecipes.size() +
                       " (search: '" + searchQuery + "')");

    int startIndex = currentPage * RECIPES_PER_PAGE;
    int endIndex =
        Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

    // Create grid layout: 5 columns x 4 rows with proper spacing
    int buttonWidth = 17; // Fixed width to match icon size (increased by 1px)
    int buttonHeight = 17; // Fixed height to match icon size (increased by 1px)
    int buttonSpacing = 5; // Consistent spacing between buttons (reduced from 8px to 5px)

    // Calculate grid positioning to center it properly in the panel
    int totalGridWidth = (GRID_COLUMNS * buttonWidth) + ((GRID_COLUMNS - 1) * buttonSpacing);
    int gridStartX = (PANEL_WIDTH - totalGridWidth) / 2 + 12; // Center the grid horizontally + 12px right
    int gridStartY = RECIPE_BUTTON_START_Y + 27; // Start 27px below the search box (10px + 17px down)

    // Only create buttons for recipes that exist
    for (int i = startIndex; i < endIndex; i++) {
      int row = (i - startIndex) / GRID_COLUMNS;
      int col = (i - startIndex) % GRID_COLUMNS;

      // Position buttons in a clean grid layout
      // Spacing should only be between buttons, not added to each button position
      int buttonX = gridStartX + (col * buttonWidth) + (col * buttonSpacing);
      int buttonY = gridStartY + (row * buttonHeight) + (row * buttonSpacing);

      RecipeHolder<CraftingRecipe> recipeHolder = filteredRecipes.get(i);
      RecipeButton button = new RecipeButton(buttonX, buttonY, buttonWidth,
                                             buttonHeight, recipeHolder);
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
        System.out.println("Total grid width: " + totalGridWidth + "px");
        System.out.println("Grid layout - Columns: " + GRID_COLUMNS + ", Rows: " + GRID_ROWS);
        System.out.println("Column spacing: " + buttonSpacing + "px");
        System.out.println("Grid start position - X: " + gridStartX + ", Y: " + gridStartY);
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

      // Update search box position - move it 70px right and 39px lower to make room for magnifying glass
      searchBox.setX(getX() + 70); // Move 70px right from left edge (was 60px, now 70px to move everything right by 10px)
      searchBox.setY(getY() + 39); // Move 39px down from top

                  // Update tab button positions - vertical tabs down the left side with fixed spacing
      int tabX = getX() + 25; // 25px from left edge
      int tabStartY = getY() + 30; // Start below the top area

                        // Manually set each tab position to ensure exact spacing
      tabButtons.get(0).setX(tabX + 2); // Compass tab (Tab 1) - move right by 2px
      tabButtons.get(0).setY(tabStartY);
      tabButtons.get(0).setSelected(0 == selectedTab);

      tabButtons.get(1).setX(tabX); // Axe+Sword tab (Tab 2) - move right by 1px (was -1, now 0)
      tabButtons.get(1).setY(tabStartY + 25); // Move up by 1px (was +26, now +25)
      tabButtons.get(1).setSelected(1 == selectedTab);

      tabButtons.get(2).setX(tabX + 2); // Bricks tab (Tab 3) - move right by 2px
      tabButtons.get(2).setY(tabStartY + 49); // Move up by 1px (was +50, now +49)
      tabButtons.get(2).setSelected(2 == selectedTab);

      tabButtons.get(3).setX(tabX + 1); // Lava Bucket+Apple tab (Tab 4) - move right by 1px more (was 0, now +1)
      tabButtons.get(3).setY(tabStartY + 73); // Move up by 1px (was +74, now +73)
      tabButtons.get(3).setSelected(3 == selectedTab);

      tabButtons.get(4).setX(tabX + 3); // Redstone tab (Tab 5) - keep at +3 (no change needed)
      tabButtons.get(4).setY(tabStartY + 97); // Move down by 1px (was +96, now +97)
      tabButtons.get(4).setSelected(4 == selectedTab);

      // Debug: Show tab positions
      System.out.println("Tab positions - Start Y: " + tabStartY);
      for (int i = 0; i < tabButtons.size(); i++) {
        System.out.println("Tab " + i + " - Position: (" + tabX + ", " + tabButtons.get(i).getY() + ")");
      }

      // Update button positions
      // Craft button to the right of search bar
      craftButton.setX(getX() + 145); // 70px (search bar X) + 70px (search bar width) + 10px spacing - 8px left + 3px right
      craftButton.setY(getY() + 34); // Same Y as search bar - 5px up

      // Page navigation centered under the grid
      int pageNavCenterX = getX() + (PANEL_WIDTH / 2) + 10; // Move left by 10px (was +20, now +10)
      int pageNavY = getY() + RECIPE_BUTTON_START_Y + 27 + (GRID_ROWS * (17 + 5)) - 5; // Raise by 20px (was +15, now -5)

      prevPageButton.setX(pageNavCenterX - 33); // Match visual arrow position
      prevPageButton.setY(pageNavY + 5); // Match visual arrow position

      nextPageButton.setX(pageNavCenterX + 19); // Match visual arrow position (moved left by 3px)
      nextPageButton.setY(pageNavY + 5); // Match visual arrow position

      // Focus the search box when opening
      searchBox.setFocused(true);

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

    // Render recipe book background based on selected tab
    ResourceLocation backgroundTexture = RECIPE_BOOK_TEXTURE; // Default background

    if (selectedTab > 0) {
      switch (selectedTab) {
        case 1: // Building tab
          backgroundTexture = TAB_BACKGROUND_2;
          break;
        case 2: // Tools tab
          backgroundTexture = TAB_BACKGROUND_3;
          break;
        case 3: // Equipment tab
          backgroundTexture = TAB_BACKGROUND_4;
          break;
        case 4: // Redstone tab
          backgroundTexture = TAB_BACKGROUND_5;
          break;
      }
    }

    graphics.blit(backgroundTexture, getX(), getY(), 0, 0, PANEL_WIDTH,
                  PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);

    // Render magnifying glass icon to the left of search box
    graphics.blit(MAGNIFYING_GLASS_TEXTURE, searchBox.getX() - 18, searchBox.getY() - 2, 0, 0, 16, 16, 16, 16);

    // Render search box
    searchBox.render(graphics, mouseX, mouseY, partialTick);

    // Render italic placeholder text if search box is empty
    if (searchBox.getValue().isEmpty()) {
      graphics.drawString(Minecraft.getInstance().font,
                         Component.literal("Search...").withStyle(ChatFormatting.ITALIC),
                         searchBox.getX() + 2, searchBox.getY() + 2,
                         0x707070, false);
    }

    // Store icon position for later use
    int iconX = craftButton.getX() + 2;
    int iconY = craftButton.getY() + 2;

    // Render tab buttons
    for (TabButton tab : tabButtons) {
      tab.render(graphics, mouseX, mouseY, partialTick);
    }

    // Don't render button background - just render icon directly
    // Note: Icon will be rendered AFTER overlays to ensure overlays appear on top

    // Page navigation buttons are invisible - only custom arrows are rendered

    // Render custom arrow images over the transparent buttons
    int pageCount = (int)Math.ceil((double)availableRecipes.size() / RECIPES_PER_PAGE);
    if (pageCount > 1) {
      int pageNavCenterX = getX() + (PANEL_WIDTH / 2) + 10; // Move left by 10px
      int pageNavY = getY() + RECIPE_BUTTON_START_Y + 27 + (GRID_ROWS * (17 + 5)) - 5;

      // Render arrow images positioned on either side of the text
      // Left arrow
      graphics.blit(LEFT_ARROW_TEXTURE, pageNavCenterX - 29, pageNavY + 10, 0, 0, 10, 10, 10, 10);

      // Right arrow
      graphics.blit(RIGHT_ARROW_TEXTURE, pageNavCenterX + 19, pageNavY + 10, 0, 0, 10, 10, 10, 10);
    }

    // Render recipe buttons
    for (RecipeButton button : recipeButtons) {
      // Temporarily   adjust button position for rendering
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
    // Calculate pages based on filtered recipes, not total available recipes
    List<RecipeHolder<CraftingRecipe>> filteredRecipes = getFilteredRecipes();

    int totalPages =
        (int)Math.ceil((double)filteredRecipes.size() / RECIPES_PER_PAGE);
    if (totalPages > 1) {
      String pageInfo = currentPage + 1 + "/" + totalPages;
      // Center the page info between the navigation arrows
      int pageNavCenterX = getX() + (PANEL_WIDTH / 2) + 10;
      int pageNavY = getY() + RECIPE_BUTTON_START_Y + 27 + (GRID_ROWS * (17 + 5)) - 5;
      int textWidth = Minecraft.getInstance().font.width(pageInfo);
      // Render main text (no drop shadow)
      graphics.drawString(Minecraft.getInstance().font, pageInfo,
                         pageNavCenterX - (textWidth / 2), pageNavY + 10, 0x5c4122); // Custom brown color
    }

    // Render vanilla crafting table icon FIRST
    ItemStack craftingTable = new ItemStack(net.minecraft.world.item.Items.CRAFTING_TABLE);
    graphics.renderItem(craftingTable, iconX, iconY);
    graphics.renderItemDecorations(Minecraft.getInstance().font, craftingTable, iconX, iconY);

    if (showOnlyCraftable) {
      // Disable depth test to show overlay always on top
      RenderSystem.disableDepthTest();
      // Draw check overlay when filter is active (showing only craftable recipes)
      // Use the check overlay texture
      graphics.blit(CRAFTABLE_OVERLAY_CHECK, iconX - 5, iconY - 3, 0, 0, 10, 10, 10, 10);
      // Re-enable depth test
      RenderSystem.enableDepthTest();
    } else {
      // Draw X overlay when filter is inactive (showing all recipes)
      // Disable depth test to show overlay always on top
      RenderSystem.disableDepthTest();
      // Use the X overlay texture
      graphics.blit(CRAFTABLE_OVERLAY_X, iconX - 5, iconY - 3, 0, 0, 10, 10, 10, 10);
      // Re-enable depth test
      RenderSystem.enableDepthTest();
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

      // Handle tab button clicks
      for (TabButton tab : tabButtons) {
        if (tab.mouseClicked(mouseX, mouseY, button)) {
          return true;
        }
      }

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

    // Handle escape key to close recipe book
    if (keyCode == 256) { // 256 is the key code for Escape
      setVisible(false);
      return true;
    }

    // If search box is focused, handle all key input to prevent conflicts
    if (searchBox.isFocused()) {
      if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
        searchQuery = searchBox.getValue();
        createRecipeButtons();
        return true;
      }
      // Even if the search box didn't handle it, consume the key to prevent inventory closing
      return true;
    }

    // Handle search box key input when not focused
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

    // If search box is focused, handle all character input to prevent conflicts
    if (searchBox.isFocused()) {
      if (searchBox.charTyped(codePoint, modifiers)) {
        searchQuery = searchBox.getValue();
        currentPage = 0; // Reset to first page when searching
        createRecipeButtons();
        return true;
      }
      // Even if the search box didn't handle it, consume the character to prevent inventory closing
      return true;
    }

    // Handle search box character input when not focused
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

    // Get the craftMatrix container directly from the menu
    Container craftMatrix = menu.getCraftMatrix();
    if (craftMatrix == null) {
      System.out.println("ERROR: Could not get craftMatrix from menu");
      return;
    }

    // Clear the current crafting grid
    for (int i = 0; i < 9; i++) {
      craftMatrix.setItem(i, ItemStack.EMPTY);
    }

    // Get the recipe ingredients
    List<Ingredient> ingredients = recipe.getIngredients();
    System.out.println("Recipe has " + ingredients.size() + " ingredients");

    // Fill the crafting grid with ingredients from player's inventory
    for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
      Ingredient ingredient = ingredients.get(i);
      if (ingredient != null && !ingredient.isEmpty()) {
        // Find a matching item in the player's inventory or backpack
        ItemStack matchingItem = findMatchingItem(ingredient, menu);
        if (!matchingItem.isEmpty()) {
          // Move the item to the crafting grid
          craftMatrix.setItem(i, matchingItem.copy());
          System.out.println("Moved ingredient " +
                             matchingItem.getDisplayName().getString() +
                             " to crafting slot " + i);
        } else {
          System.out.println("No matching ingredient found for slot " + i);
        }
      }
    }

    // Trigger the menu's slotsChanged method to update the crafting result
    menu.slotsChanged(craftMatrix);

    // Don't set the result directly - let the normal crafting logic handle it
    // The onCraftMatrixChanged() method will be called automatically when ingredients are placed
    // and it will calculate and set the result if there's a valid recipe match

    System.out.println("Filled crafting grid with recipe ingredients - result will be calculated automatically");
  }

  private ItemStack findMatchingItem(Ingredient ingredient, BackpackMenu menu) {
    // First check player inventory (slots 37-72)
    for (int i = 37; i < menu.slots.size(); i++) {
      ItemStack stack = menu.getSlot(i).getItem();
      if (!stack.isEmpty() && ingredient.test(stack)) {
        return stack;
      }
    }

    // Then check backpack storage (slots 10-36)
    for (int i = 10; i < 37; i++) {
      ItemStack stack = menu.getSlot(i).getItem();
      if (!stack.isEmpty() && ingredient.test(stack)) {
        return stack;
      }
    }

    return ItemStack.EMPTY;
  }

  private class RecipeButton extends AbstractWidget {
    private final RecipeHolder<CraftingRecipe> recipeHolder;

    public RecipeButton(int x, int y, int width, int height,
                        RecipeHolder<CraftingRecipe> recipeHolder) {
      super(x, y, width, height, Component.empty());
      this.recipeHolder = recipeHolder;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                             float partialTick) {
      // Render recipe button background - subtle hover effect only
      int color = isHovered() ? 0x40FFFFFF : 0x20FFFFFF;
      graphics.fill(getX(), getY(), getX() + width, getY() + height, color);

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

          // Debug background removed for cleaner look

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

  private class TabButton extends AbstractWidget {
    private final int tabId;
    private final ItemStack icon1;
    private final ItemStack icon2; // Second icon for tabs that need two items
    private final String label;
    private boolean isSelected = false;

    public TabButton(int tabId, int x, int y, int width, int height, ItemStack icon, String label) {
      super(x, y, width, height, Component.literal(label));
      this.tabId = tabId;
      this.icon1 = icon;
      this.icon2 = null;
      this.label = label;
    }

    public TabButton(int tabId, int x, int y, int width, int height, ItemStack icon1, ItemStack icon2, String label) {
      super(x, y, width, height, Component.literal(label));
      this.tabId = tabId;
      this.icon1 = icon1;
      this.icon2 = icon2;
      this.label = label;
    }

    public void setSelected(boolean selected) {
      this.isSelected = selected;
    }

    public int getTabId() {
      return tabId;
    }

            @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      // Render icon(s) in the tab - no background since we use full GUI backgrounds
      if (icon2 != null) {
        // Render two icons side by side with more spacing, moved left by 4px total
        graphics.renderItem(icon1, getX() - 3, getY() + 2);
        graphics.renderItemDecorations(Minecraft.getInstance().font, icon1, getX() - 3, getY() + 2);
        graphics.renderItem(icon2, getX() + 9, getY() + 2);
        graphics.renderItemDecorations(Minecraft.getInstance().font, icon2, getX() + 9, getY() + 2);
      } else {
        // Render single icon centered
        graphics.renderItem(icon1, getX() + 2, getY() + 2);
        graphics.renderItemDecorations(Minecraft.getInstance().font, icon1, getX() + 2, getY() + 2);
      }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
      selectedTab = tabId;
      currentPage = 0; // Reset to first page when changing tabs
      createRecipeButtons();

      // Update all tab selections
      for (TabButton tab : tabButtons) {
        tab.setSelected(tab.getTabId() == selectedTab);
      }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      // Narration support for accessibility
    }
  }
}
