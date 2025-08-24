package com.example.backpack;

import java.util.List;

public class GuiLayout {
    public static class Size { public int w, h; }
    public static class Pos { public int x, y; }
    public static class Button { public int x, y, w, h; }

    public String texture;
    public Size size;
    public List<Pos> craft;
    public Pos result;
    public Button recipe_book_button;
    public List<Pos> backpack;
    public List<Pos> player;
    public List<Pos> hotbar;
}
