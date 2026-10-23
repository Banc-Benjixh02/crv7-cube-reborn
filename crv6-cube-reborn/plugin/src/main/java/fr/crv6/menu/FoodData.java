package fr.crv6.menu;

import org.bukkit.Material;

/** Nutrition / saturation for CRV6 food items. Excludes ecran + ore apples. */
public final class FoodData {
    public final String material;
    public final int cmd;
    public final int nutrition;   // half-shanks
    public final float saturation;
    public final String name;

    public FoodData(String material, int cmd, int nutrition, float saturation, String name) {
        this.material = material;
        this.cmd = cmd;
        this.nutrition = nutrition;
        this.saturation = saturation;
        this.name = name;
    }

    public static final FoodData[] ALL = {
            // Fruits / snacks
            new FoodData("apple", 1, 4, 0.6f, "Diamond Cookie"),   // cookie-like
            new FoodData("apple", 2, 3, 0.4f, "Orange"),
            new FoodData("apple", 3, 3, 0.5f, "Banane"),
            new FoodData("apple", 4, 2, 0.3f, "Grapes"),
            // Fast food
            new FoodData("baked_potato", 1, 8, 1.2f, "CRFC"),       // big meal
            new FoodData("baked_potato", 2, 8, 1.2f, "McDo"),
            // Meat / fish
            new FoodData("cooked_chicken", 1, 6, 0.8f, "Cooked Chokobo"),
            // skip gallashower (2) = shampoo
            new FoodData("cooked_cod", 1, 5, 0.7f, "Pwasson"),
            // Special
            new FoodData("cookie", 4, 4, 0.5f, "Mandragor"),
            new FoodData("cookie", 5, 0, 0f, "Chiantos ferme"), // not eaten closed
            new FoodData("cookie", 6, 20, 20f, "Chiantos ouvert"), // full food when eaten
            // EXCLUDE cookie 1,2,3 ore apples
            new FoodData("rabbit_stew", 1, 8, 1.0f, "Salade"),
            new FoodData("snowball", 1, 2, 0.3f, "Tomate"),
            // EXCLUDE snowball 2 ecran
    };

    public static FoodData match(String material, int cmd) {
        for (FoodData f : ALL) {
            if (f.material.equals(material) && f.cmd == cmd) return f;
        }
        return null;
    }

    public static FoodData match(Material mat, int cmd) {
        if (mat == null) return null;
        return match(mat.getKey().getKey(), cmd);
    }
}
