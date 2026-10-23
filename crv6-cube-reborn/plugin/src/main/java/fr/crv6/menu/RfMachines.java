package fr.crv6.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Systeme RF: generateur charbon, panneau solaire, batterie, collecteur oxygen.
 */
public final class RfMachines implements Listener {

    public enum Type { COAL_GEN, SOLAR, BATTERY, OXYGEN }

    public static final class Machine {
        public final Type type;
        public final Location loc;
        public int rf;
        public final int maxRf;
        public int burnLeft; // ticks of coal burn remaining
        public UUID displayId;
        public UUID interactionId;
        public float yaw; // orientation a la pose
        public final ItemStack[] slots; // internal inv

        Machine(Type type, Location loc, float yaw) {
            this.type = type;
            this.loc = loc.clone();
            this.yaw = yaw;
            this.maxRf = switch (type) {
                case COAL_GEN -> 2000;
                case SOLAR -> 500;
                case BATTERY -> 10000;
                case OXYGEN -> 1500;
            };
            this.slots = new ItemStack[switch (type) {
                case COAL_GEN -> 3;   // fuel slots
                case SOLAR -> 0;
                case BATTERY -> 2;  // charge item in/out
                case OXYGEN -> 4;   // bottles
            }];
        }
    }

    public static final class RfHolder implements InventoryHolder {
        public final String key;
        public final Type type;
        public RfHolder(String key, Type type) { this.key = key; this.type = type; }
        @Override public Inventory getInventory() { return null; }
    }

    private final JavaPlugin plugin;
    private final Map<String, Machine> machines = new ConcurrentHashMap<>();
    private final NamespacedKey KEY_RF = new NamespacedKey("crv6", "rf_machine");
    private final NamespacedKey KEY_RF_TYPE = new NamespacedKey("crv6", "rf_type");
    private final NamespacedKey KEY_RF_YAW = new NamespacedKey("crv6", "rf_yaw");
    private final NamespacedKey KEY_ITEM_RF = new NamespacedKey("crv6", "item_rf");

    // CMD on dropper
    public static final int CMD_COAL = 1, CMD_SOLAR = 2, CMD_BATTERY = 3, CMD_OXYGEN = 4, CMD_COAL_ON = 5;

    public RfMachines(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L); // 1s
        Bukkit.getScheduler().runTaskLater(plugin, this::rebind, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, this::rebind, 100L);
        Bukkit.getScheduler().runTaskLater(plugin, this::rebind, 200L);
        plugin.getLogger().info("[RF] Systeme energie RF charge");
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Type typeFromCmd(int cmd) {
        return switch (cmd) {
            case CMD_COAL -> Type.COAL_GEN;
            case CMD_SOLAR -> Type.SOLAR;
            case CMD_BATTERY -> Type.BATTERY;
            case CMD_OXYGEN -> Type.OXYGEN;
            default -> null;
        };
    }

    private int cmdFromType(Type t) {
        return switch (t) {
            case COAL_GEN -> CMD_COAL;
            case SOLAR -> CMD_SOLAR;
            case BATTERY -> CMD_BATTERY;
            case OXYGEN -> CMD_OXYGEN;
        };
    }

    private String title(Type t) {
        return switch (t) {
            case COAL_GEN -> "§8§l⚙ §6Generateur a charbon §8§l⚙";
            case SOLAR -> "§8§l⚡ §ePanneau solaire §8§l⚡";
            case BATTERY -> "§8§l🔋 §bBatterie RF §8§l🔋";
            case OXYGEN -> "§8§l🧪 §3Collecteur d'oxygen §8§l🧪";
        };
    }

    public boolean isMachineItem(ItemStack stack) {
        if (stack == null || stack.getType() != Material.DROPPER) return false;
        int cmd = getCmd(stack);
        return cmd >= 1 && cmd <= 4;
    }

