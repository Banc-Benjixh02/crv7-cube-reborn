package fr.crv6.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socle à poupée, table, chaise, boîte aux lettres.
 */
public final class Crv6Furniture implements Listener {

    public static final int CMD_SOCLE = 12001;
    public static final int CMD_TABLE = 12002;
    public static final int CMD_CHAISE = 12003;
    public static final int CMD_MAILBOX = 12004;

    private final CRV6MenuPlugin plugin;
    private final NamespacedKey KEY_FURN = new NamespacedKey("crv6", "furniture");
    private final NamespacedKey KEY_FURN_TYPE = new NamespacedKey("crv6", "furniture_type");
    private final NamespacedKey KEY_SOCLE_DOLL = new NamespacedKey("crv6", "socle_doll");
    private final NamespacedKey KEY_MAIL_OWNER = new NamespacedKey("crv6", "mail_owner");
    private final NamespacedKey KEY_SEAT = new NamespacedKey("crv6", "seat");

    private final Map<String, UUID> displays = new ConcurrentHashMap<>(); // locKey -> display
    private final Map<String, UUID> socleDolls = new ConcurrentHashMap<>(); // socle loc -> doll display
    private final Map<String, UUID> holograms = new ConcurrentHashMap<>(); // socle loc -> text display
    private final Map<UUID, String> openMail = new ConcurrentHashMap<>(); // player -> mailbox locKey

