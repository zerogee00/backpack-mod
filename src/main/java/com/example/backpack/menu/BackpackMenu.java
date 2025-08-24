package com.example.backpack.menu;

import com.example.backpack.BackpackMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Backpack container menu:
 *   indices:
 *     0 ...................... result
 *     1..9 ................... crafting 3x3
 *     10..36 ................. backpack storage 27
 *     37..63 ................. player inventory 27
 *     64..72 ................. hotbar 9
 */
public class BackpackMenu extends AbstractContainerMenu {

  // --- must match BackpackScreen ---
  public static final int SLOT = 16;    // plate size (matches BackpackScreen)
  public static final int SPACING = 17; //  16 + 1px gap

  public static final int CRAFT_X = 57; // 3x3 crafting grid (top-left)
  public static final int CRAFT_Y = 19;

  public static final int RESULT_X = 159;
  public static final int RESULT_Y = 28;

  public static final int RECIPE_X = 159; // recipe button icon (below result)
  public static final int RECIPE_Y = RESULT_Y + SPACING + 9;

  public static final int PACK_X = 40; // 3x9 backpack storage
  public static final int PACK_Y = 80;

  public static final int INV_X = 40; // 3x9 player inventory
  public static final int INV_Y =
      PACK_Y + (3 * SPACING) + 8; // 8px space between sections

  public static final int HOT_X = 40; // 1x9 hotbar
  public static final int HOT_Y = INV_Y + (3 * SPACING) + 9;

  // index ranges
  private static final int RESULT_SLOT = 0;
  private static final int CRAFT_FIRST = 1;
  private static final int CRAFT_COUNT = 9;
  private static final int CRAFT_LAST = CRAFT_FIRST + CRAFT_COUNT - 1;

  private static final int PACK_FIRST = CRAFT_LAST + 1; // 10
  private static final int PACK_COUNT = 27;
  private static final int PACK_LAST = PACK_FIRST + PACK_COUNT - 1; // 36

  private static final int INV_FIRST = PACK_LAST + 1; // 37
  private static final int INV_COUNT = 27;
  private static final int INV_LAST = INV_FIRST + INV_COUNT - 1; // 63

  private static final int HOT_FIRST = INV_LAST + 1; // 64
  private static final int HOT_COUNT = 9;
  private static final int HOT_LAST = HOT_FIRST + HOT_COUNT - 1; // 72

  // crafting
  private final Container craftMatrix = new SimpleContainer(9);
  private final Container craftResult = new SimpleContainer(1);

  // the backpack’s 27-slot storage (swap for your block entity/container)
  private final Container backpackInv;

  private final Player player;
  private final Level level;

  public BackpackMenu(int id, Inventory playerInv) {
    this(id, playerInv,
         new SimpleContainer(
             PACK_COUNT)); // fallback if you’re opening from an item
  }

  public BackpackMenu(int id, Inventory playerInv, Container backpackStorage) {
    super(BackpackMod.BACKPACK_MENU.get(), id); // Use our custom menu type
    this.player = playerInv.player;
    this.level = player.level();
    this.backpackInv = backpackStorage;

    this.addSlot(new Slot(this.craftResult, RESULT_SLOT, RESULT_X, RESULT_Y) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return false; // Result slot cannot accept items
      }
    });

    // Crafting grid 3x3 (indices 1..9)
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 3; c++) {
        int idx = CRAFT_FIRST + c + r * 3;
        this.addSlot(new Slot(this.craftMatrix, idx - CRAFT_FIRST,
                              CRAFT_X + c * SPACING, CRAFT_Y + r * SPACING) {
          @Override
          public void setChanged() {
            super.setChanged();
            BackpackMenu.this.slotsChanged(this.container);
          }
        });
      }
    }

    // Backpack storage 3x9 (indices 10..36)
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 9; c++) {
        int slot = PACK_FIRST + c + r * 9;
        this.addSlot(new Slot(this.backpackInv, slot - PACK_FIRST,
                              PACK_X + c * SPACING, PACK_Y + r * SPACING));
      }
    }

    // Player inventory 3x9 (indices 37..63)
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 9; c++) {
        int slot = INV_FIRST + c + r * 9;
        this.addSlot(new Slot(playerInv, c + r * 9 + 9, INV_X + c * SPACING,
                              INV_Y + r * SPACING));
      }
    }

    // Hotbar 1x9 (indices 64..72)
    for (int c = 0; c < 9; c++) {
      int slot = HOT_FIRST + c;
      this.addSlot(new Slot(playerInv, c, HOT_X + c * SPACING, HOT_Y));
    }

    onCraftMatrixChanged();
  }

  // Re-run recipe matching whenever inputs change
  @Override
  public void slotsChanged(Container container) {
    super.slotsChanged(container);
    if (container == craftMatrix)
      onCraftMatrixChanged();
  }

  private void onCraftMatrixChanged() {
    // For now, disable crafting until we implement proper recipe handling
    // TODO: Implement proper crafting logic
    System.out.println("Crafting matrix changed!");
    this.craftResult.setItem(0, ItemStack.EMPTY);
  }

  @Override
  public boolean stillValid(Player player) {
    return true; // tighten if your storage is a block entity
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    this.craftResult.clearContent();
    if (!player.level().isClientSide) {
      this.clearContainer(player, this.craftMatrix);
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int clickedIndex) {
    ItemStack old = ItemStack.EMPTY;
    Slot clicked = this.slots.get(clickedIndex);
    if (!clicked.hasItem())
      return ItemStack.EMPTY;

    ItemStack stack = clicked.getItem();
    old = stack.copy();

    // 1) Move from result into player inventory first
    if (clickedIndex == RESULT_SLOT) {
      if (!this.moveItemStackTo(stack, INV_FIRST, HOT_LAST, true))
        return ItemStack.EMPTY;
      clicked.onQuickCraft(stack, old);
    }
    // 2) Click in backpack storage -> player inventory/hotbar
    else if (clickedIndex >= PACK_FIRST && clickedIndex <= PACK_LAST) {
      if (!this.moveItemStackTo(stack, INV_FIRST, HOT_LAST, false))
        return ItemStack.EMPTY;
    }
    // 3) Click in player inventory/hotbar -> backpack storage
    else if (clickedIndex >= INV_FIRST && clickedIndex <= HOT_LAST) {
      if (!this.moveItemStackTo(stack, PACK_FIRST, PACK_LAST, false))
        return ItemStack.EMPTY;
    }
    // 4) Crafting grid -> player inventory
    else if (clickedIndex >= CRAFT_FIRST && clickedIndex <= CRAFT_LAST) {
      if (!this.moveItemStackTo(stack, INV_FIRST, HOT_LAST, false))
        return ItemStack.EMPTY;
    }

    if (stack.isEmpty())
      clicked.set(ItemStack.EMPTY);
    else
      clicked.setChanged();

    if (stack.getCount() == old.getCount())
      return ItemStack.EMPTY;
    clicked.onTake(player, stack);
    return old;
  }

  // Provider class for opening the menu
  public static class Provider implements MenuProvider {
    private final ItemStack backpackStack;

    public Provider(ItemStack stack) { this.backpackStack = stack.copy(); }

    @Override
    public Component getDisplayName() {
      return Component.translatable("container.backpack");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv,
                                            Player player) {
      return new BackpackMenu(id, playerInv);
    }
  }
}