    private int getCmd(ItemStack stack) {
        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasCustomModelDataComponent()) {
                List<Float> f = meta.getCustomModelDataComponent().getFloats();
                if (f != null && !f.isEmpty()) return f.get(0).intValue();
            }
            if (meta != null && meta.hasCustomModelData()) return meta.getCustomModelData();
        } catch (Throwable ignored) {}
        return 0;
    }

    public ItemStack machineItem(Type type) {
        ItemStack stack = new ItemStack(Material.DROPPER);
        ItemMeta meta = stack.getItemMeta();
        int cmd = cmdFromType(type);
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        String name = switch (type) {
            case COAL_GEN -> "Generateur a charbon";
            case SOLAR -> "Panneau solaire";
            case BATTERY -> "Batterie RF";
            case OXYGEN -> "Collecteur d'oxygen";
        };
        meta.displayName(Component.text(name, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Machine RF — clique droit pour placer", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Puis clique la machine pour ouvrir le menu", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    // ---- Placement ----

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (!isMachineItem(hand)) return;
        if (event.getClickedBlock() == null) return;
        event.setCancelled(true);

        Type type = typeFromCmd(getCmd(hand));
        if (type == null) return;
        Location place = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        if (!place.getBlock().getType().isAir()) {
            event.getPlayer().sendActionBar(Component.text("Pas la place", NamedTextColor.RED));
            return;
        }
        place.getBlock().setType(Material.BARRIER);
        float yaw = event.getPlayer().getLocation().getYaw();
        // snap 90 deg selon ou le joueur regarde
        yaw = Math.round(yaw / 90f) * 90f;
        Machine m = new Machine(type, place, yaw);
        spawnVisual(m);
        machines.put(key(place), m);
        hand.setAmount(hand.getAmount() - 1);
        event.getPlayer().playSound(place, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.2f);
        event.getPlayer().sendActionBar(Component.text("Machine RF placee", NamedTextColor.GREEN));
    }

    private void spawnVisual(Machine m) {
        Location dloc = m.loc.clone().add(0.5, 0.0, 0.5);
        if (m.type == Type.SOLAR) dloc.add(0, 0.0, 0);

        // remove old
        if (m.displayId != null) {
            var e = Bukkit.getEntity(m.displayId);
            if (e != null) e.remove();
        }
        ItemDisplay disp = m.loc.getWorld().spawn(dloc, ItemDisplay.class);
        disp.setPersistent(true);
        disp.setInvulnerable(true);
        disp.setGravity(false);
        try { disp.setBillboard(Display.Billboard.FIXED); } catch (Throwable ignored) {}
        try { disp.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
        try {
            float yOff = 0.5f; // +8 pixels
            // Minecraft yaw: 0 = south, 90 = west — rotation Y en radians
            float yawRad = (float) Math.toRadians(-m.yaw);
            disp.setTransformation(new Transformation(
                    new Vector3f(0f, yOff, 0f),
                    new AxisAngle4f(yawRad, 0f, 1f, 0f),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            ));
            disp.setRotation(m.yaw, 0f);
        } catch (Throwable ignored) {}
        disp.setItemStack(machineItem(m.type));
        disp.getPersistentDataContainer().set(KEY_RF, PersistentDataType.BYTE, (byte) 1);
        disp.getPersistentDataContainer().set(KEY_RF_TYPE, PersistentDataType.STRING, m.type.name());
        disp.getPersistentDataContainer().set(KEY_RF_YAW, PersistentDataType.FLOAT, m.yaw);
        m.displayId = disp.getUniqueId();

        // Interaction hitbox
        if (m.interactionId != null) {
            var e = Bukkit.getEntity(m.interactionId);
            if (e != null) e.remove();
        }
        // Barrier solide (invisible) pour le clic + Interaction large
        if (m.loc.getBlock().getType() != Material.BARRIER) {
            m.loc.getBlock().setType(Material.BARRIER);
        }
        Location iloc = m.loc.clone().add(0.5, 0.0, 0.5);
        Interaction inter = m.loc.getWorld().spawn(iloc, Interaction.class);
        inter.setPersistent(true);
        inter.setInteractionWidth(0.95f);
        inter.setInteractionHeight(m.type == Type.SOLAR ? 0.55f : 1.0f);
        try { inter.setResponsive(true); } catch (Throwable ignored) {}
        inter.getPersistentDataContainer().set(KEY_RF, PersistentDataType.BYTE, (byte) 1);
        inter.getPersistentDataContainer().set(KEY_RF_TYPE, PersistentDataType.STRING, key(m.loc));
        m.interactionId = inter.getUniqueId();
    }

    /** Machine exactement sous le regard (raycast bloc ou centre proche). */
    private Machine findMachineLookedAt(Player player) {
        // 1) Raytrace blocks — barrier machine
        try {
            var ray = player.rayTraceBlocks(4.5);
            if (ray != null && ray.getHitBlock() != null) {
                Machine m = machines.get(key(ray.getHitBlock().getLocation()));
                if (m != null) return m;
            }
        } catch (Throwable ignored) {}

        // 2) Points le long du regard, uniquement le bloc traverse
        Location eye = player.getEyeLocation();
        var dir = eye.getDirection().normalize();
        for (double d = 0.2; d <= 4.0; d += 0.15) {
            Location probe = eye.clone().add(dir.clone().multiply(d));
            Location bl = new Location(
                    probe.getWorld(),
                    Math.floor(probe.getX()),
                    Math.floor(probe.getY()),
                    Math.floor(probe.getZ())
            );
            Machine m = machines.get(key(bl));
            if (m != null) return m;
        }
        return null;
    }


    // ---- Open GUI ----

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction inter)) return;
        if (!inter.getPersistentDataContainer().has(KEY_RF, PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        String k = inter.getPersistentDataContainer().get(KEY_RF_TYPE, PersistentDataType.STRING);
        if (k == null) return;
        Machine m = machines.get(k);
        if (m == null) return;
        openGui(event.getPlayer(), m);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractAtEntity(org.bukkit.event.player.PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction inter)) return;
        if (!inter.getPersistentDataContainer().has(KEY_RF, PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        String k = inter.getPersistentDataContainer().get(KEY_RF_TYPE, PersistentDataType.STRING);
        if (k == null) return;
        Machine m = machines.get(k);
        if (m == null) return;
        openGui(event.getPlayer(), m);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (isMachineItem(player.getInventory().getItemInMainHand())) return;
        if (event.getClickedBlock() == null) return;

        Machine m = machines.get(key(event.getClickedBlock().getLocation()));
        if (m == null) return;

        event.setCancelled(true);
        openGui(player, m);
    }


    private static final String NEG = "\uE005"; // -8px
    // Fonds GUI distincts par machine (pack: menu_rf_*.png)
    private static final String BG_COAL = "\uE00B";
    private static final String BG_SOLAR = "\uE00C";
    private static final String BG_BATTERY = "\uE00D";
    private static final String BG_OXYGEN = "\uE00E";

    private Component rfGuiTitle(Type type) {
        String bg = switch (type) {
            case COAL_GEN -> BG_COAL;
            case SOLAR -> BG_SOLAR;
            case BATTERY -> BG_BATTERY;
            case OXYGEN -> BG_OXYGEN;
        };
        return Component.text()
                .append(Component.text(NEG + bg)
                        .font(net.kyori.adventure.key.Key.key("minecraft", "default"))
                        .color(NamedTextColor.WHITE))
                .build();
    }

    private void openGui(Player player, Machine m) {
        int size = 27;
        Inventory inv = Bukkit.createInventory(new RfHolder(key(m.loc), m.type), size, rfGuiTitle(m.type));
        fillTheme(inv, m);
        syncSlots(inv, m);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 0.4f, 1.5f);
    }

    private ItemStack pane(Material mat, String name) {
        ItemStack s = new ItemStack(mat);
        ItemMeta meta = s.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        s.setItemMeta(meta);
        return s;
    }

    private static final int CMD_PILE = 9201;
    private static final int CMD_ECLAIR = 9202;

    private ItemStack iconPaper(int cmd, String name, NamedTextColor color) {
        ItemStack s = new ItemStack(Material.PAPER);
        ItemMeta meta = s.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        s.setItemMeta(meta);
        return s;
    }

    private ItemStack info(Machine m) {
        ItemStack s = iconPaper(CMD_PILE, "Energie RF", NamedTextColor.YELLOW);
        ItemMeta meta = s.getItemMeta();
        List<Component> lore = new ArrayList<>();
        int pct = m.maxRf == 0 ? 0 : (m.rf * 100 / m.maxRf);
        lore.add(Component.text(m.rf + " / " + m.maxRf + " RF", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(bar(pct), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        if (m.type == Type.COAL_GEN) {
            lore.add(Component.text("Brulage: " + (m.burnLeft > 0 ? (m.burnLeft + "s") : "eteint"),
                    NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Prod: 40 RF/s | Range 5x5", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else if (m.type == Type.SOLAR) {
            boolean day = m.loc.getWorld().getEnvironment() == World.Environment.NORMAL
                    && m.loc.getWorld().isDayTime();
            lore.add(Component.text(day ? "Soleil actif" : "Nuit / pas de soleil",
                    day ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Prod: 15 RF/s | Range 8x8", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else if (m.type == Type.BATTERY) {
            lore.add(Component.text("Stockage + recharge d'outils", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Range redistribution 8x8", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else if (m.type == Type.OXYGEN) {
            lore.add(Component.text("Consomme 25 RF / tick de fill", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        s.setItemMeta(meta);
        return s;
    }

    private String bar(int pct) {
        int n = Math.max(0, Math.min(10, pct / 10));
        return "§a" + "█".repeat(n) + "§8" + "█".repeat(10 - n) + " §7" + pct + "%";
    }

    private void fillTheme(Inventory inv, Machine m) {
        ItemStack invFiller = invisibleFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, invFiller.clone());
        }
        // slots interactifs = vides (seront remplis par syncSlots)
        for (int slot : interactiveSlots(m.type)) {
            inv.setItem(slot, null);
        }
        inv.setItem(4, info(m));
        inv.setItem(26, closeButton());
    }

    /** Slots ou le joueur peut poser / prendre des items. */
    private int[] interactiveSlots(Type type) {
        return switch (type) {
            case COAL_GEN -> new int[]{12, 13, 14};
            case BATTERY -> new int[]{13, 15};
            case OXYGEN -> new int[]{12, 13, 14, 15};
            case SOLAR -> new int[]{};
        };
    }

    private static final int CMD_INVISIBLE = 9204;

    /** Item invisible qui bloque le slot (pas de perte d'items). */
    private ItemStack invisibleFiller() {
        ItemStack s = new ItemStack(Material.PAPER);
        ItemMeta meta = s.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) CMD_INVISIBLE));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(CMD_INVISIBLE); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        try { meta.setHideTooltip(true); } catch (Throwable ignored) {}
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        s.setItemMeta(meta);
        return s;
    }

    private ItemStack closeButton() {
        // Meme croix que le menu admin (paper CMD 9203 = rf_menu_back)
        ItemStack s = new ItemStack(Material.PAPER);
        ItemMeta meta = s.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of(9203f));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(9203); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text("Fermer", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(Component.text("Ferme le menu", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)));
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        s.setItemMeta(meta);
        return s;
    }

    private void syncSlots(Inventory inv, Machine m) {
        // clear working slots then put machine slots
        if (m.type == Type.COAL_GEN) {
            inv.setItem(11, label(Material.COAL, "§6Combustible", "Charbon / charbon de bois"));
            inv.setItem(12, m.slots[0]);
            inv.setItem(13, m.slots[1]);
            inv.setItem(14, m.slots[2]);
        } else if (m.type == Type.BATTERY) {
            inv.setItem(11, label(Material.NETHERITE_PICKAXE, "§bRecharge outil", "Desassembleur etc."));
            inv.setItem(13, m.slots[0]);
            inv.setItem(15, m.slots[1]);
        } else if (m.type == Type.OXYGEN) {
            inv.setItem(10, label(Material.GLASS_BOTTLE, "§3Bouteilles O2", "Slots de remplissage"));
            inv.setItem(12, m.slots[0]);
            inv.setItem(13, m.slots[1]);
            inv.setItem(14, m.slots[2]);
            inv.setItem(15, m.slots[3]);
        } else if (m.type == Type.SOLAR) {
            inv.setItem(13, label(Material.SUNFLOWER, "§eSolaire", "Aucun slot — production auto"));
        }
        inv.setItem(4, info(m));
    }

    private ItemStack label(Material mat, String name, String lore) {
        ItemStack s = new ItemStack(mat);
        ItemMeta meta = s.getItemMeta();
        meta.displayName(Component.text(name.replace("§6", "").replace("§b", "").replace("§3", "").replace("§e", ""),
                NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        s.setItemMeta(meta);
        return s;
    }

    private boolean isBorderSlot(Machine m, int slot) {
        if (slot == 26 || slot == 4) return true; // croix + info
        for (int s : interactiveSlots(m.type)) {
            if (s == slot) return false;
        }
        return true;
    }

    private boolean isFiller(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        if (stack.getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) return true;
        if (stack.getType() == Material.GRAY_STAINED_GLASS_PANE) return true;
        if (stack.getType() == Material.BLACK_STAINED_GLASS_PANE) return true;
        if (stack.getType() == Material.PAPER) {
            try {
                ItemMeta meta = stack.getItemMeta();
                if (meta != null && meta.hasCustomModelDataComponent()) {
                    var floats = meta.getCustomModelDataComponent().getFloats();
                    if (floats != null && !floats.isEmpty() && floats.get(0).intValue() == CMD_INVISIBLE)
                        return true;
                }
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == CMD_INVISIBLE)
                    return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private void readSlots(Inventory inv, Machine m) {
        if (m.type == Type.COAL_GEN) {
            m.slots[0] = cloneOrNull(inv.getItem(12));
            m.slots[1] = cloneOrNull(inv.getItem(13));
            m.slots[2] = cloneOrNull(inv.getItem(14));
        } else if (m.type == Type.BATTERY) {
            m.slots[0] = cloneOrNull(inv.getItem(13));
            m.slots[1] = cloneOrNull(inv.getItem(15));
        } else if (m.type == Type.OXYGEN) {
            m.slots[0] = cloneOrNull(inv.getItem(12));
            m.slots[1] = cloneOrNull(inv.getItem(13));
            m.slots[2] = cloneOrNull(inv.getItem(14));
            m.slots[3] = cloneOrNull(inv.getItem(15));
        }
    }

    private ItemStack cloneOrNull(ItemStack s) {
        if (s == null || s.getType().isAir()) return null;
        // ignore decorative panes / labels
        if (s.getType().name().contains("GLASS_PANE")) return null;
        if (s.getType() == Material.REDSTONE_LAMP || s.getType() == Material.SUNFLOWER) return null;
        if (isFiller(s)) return null;
        return s.clone();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RfHolder holder)) return;
        Machine m = machines.get(holder.key);
        if (m == null) return;
        int raw = event.getRawSlot();
        // Croix fermer
        if (raw == 26) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player p) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            }
            return;
        }
        if (raw < event.getInventory().getSize() && isBorderSlot(m, raw)) {
            event.setCancelled(true);
            return;
        }
        // empeche de prendre un filler si un arrive dans le curseur
        ItemStack cur = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (isFiller(cur) || isFiller(cursor)) {
            event.setCancelled(true);
            return;
        }
        // allow taking from machine slots; refresh info next tick
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (event.getWhoClicked() instanceof Player p) {
                Inventory top = p.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof RfHolder) {
                    readSlots(top, m);
                    top.setItem(4, info(m));
                }
            }
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof RfHolder holder)) return;
        Machine m = machines.get(holder.key);
        if (m == null) return;
        readSlots(event.getInventory(), m);
    }

    // ---- Tick logic ----

    private void tick() {
        // 1) produce
        for (Machine m : machines.values()) {
            switch (m.type) {
                case COAL_GEN -> tickCoal(m);
                case SOLAR -> tickSolar(m);
                default -> {}
            }
        }
        // 2) distribute RF from gens/batteries to nearby machines that need it
        distribute();
        // 3) consumers
        for (Machine m : machines.values()) {
            if (m.type == Type.OXYGEN) tickOxygen(m);
            if (m.type == Type.BATTERY) tickBatteryCharge(m);
        }
        // 4) refresh open GUIs
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof RfHolder h) {
                Machine m = machines.get(h.key);
                if (m != null) {
                    p.getOpenInventory().getTopInventory().setItem(4, info(m));
                }
            }
        }
    }

    private void tickCoal(Machine m) {
        boolean wasBurning = m.burnLeft > 0;
        if (m.burnLeft > 0) {
            m.burnLeft--;
            m.rf = Math.min(m.maxRf, m.rf + 40); // 40 RF/s
            // fumee noire au-dessus
            Location smoke = m.loc.clone().add(0.5, 1.4, 0.5);
            m.loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, smoke, 2, 0.1, 0.15, 0.1, 0.01);
            m.loc.getWorld().spawnParticle(Particle.SMOKE, smoke, 3, 0.12, 0.2, 0.12, 0.005);
            updateCoalVisual(m, true);
            return;
        }
        // try consume coal from slots
        for (int i = 0; i < m.slots.length; i++) {
            ItemStack s = m.slots[i];
            if (s == null) continue;
            if (s.getType() == Material.COAL || s.getType() == Material.CHARCOAL) {
                s.setAmount(s.getAmount() - 1);
                if (s.getAmount() <= 0) m.slots[i] = null;
                m.burnLeft = 8; // 8 seconds per coal
                m.rf = Math.min(m.maxRf, m.rf + 40);
                updateCoalVisual(m, true);
                return;
            }
            if (s.getType() == Material.COAL_BLOCK) {
                s.setAmount(s.getAmount() - 1);
                if (s.getAmount() <= 0) m.slots[i] = null;
                m.burnLeft = 80;
                updateCoalVisual(m, true);
                return;
            }
        }
        if (wasBurning) updateCoalVisual(m, false);
        else updateCoalVisual(m, false);
    }

    private void updateCoalVisual(Machine m, boolean on) {
        if (m.displayId == null) return;
        var e = Bukkit.getEntity(m.displayId);
        if (!(e instanceof ItemDisplay disp)) return;
        ItemStack visual = machineItem(Type.COAL_GEN);
        if (on) {
            ItemMeta meta = visual.getItemMeta();
            try {
                CustomModelDataComponent c = meta.getCustomModelDataComponent();
                c.setFloats(java.util.List.of((float) CMD_COAL_ON));
                meta.setCustomModelDataComponent(c);
            } catch (Throwable t) {
                try { meta.setCustomModelData(CMD_COAL_ON); } catch (Throwable ignored) {}
            }
            visual.setItemMeta(meta);
        }
        disp.setItemStack(visual);
    }

    private void tickSolar(Machine m) {
        World w = m.loc.getWorld();
        if (w.getEnvironment() != World.Environment.NORMAL) return;
        if (!w.isDayTime()) return;
        // sky access: simple check highest block
        int highest = w.getHighestBlockYAt(m.loc);
        if (highest > m.loc.getBlockY() + 1) return;
        m.rf = Math.min(m.maxRf, m.rf + 15);
    }

    private void distribute() {
        // push RF from producers to batteries/oxygen in range
        List<Machine> list = new ArrayList<>(machines.values());
        for (Machine src : list) {
            if (src.rf <= 0) continue;
            int range = switch (src.type) {
                case COAL_GEN -> 5;
                case SOLAR, BATTERY -> 8;
                default -> 0;
            };
            if (range == 0) continue;
            for (Machine dst : list) {
                if (dst == src) continue;
                if (dst.type != Type.BATTERY && dst.type != Type.OXYGEN) continue;
                if (dst.rf >= dst.maxRf) continue;
                if (!sameWorld(src, dst)) continue;
                if (chebyshev(src.loc, dst.loc) > range) continue;
                int space = dst.maxRf - dst.rf;
                int move = Math.min(20, Math.min(space, src.rf)); // transfer rate
                if (move <= 0) continue;
                src.rf -= move;
                dst.rf += move;
            }
        }
    }

    private boolean sameWorld(Machine a, Machine b) {
        return a.loc.getWorld().equals(b.loc.getWorld());
    }

    private int chebyshev(Location a, Location b) {
        return Math.max(Math.abs(a.getBlockX() - b.getBlockX()),
                Math.max(Math.abs(a.getBlockY() - b.getBlockY()),
                        Math.abs(a.getBlockZ() - b.getBlockZ())));
    }

    private void tickOxygen(Machine m) {
        // fill bottles: carrot_on_a_stick CMD 10-13 with damage
        for (int i = 0; i < m.slots.length; i++) {
            ItemStack s = m.slots[i];
            if (s == null) continue;
            if (s.getType() != Material.CARROT_ON_A_STICK) continue;
            if (m.rf < 25) return;
            ItemMeta meta = s.getItemMeta();
            if (!(meta instanceof Damageable dmg)) continue;
            int max = dmg.getMaxDamage() > 0 ? dmg.getMaxDamage() : 100;
            int damage = dmg.getDamage();
            if (damage <= 0) continue; // full
            // reduce damage = refill oxygen
            int newDmg = Math.max(0, damage - Math.max(1, max / 20));
            dmg.setDamage(newDmg);
            s.setItemMeta(meta);
            m.rf -= 25;
            m.slots[i] = s;
            return; // one bottle per tick
        }
    }

    private void tickBatteryCharge(Machine m) {
        // charge tools in slots with item RF PDC
        for (int i = 0; i < m.slots.length; i++) {
            ItemStack s = m.slots[i];
            if (s == null) continue;
            if (m.rf < 10) return;
            ItemMeta meta = s.getItemMeta();
            if (meta == null) continue;
            Integer cur = meta.getPersistentDataContainer().get(KEY_ITEM_RF, PersistentDataType.INTEGER);
            if (cur == null) cur = 0;
            int maxItem = 2000;
            if (cur >= maxItem) continue;
            int add = Math.min(50, Math.min(maxItem - cur, m.rf));
            cur += add;
            m.rf -= add;
            meta.getPersistentDataContainer().set(KEY_ITEM_RF, PersistentDataType.INTEGER, cur);
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            // update RF lore line
            lore.removeIf(c -> c.toString().contains("RF:"));
            lore.add(Component.text("RF: " + cur + "/" + maxItem, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            s.setItemMeta(meta);
            m.slots[i] = s;
        }
    }

    // ---- Break ----

    /** Casse la machine et drop l'item + contenu. */
    private void breakMachine(Machine m, Player breaker) {
        if (m == null) return;
        String k = key(m.loc);
        machines.remove(k);
        removeVisual(m);
        if (m.loc.getBlock().getType() == Material.BARRIER
                || m.loc.getBlock().getType() == Material.STRUCTURE_VOID) {
            m.loc.getBlock().setType(Material.AIR);
        }
        Location dropAt = m.loc.clone().add(0.5, 0.3, 0.5);
        m.loc.getWorld().dropItemNaturally(dropAt, machineItem(m.type));
        for (ItemStack s : m.slots) {
            if (s != null) m.loc.getWorld().dropItemNaturally(dropAt, s.clone());
        }
        if (breaker != null) {
            breaker.playSound(m.loc, Sound.BLOCK_STONE_BREAK, 0.8f, 0.9f);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Machine m = machines.get(key(event.getBlock().getLocation()));
        if (m == null) return;
        event.setCancelled(true);
        event.setDropItems(false);
        breakMachine(m, event.getPlayer());
    }


    /** Clic gauche sur barrier / machine = casser (survie / creatif). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClickBreak(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return; // seulement clic sur bloc
        Player player = event.getPlayer();
        if (isMachineItem(player.getInventory().getItemInMainHand())) return;
        if (event.getClickedBlock() == null) return;

        Block b = event.getClickedBlock();
        Machine m = machines.get(key(b.getLocation()));
        if (m == null) return; // pas une machine = cassage vanilla

        event.setCancelled(true);
        breakMachine(m, player);
    }


    private void removeVisual(Machine m) {
        if (m.displayId != null) {
            var e = Bukkit.getEntity(m.displayId);
            if (e != null) e.remove();
        }
        if (m.interactionId != null) {
            var e = Bukkit.getEntity(m.interactionId);
            if (e != null) e.remove();
        }
    }

    private void rebind() {
        machines.clear();
        int fixed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (var e : world.getEntities()) {
                if (!(e instanceof ItemDisplay d)) continue;
                if (!d.getPersistentDataContainer().has(KEY_RF, PersistentDataType.BYTE)) continue;
                String typeName = d.getPersistentDataContainer().get(KEY_RF_TYPE, PersistentDataType.STRING);
                if (typeName == null) continue;
                Type type;
                try { type = Type.valueOf(typeName); } catch (Exception ex) { continue; }
                Location base = d.getLocation().clone();
                Block b = base.getBlock();
                if (b.getType() != Material.BARRIER) {
                    Block under = base.clone().add(0, -0.15, 0).getBlock();
                    if (under.getType() == Material.BARRIER) b = under;
                    else {
                        b = new Location(world, Math.floor(base.getX()), Math.floor(base.getY()), Math.floor(base.getZ())).getBlock();
                        if (b.getType().isAir() || b.getType() == Material.BARRIER) b.setType(Material.BARRIER);
                        else {
                            Block b2 = b.getRelative(0, -1, 0);
                            if (b2.getType().isAir()) { b2.setType(Material.BARRIER); b = b2; }
                            else b.setType(Material.BARRIER);
                        }
                    }
                }
                float yaw = 0f;
                try {
                    if (d.getPersistentDataContainer().has(KEY_RF_YAW, PersistentDataType.FLOAT)) {
                        yaw = d.getPersistentDataContainer().get(KEY_RF_YAW, PersistentDataType.FLOAT);
                    }
                } catch (Throwable ignored) {}
                Machine m = new Machine(type, b.getLocation(), yaw);
                m.displayId = d.getUniqueId();
                // recenter + transform
                Location dloc = b.getLocation().clone().add(0.5, 0.0, 0.5);
                d.teleport(dloc);
                try { d.setBillboard(Display.Billboard.FIXED); } catch (Throwable ignored) {}
                try { d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
                try {
                    float yOff = 0.5f;
                    float yawRad = (float) Math.toRadians(-yaw);
                    d.setTransformation(new Transformation(
                            new Vector3f(0f, yOff, 0f),
                            new AxisAngle4f(yawRad, 0f, 1f, 0f),
                            new Vector3f(1f, 1f, 1f),
                            new AxisAngle4f(0f, 0f, 1f, 0f)
                    ));
                    d.setRotation(yaw, 0f);
                } catch (Throwable ignored) {}
                // restore item if missing
                ItemStack cur = d.getItemStack();
                if (cur == null || cur.getType().isAir()) {
                    d.setItemStack(machineItem(type));
                    fixed++;
                }
                machines.put(key(b.getLocation()), m);
                // interaction
                boolean hasInter = false;
                for (var near : b.getWorld().getNearbyEntities(b.getLocation().clone().add(0.5, 0.5, 0.5), 0.7, 0.7, 0.7)) {
                    if (near instanceof Interaction inter && inter.getPersistentDataContainer().has(KEY_RF, PersistentDataType.BYTE)) {
                        m.interactionId = inter.getUniqueId();
                        hasInter = true;
                        break;
                    }
                }
                if (!hasInter) {
                    Location iloc = b.getLocation().clone().add(0.5, 0, 0.5);
                    Interaction inter = b.getWorld().spawn(iloc, Interaction.class);
                    inter.setPersistent(true);
                    inter.setInteractionWidth(0.95f);
                    inter.setInteractionHeight(type == Type.SOLAR ? 0.55f : 1.0f);
                    inter.getPersistentDataContainer().set(KEY_RF, PersistentDataType.BYTE, (byte) 1);
                    inter.getPersistentDataContainer().set(KEY_RF_TYPE, PersistentDataType.STRING, key(b.getLocation()));
                    m.interactionId = inter.getUniqueId();
                    fixed++;
                }
            }
        }
        plugin.getLogger().info("[RF] rebind " + machines.size() + " machine(s), fixed=" + fixed);
    }

 }