    public Crv6Furniture(CRV6MenuPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskLater(plugin, this::rebind, 50L);
        Bukkit.getScheduler().runTaskLater(plugin, this::rebind, 120L);
        // particles on socles with dolls
        new BukkitRunnable() {
            @Override public void run() {
                for (String k : socleDolls.keySet()) {
                    Location loc = parseLoc(k);
                    if (loc == null || loc.getWorld() == null) continue;
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0.5, 1.2, 0.5), 1, 0.15, 0.1, 0.15, 0.001);
                }
            }
        }.runTaskTimer(plugin, 40L, 25L);
        plugin.getLogger().info("[Furniture] socle / table / chaise / mailbox actifs");
    }

    // ---------- items ----------
    public static ItemStack furnitureItem(int cmd, String name, NamedTextColor color, String... loreLines) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        for (String l : loreLines) {
            lore.add(Component.text(l, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack socleItem() {
        return furnitureItem(CMD_SOCLE, "Socle a poupee", NamedTextColor.LIGHT_PURPLE,
                "Place une poupee dessus (clic droit)", "Affiche le nom + particules");
    }
    public ItemStack tableItem() {
        return furnitureItem(CMD_TABLE, "Table", NamedTextColor.GOLD, "Meuble posable");
    }
    public ItemStack chaiseItem() {
        return furnitureItem(CMD_CHAISE, "Chaise", NamedTextColor.GOLD, "Assieds-toi (clic droit)");
    }
    public ItemStack mailboxItem() {
        return furnitureItem(CMD_MAILBOX, "Boite aux lettres", NamedTextColor.AQUA,
                "Clic droit: deposer / recuperer du courrier", "Liee a ton pseudo a la pose");
    }

    private int getCmd(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return 0;
        try {
            var c = stack.getItemMeta().getCustomModelDataComponent();
            var floats = c.getFloats();
            if (floats != null && !floats.isEmpty()) return Math.round(floats.get(0));
        } catch (Throwable ignored) {}
        try {
            if (stack.getItemMeta().hasCustomModelData()) return stack.getItemMeta().getCustomModelData();
        } catch (Throwable ignored) {}
        return 0;
    }

    private boolean isFurniture(ItemStack stack) {
        int c = getCmd(stack);
        return stack != null && stack.getType() == Material.PAPER && c >= CMD_SOCLE && c <= CMD_MAILBOX;
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Location parseLoc(String key) {
        try {
            String[] p = key.split(":");
            World w = Bukkit.getWorld(p[0]);
            if (w == null) return null;
            return new Location(w, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        } catch (Exception e) { return null; }
    }

    // ---------- place ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Sit on chair
        if (event.getClickedBlock().getType() == Material.BARRIER) {
            String k = locKey(event.getClickedBlock().getLocation());
            UUID id = displays.get(k);
            if (id != null) {
                Entity e = Bukkit.getEntity(id);
                if (e instanceof ItemDisplay disp) {
                    String type = disp.getPersistentDataContainer().get(KEY_FURN_TYPE, PersistentDataType.STRING);
                    if ("chaise".equals(type)) {
                        event.setCancelled(true);
                        sit(player, event.getClickedBlock().getLocation());
                        return;
                    }
                    if ("socle".equals(type)) {
                        // place doll on socle if holding doll
                        if (isDoll(hand)) {
                            event.setCancelled(true);
                            putDollOnSocle(player, event.getClickedBlock().getLocation(), hand);
                            return;
                        }
                    }
                    if ("mailbox".equals(type)) {
                        event.setCancelled(true);
                        openMailbox(player, event.getClickedBlock().getLocation(), disp);
                        return;
                    }
                }
            }
        }

        if (!isFurniture(hand)) return;
        event.setCancelled(true);
        Location place = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        if (!place.getBlock().getType().isAir()) {
            player.sendActionBar(Component.text("Pas la place", NamedTextColor.RED));
            return;
        }
        float yaw = Math.round(player.getLocation().getYaw() / 45f) * 45f;
        int cmd = getCmd(hand);
        String type = switch (cmd) {
            case CMD_SOCLE -> "socle";
            case CMD_TABLE -> "table";
            case CMD_CHAISE -> "chaise";
            case CMD_MAILBOX -> "mailbox";
            default -> null;
        };
        if (type == null) return;

        place.getBlock().setType(Material.BARRIER);
        spawnDisplay(place, type, cmd, yaw, player.getName());
        hand.setAmount(hand.getAmount() - 1);
        player.playSound(place, Sound.BLOCK_WOOD_PLACE, 0.8f, 1.1f);
        player.sendActionBar(Component.text(type.substring(0, 1).toUpperCase() + type.substring(1) + " place(e)", NamedTextColor.GREEN));
    }

    private void spawnDisplay(Location place, String type, int cmd, float yaw, String owner) {
        Location dloc = place.clone().add(0.5, 0.0, 0.5);
        ItemDisplay disp = place.getWorld().spawn(dloc, ItemDisplay.class);
        disp.setPersistent(true);
        disp.setInvulnerable(true);
        disp.setGravity(false);
        try { disp.setBillboard(Display.Billboard.FIXED); } catch (Throwable ignored) {}
        try { disp.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
        try {
            float yawRad = (float) Math.toRadians(-yaw);
            // +8px (0.5) pour sortir du sol — model origin au pied
            float yOff = 0.5f;
            disp.setTransformation(new Transformation(
                    new Vector3f(0f, yOff, 0f),
                    new AxisAngle4f(yawRad, 0f, 1f, 0f),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            ));
            disp.setRotation(yaw, 0f);
        } catch (Throwable ignored) {}
        ItemStack visual = furnitureItem(cmd, type, NamedTextColor.WHITE);
        disp.setItemStack(visual);
        disp.getPersistentDataContainer().set(KEY_FURN, PersistentDataType.BYTE, (byte) 1);
        disp.getPersistentDataContainer().set(KEY_FURN_TYPE, PersistentDataType.STRING, type);
        if ("mailbox".equals(type) && owner != null) {
            disp.getPersistentDataContainer().set(KEY_MAIL_OWNER, PersistentDataType.STRING, owner);
        }
        displays.put(locKey(place), disp.getUniqueId());
    }

    private boolean isDoll(ItemStack stack) {
        if (stack == null || stack.getType() != Material.ARMOR_STAND) return false;
        int c = getCmd(stack);
        return c == 3 || (c >= 10001 && c <= 11099);
    }

    // ---------- socle ----------
    private void putDollOnSocle(Player player, Location socleLoc, ItemStack doll) {
        String k = locKey(socleLoc);
        // remove previous doll
        UUID old = socleDolls.remove(k);
        if (old != null) {
            Entity e = Bukkit.getEntity(old);
            if (e != null) e.remove();
        }
        UUID oldH = holograms.remove(k);
        if (oldH != null) {
            Entity e = Bukkit.getEntity(oldH);
            if (e != null) e.remove();
        }

        Location dloc = socleLoc.clone().add(0.5, 1.15, 0.5);
        ItemDisplay dollDisp = socleLoc.getWorld().spawn(dloc, ItemDisplay.class);
        dollDisp.setPersistent(true);
        dollDisp.setInvulnerable(true);
        dollDisp.setGravity(false);
        try { dollDisp.setBillboard(Display.Billboard.FIXED); } catch (Throwable ignored) {}
        try { dollDisp.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
        try {
            dollDisp.setTransformation(new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 1f, 0f),
                    new Vector3f(0.4f, 0.4f, 0.4f),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            ));
        } catch (Throwable ignored) {}
        ItemStack visual = doll.clone();
        visual.setAmount(1);
        dollDisp.setItemStack(visual);
        dollDisp.getPersistentDataContainer().set(KEY_SOCLE_DOLL, PersistentDataType.BYTE, (byte) 1);
        socleDolls.put(k, dollDisp.getUniqueId());

        // name hologram
        String name = "Poupee";
        if (doll.hasItemMeta() && doll.getItemMeta().hasDisplayName()) {
            try {
                name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(doll.getItemMeta().displayName());
            } catch (Throwable t) {
                try { name = doll.getItemMeta().getDisplayName(); } catch (Throwable ignored) {}
            }
        }
        Location hloc = socleLoc.clone().add(0.5, 1.85, 0.5);
        TextDisplay text = socleLoc.getWorld().spawn(hloc, TextDisplay.class);
        text.text(Component.text(name, NamedTextColor.LIGHT_PURPLE));
        text.setPersistent(true);
        text.setBillboard(Display.Billboard.CENTER);
        try { text.setSeeThrough(true); } catch (Throwable ignored) {}
        try { text.setDefaultBackground(false); } catch (Throwable ignored) {}
        holograms.put(k, text.getUniqueId());

        doll.setAmount(doll.getAmount() - 1);
        player.playSound(socleLoc, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.7f, 1.3f);
        player.sendActionBar(Component.text("Poupee exposee: " + name, NamedTextColor.LIGHT_PURPLE));
    }

    // ---------- chaise sit (style coussin 26.3 : ArmorStand marker non-small + pose) ----------
    private void sit(Player player, Location chairLoc) {
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
        // Position type coussin : centre du bloc, yaw joueur, AS normal (pas small)
        Location sitLoc = chairLoc.clone().add(0.5, 0.0, 0.5);
        sitLoc.setYaw(player.getLocation().getYaw());
        sitLoc.setPitch(0f);

        ArmorStand seat = chairLoc.getWorld().spawn(sitLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setInvisible(true);
            as.setGravity(false);
            as.setMarker(true);
            as.setSmall(false); // hauteur d'assise type coussin vanilla
            as.setBasePlate(false);
            as.setArms(false);
            as.setPersistent(false);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setCollidable(false);
            as.setCustomNameVisible(false);
            as.setCanPickupItems(false);
            as.getPersistentDataContainer().set(KEY_SEAT, PersistentDataType.BYTE, (byte) 1);
            try {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
                }
            } catch (Throwable ignored) {}
        });

        // Monter le joueur (systeme vehicule = assise coussin)
        boolean mounted = seat.addPassenger(player);
        if (!mounted) {
            seat.remove();
            player.sendActionBar(Component.text("Impossible de s'asseoir", NamedTextColor.RED));
            return;
        }

        // Pose SITTING si dispo (Paper/MC recent)
        try {
            player.setPose(Pose.SITTING, true);
        } catch (Throwable ignored) {
            try {
                player.setPose(Pose.SITTING);
            } catch (Throwable ignored2) {}
        }

        player.sendActionBar(Component.text("Assis — sneak pour te lever", NamedTextColor.YELLOW));

        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline() || !seat.isValid()
                        || player.getVehicle() == null || !player.getVehicle().equals(seat)) {
                    if (seat.isValid()) seat.remove();
                    try { player.setPose(Pose.STANDING, true); } catch (Throwable ignored) {}
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    /** Supprime les display entities (+ seats) dans un rayon (blocs). */
    public int clearDisplays(Location center, double radius) {
        int removed = 0;
        if (center.getWorld() == null) return 0;
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            boolean drop = false;
            if (e instanceof ItemDisplay || e instanceof TextDisplay || e instanceof BlockDisplay) {
                drop = true;
            } else if (e instanceof Interaction inter) {
                // interactions meubles / gamelle / RF
                if (inter.getPersistentDataContainer().has(KEY_FURN, PersistentDataType.BYTE)
                        || inter.getPersistentDataContainer().getKeys().stream()
                            .anyMatch(k -> k.getNamespace().equals("crv6"))) {
                    drop = true;
                }
            } else if (e instanceof ArmorStand as) {
                if (as.getPersistentDataContainer().has(KEY_SEAT, PersistentDataType.BYTE)
                        || as.isMarker()) {
                    // seulement nos seats / markers proches, pas les vrais armor stands deco
                    if (as.getPersistentDataContainer().has(KEY_SEAT, PersistentDataType.BYTE)) {
                        drop = true;
                    }
                }
            }
            if (drop) {
                // nettoyer maps locales si c'est un meuble
                try {
                    if (e instanceof ItemDisplay d && d.getPersistentDataContainer().has(KEY_FURN, PersistentDataType.BYTE)) {
                        String k = locKey(e.getLocation().getBlock().getLocation());
                        displays.remove(k);
                        socleDolls.remove(k);
                        UUID h = holograms.remove(k);
                        if (h != null) {
                            Entity he = Bukkit.getEntity(h);
                            if (he != null) he.remove();
                        }
                        Block b = e.getLocation().getBlock();
                        if (b.getType() == Material.BARRIER) b.setType(Material.AIR);
                        Block under = e.getLocation().clone().add(0, -0.2, 0).getBlock();
                        if (under.getType() == Material.BARRIER) under.setType(Material.AIR);
                    }
                } catch (Throwable ignored) {}
                e.remove();
                removed++;
            }
        }
        return removed;
    }

    // ---------- mailbox ----------
    private Path mailDir() {
        Path d = plugin.getDataFolder().toPath().resolve("mail");
        try { Files.createDirectories(d); } catch (IOException ignored) {}
        return d;
    }

    private Path mailFile(String playerName) {
        return mailDir().resolve(playerName.toLowerCase(Locale.ROOT) + ".txt");
    }

    private void openMailbox(Player player, Location loc, ItemDisplay disp) {
        String owner = disp.getPersistentDataContainer().get(KEY_MAIL_OWNER, PersistentDataType.STRING);
        if (owner == null) owner = player.getName();
        boolean isOwner = owner.equalsIgnoreCase(player.getName());

        Inventory inv = Bukkit.createInventory(new MailHolder(locKey(loc), owner), 27,
                Component.text(isOwner ? "Ma boite — " + owner : "Envoyer a " + owner, NamedTextColor.AQUA));
        // filler edges
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.displayName(Component.text(" "));
        glass.setItemMeta(gm);
        for (int i = 0; i < 27; i++) if (i < 9 || i > 17) inv.setItem(i, glass.clone());

        if (isOwner) {
            // load pending items as simple material:amount:name lines - store base64-ish simple
            List<ItemStack> mail = loadMail(owner);
            int slot = 9;
            for (ItemStack it : mail) {
                if (slot > 17) break;
                inv.setItem(slot++, it);
            }
            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta im = info.getItemMeta();
            im.displayName(Component.text("Ton courrier", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            im.lore(List.of(
                    Component.text("Recupere les items (slots du milieu)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("Ferme l'inventaire pour valider", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            info.setItemMeta(im);
            inv.setItem(4, info);
        } else {
            ItemStack info = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta im = info.getItemMeta();
            im.displayName(Component.text("Envoyer a " + owner, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            im.lore(List.of(
                    Component.text("Depose des items au milieu", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("Ferme pour envoyer", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            info.setItemMeta(im);
            inv.setItem(4, info);
        }
        openMail.put(player.getUniqueId(), locKey(loc) + "|" + owner + "|" + (isOwner ? "in" : "out"));
        player.openInventory(inv);
        player.playSound(loc, Sound.BLOCK_CHEST_OPEN, 0.6f, 1.4f);
    }

    public static final class MailHolder implements InventoryHolder {
        public final String locKey, owner;
        public MailHolder(String locKey, String owner) { this.locKey = locKey; this.owner = owner; }
        @Override public Inventory getInventory() { return null; }
    }

    @EventHandler
    public void onMailClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MailHolder)) return;
        int slot = event.getRawSlot();
        // prevent taking glass
        if (slot < 9 || (slot > 17 && slot < 27)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMailClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MailHolder holder)) return;
        Player player = (Player) event.getPlayer();
        String meta = openMail.remove(player.getUniqueId());
        if (meta == null) return;
        String[] p = meta.split("\\|");
        boolean isOwner = p.length > 2 && "in".equals(p[2]);
        String owner = holder.owner;

        List<ItemStack> middle = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            ItemStack it = event.getInventory().getItem(i);
            if (it != null && !it.getType().isAir()) middle.add(it.clone());
        }

        if (isOwner) {
            // remaining items stay in mailbox
            saveMail(owner, middle);
            player.sendMessage(Component.text("Boite aux lettres mise a jour.", NamedTextColor.AQUA));
        } else {
            if (middle.isEmpty()) return;
            List<ItemStack> existing = loadMail(owner);
            existing.addAll(middle);
            saveMail(owner, existing);
            player.sendMessage(Component.text("Courrier envoye a " + owner + " !", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            Player target = Bukkit.getPlayerExact(owner);
            if (target != null && target.isOnline()) {
                target.sendMessage(Component.text("Tu as du courrier de " + player.getName() + " !", NamedTextColor.GOLD));
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            }
        }
    }

    private void saveMail(String playerName, List<ItemStack> items) {
        try {
            // serialize simply: material|amount|cmd
            StringBuilder sb = new StringBuilder();
            for (ItemStack it : items) {
                if (it == null) continue;
                int cmd = getCmd(it);
                sb.append(it.getType().name()).append("|").append(it.getAmount()).append("|").append(cmd).append("\n");
            }
            Files.writeString(mailFile(playerName), sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            plugin.getLogger().warning("mail save: " + e.getMessage());
        }
    }

    private List<ItemStack> loadMail(String playerName) {
        List<ItemStack> list = new ArrayList<>();
        Path f = mailFile(playerName);
        if (!Files.exists(f)) return list;
        try {
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|");
                if (p.length < 2) continue;
                Material mat = Material.matchMaterial(p[0]);
                if (mat == null) continue;
                int amount = Integer.parseInt(p[1]);
                int cmd = p.length > 2 ? Integer.parseInt(p[2]) : 0;
                ItemStack it = new ItemStack(mat, amount);
                if (cmd > 0) {
                    ItemMeta meta = it.getItemMeta();
                    try {
                        CustomModelDataComponent c = meta.getCustomModelDataComponent();
                        c.setFloats(List.of((float) cmd));
                        meta.setCustomModelDataComponent(c);
                    } catch (Throwable t) {
                        try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
                    }
                    it.setItemMeta(meta);
                }
                list.add(it);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("mail load: " + e.getMessage());
        }
        return list;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        List<ItemStack> mail = loadMail(event.getPlayer().getName());
        if (!mail.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                event.getPlayer().sendMessage(Component.text(
                        "Tu as " + mail.size() + " colis dans ta boite aux lettres !", NamedTextColor.GOLD));
            }, 40L);
        }
    }

    // ---------- break ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.BARRIER) return;
        String k = locKey(event.getClickedBlock().getLocation());
        UUID id = displays.get(k);
        if (id == null) return;
        Entity e = Bukkit.getEntity(id);
        if (!(e instanceof ItemDisplay disp)) return;
        if (!disp.getPersistentDataContainer().has(KEY_FURN, PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        breakFurniture(event.getPlayer(), event.getClickedBlock().getLocation(), disp);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.BARRIER) return;
        String k = locKey(event.getBlock().getLocation());
        UUID id = displays.get(k);
        if (id == null) return;
        Entity e = Bukkit.getEntity(id);
        if (!(e instanceof ItemDisplay disp)) return;
        if (!disp.getPersistentDataContainer().has(KEY_FURN, PersistentDataType.BYTE)) return;
        event.setDropItems(false);
        breakFurniture(event.getPlayer(), event.getBlock().getLocation(), disp);
    }

    private void breakFurniture(Player player, Location loc, ItemDisplay disp) {
        String k = locKey(loc);
        String type = disp.getPersistentDataContainer().get(KEY_FURN_TYPE, PersistentDataType.STRING);
        displays.remove(k);
        disp.remove();
        // remove doll on socle
        UUID dollId = socleDolls.remove(k);
        if (dollId != null) {
            Entity d = Bukkit.getEntity(dollId);
            if (d instanceof ItemDisplay dd) {
                ItemStack doll = dd.getItemStack();
                if (doll != null) loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), doll);
                dd.remove();
            }
        }
        UUID hId = holograms.remove(k);
        if (hId != null) {
            Entity h = Bukkit.getEntity(hId);
            if (h != null) h.remove();
        }
        loc.getBlock().setType(Material.AIR);
        ItemStack drop = switch (type == null ? "" : type) {
            case "socle" -> socleItem();
            case "table" -> tableItem();
            case "chaise" -> chaiseItem();
            case "mailbox" -> mailboxItem();
            default -> new ItemStack(Material.PAPER);
        };
        loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.3, 0.5), drop);
        if (player != null) {
            player.playSound(loc, Sound.BLOCK_WOOD_BREAK, 0.7f, 1f);
        }
    }

    private void rebind() {
        displays.clear();
        int n = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (!(e instanceof ItemDisplay d)) continue;
                if (!d.getPersistentDataContainer().has(KEY_FURN, PersistentDataType.BYTE)) continue;
                Location base = d.getLocation();
                Block b = base.getBlock();
                if (b.getType() != Material.BARRIER) {
                    Block under = base.clone().add(0, -0.2, 0).getBlock();
                    if (under.getType() == Material.BARRIER) b = under;
                    else {
                        b = new Location(world, Math.floor(base.getX()), Math.floor(base.getY()), Math.floor(base.getZ())).getBlock();
                        if (b.getType().isAir()) b.setType(Material.BARRIER);
                    }
                }
                displays.put(locKey(b.getLocation()), d.getUniqueId());
                n++;
            }
            // socle dolls
            for (Entity e : world.getEntities()) {
                if (!(e instanceof ItemDisplay d)) continue;
                if (!d.getPersistentDataContainer().has(KEY_SOCLE_DOLL, PersistentDataType.BYTE)) continue;
                Location base = d.getLocation().clone().add(0, -0.65, 0);
                String k = locKey(new Location(world, Math.floor(base.getX()), Math.floor(base.getY()), Math.floor(base.getZ())));
                socleDolls.put(k, d.getUniqueId());
            }
        }
        plugin.getLogger().info("[Furniture] rebind " + n + " meuble(s)");
    }
}
