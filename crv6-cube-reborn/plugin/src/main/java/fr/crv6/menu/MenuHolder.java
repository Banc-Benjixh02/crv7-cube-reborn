package fr.crv6.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
    public enum Type { MAIN, CATEGORY, TETES, TETE_DETAIL }

    public final Type type;
    public final String category;
    public final int page;
    /** Nom du joueur CR pour TETE_DETAIL */
    public final String playerName;

    public MenuHolder(Type type, String category, int page) {
        this(type, category, page, null);
    }

    public MenuHolder(Type type, String category, int page, String playerName) {
        this.type = type;
        this.category = category;
        this.page = page;
        this.playerName = playerName;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
