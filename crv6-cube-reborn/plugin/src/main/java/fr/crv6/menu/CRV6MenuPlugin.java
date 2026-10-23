package fr.crv6.menu;

import org.joml.Vector3f;

import org.joml.AxisAngle4f;

import org.bukkit.util.Transformation;

import org.bukkit.Location;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionType;
import org.bukkit.inventory.meta.PotionMeta;
import java.util.Random;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.block.Block;
import org.bukkit.Tag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Item;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Display;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public final class CRV6MenuPlugin extends JavaPlugin implements Listener {

    private RfMachines rfMachines;
    private Crv6Furniture furniture;

    private static final Set<Integer> HIDE_NAMETAG_CMDS = Set.of(7, 15, 16, 18, 19, 22); // + maskass
    private static final Set<Integer> JUMP_CMDS = Set.of(12, 13, 14); // chat, petteux, kawa
    private static final Set<Integer> FREDDY_CMDS = Set.of(18, 19); // freddy + golden freddy: slow
    private static final int LUNETTE_CMD = 20;
    private static final int UTA_CMD = 21; // Casque de UTA
    private static final int MASKASS_CMD = 22; // Masque de Maskass
    private static final int RING_GAG_CMD = 23; // Ring Gag = baillon
    private static final int GAMELLE_CMD_VIDE = 1;
    private static final int GAMELLE_CMD_MAX = 5; // 1 vide .. 5 plein
    private static final NamespacedKey KEY_GAMELLE = new NamespacedKey("crv6", "gamelle");
    private static final NamespacedKey KEY_GAMELLE_LEVEL = new NamespacedKey("crv6", "gamelle_level");
    private static final NamespacedKey KEY_GAMELLE_DISPLAY = new NamespacedKey("crv6", "gamelle_display");
    private static final NamespacedKey KEY_GAMELLE_FOOD = new NamespacedKey("crv6", "gamelle_food");
    private static final int CHAT_EARS_CMD = 12; // Oreilles de chat
    private static final int FREDDY_CMD = 18;
    private static final int GOLDEN_FREDDY_CMD = 19;
    private NamespacedKey freddySlowKey;

    // zabuza = netherite_axe CMD 3 | desassembleur = netherite_pickaxe CMD 1
    private static final int ZABUZA_CMD = 3;
    private static final int BAILLON_CMD = 2; // billyhead -> Baillon
    private static final int GAS_MASK_CMD = 7; // mask -> Masque a gaz
    private static final int SCUBA_CMD = 11; // scaff -> Scaphandre
    private static final int TAZER_SELECT_CMD = 4;
    private static final int TAZER_CMD = 5;
    private static final int CHIANTOS_FERME_CMD = 5;
    private static final int CHIANTOS_OUVERT_CMD = 6;
    private static final double DWARF_SCALE = 0.5;
    private static final double DWARF_MINE_BONUS = 1.0; // +100% block break speed
    private static final Set<Integer> BOTTLE_CMDS = Set.of(10, 11, 12, 13);
    // max durability by bottle CMD (higher = more oxygen)
    private static final java.util.Map<Integer, Integer> BOTTLE_MAX = java.util.Map.of(
            10, 120,   // base - faible
            11, 400,   // I
            12, 1200,  // II
            13, 20000  // III - quasi infini
    );
    private static final int DESASSEMBLEUR_CMD = 1;
    private static final int PAILLE_CMD = 8; // hat luck 2
    private static final int PANNEAU_CMD = 4; // netherite axe 0 dmg + kb
    private static final int PETTEUX_CMD = 13;
    private static final int BANC_CMD = 1;
    private static final int CRYZO_BLOCK_CMD = 9;
    private static final int SANGLIER_CMD = 17;
    private static final double ZABUZA_DAMAGE_MULT = 1.10; // +10% vs netherite axe
    private static final double ZABUZA_SLOW_FACTOR = -0.20; // -20% speed

    // Life crystal: emerald CMD 2 (artefact_vie) + stick CMD 2 (cristal)
    private static final int JUMP_AMPLIFIER = 1;
    private static final double FALL_DAMAGE_DIVISOR = 2.0;
    private static final double LIFE_CRYSTAL_BONUS = 6.0; // +3 hearts

    private static final String TEAM_NAME = "crv6_hide_nt";
    private NamespacedKey lifeKey;
    private NamespacedKey bottleOxygenKey;
    private NamespacedKey tazerTargetKey;
    private NamespacedKey zabuzaKey;
    private Team hideTeam;

    private final Set<UUID> hidden = new HashSet<>();
    private final Set<UUID> jumping = new HashSet<>();
    private final Set<UUID> hasLifeBonus = new HashSet<>();
    private final Set<UUID> holdingZabuza = new HashSet<>();
    private final Set<UUID> wearingUta = new HashSet<>();
    private final Set<UUID> wearingBaillon = new HashSet<>();
    private final Set<UUID> wearingMaskass = new HashSet<>();
    // barrier loc key -> display entity uuid (fallback tracking)
    private final Map<String, UUID> gamelleAt = new ConcurrentHashMap<>();
    private int gamelleFreezeTick = 0;
    private final Set<UUID> wearingChatEars = new HashSet<>();
    private final Set<UUID> scubaAirBoosted = new HashSet<>();
    private final Set<UUID> stunned = new HashSet<>();
    private final Set<UUID> dwarfPlayers = new HashSet<>();
    private final Set<UUID> lunetteWearers = new HashSet<>();
    private final Set<UUID> freddyOverlayActive = new HashSet<>();
    private final UUID freddyOverlayUuid = UUID.nameUUIDFromBytes("crv6-freddy-pov".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private com.sun.net.httpserver.HttpServer overlayServer;
    private byte[] overlayPackBytes;
    private String overlaySha1 = "";
    private String overlayUrl = "";
    private NamespacedKey dwarfScaleKey;
    private NamespacedKey dwarfMineKey;
    private int oxygenTick = 0;

    @Override
    public void onEnable() {
        try {
            lifeKey = new NamespacedKey(this, "life_crystal_bonus");
            bottleOxygenKey = new NamespacedKey(this, "bottle_oxygen");
            tazerTargetKey = new NamespacedKey(this, "tazer_target");
            dwarfScaleKey = new NamespacedKey(this, "dwarf_scale");
            dwarfMineKey = new NamespacedKey(this, "dwarf_mine");
            freddySlowKey = new NamespacedKey(this, "freddy_slow");
            zabuzaKey = new NamespacedKey(this, "zabuza_slow");
            Bukkit.getPluginManager().registerEvents(this, this);
            setupTeam();
            Bukkit.getScheduler().runTaskTimer(this, this::tickEffects, 20L, 5L);
            registerShampooRecipes();
            registerFoodRecipes();
        } catch (Throwable t) {
            getLogger().severe("Init error: " + t.getMessage());
            t.printStackTrace();
        }
        // Commands
        if (getCommand("crv6") != null) {
            getCommand("crv6").setExecutor(this);
        } else {
            getLogger().severe("Command /crv6 missing from plugin.yml!");
        }
        if (getCommand("crfurniture") != null) {
            getCommand("crfurniture").setExecutor(this);
        }
        if (getCommand("crclear") != null) {
            getCommand("crclear").setExecutor(this);
        }
        if (getCommand("crreload") != null) {
            getCommand("crreload").setExecutor(this);
        }
        // VoiceChat: register now + retry later (SVC may load after us)
        saveDefaultConfig();
        setupFreddyOverlay();
        try { BaillonVoiceHook.tryRegister(this); }
        catch (Throwable t) { getLogger().info("VoiceChat optionnel: " + t.getMessage()); }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try { BaillonVoiceHook.tryRegister(this); }
            catch (Throwable t) { getLogger().info("VoiceChat retry: " + t.getMessage()); }
        }, 60L);
        
        // Rebind gamelles (ItemDisplay persistants)
        Bukkit.getScheduler().runTaskLater(this, this::rebindGamelles, 40L);
        rfMachines = new RfMachines(this);
        rfMachines.start();
        furniture = new Crv6Furniture(this);
        furniture.start();
        Crv6Players.load(this);
        // Cache async des icones têtes/poupées (évite le lag a l'ouverture du menu)
        
        // Variantes joueurs (resource JSON)
        loadPlayerVariants("benji_variants.json", "Benjixh02");
        loadPlayerVariants("bulbily_variants.json", "Bulbily");
        loadPlayerVariants("cryzopha_variants.json", "Cryzopha");
        loadPlayerVariants("kaulose_variants.json", "Kaulose");
        loadPlayerVariants("cecile_variants.json", "cecile71000");
        loadPlayerVariants("kama_variants.json", "Kama");
        loadPlayerVariants("lily_variants.json", "LillyChouquette");

        Bukkit.getScheduler().runTaskAsynchronously(this, this::prebuildTetesCache);
        // Rebind displays apres restart (poupees + gamelles)
        Bukkit.getScheduler().runTaskLater(this, this::rebindPoupes, 40L);
        Bukkit.getScheduler().runTaskLater(this, this::rebindGamelles, 45L);
        getLogger().info("CRV6 Menu loaded - " + ItemData.ALL.size() + " items | /crv6 /crreload");
    }

    @Override
    public void onDisable() {
        stopFreddyOverlay();
        onDisableBody();
    }

    private void onDisableBody() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            showNametag(p);
            clearJump(p);
            removeLifeBonus(p);
            clearZabuza(p);
            clearBaillon(p);
            wearingUta.remove(p.getUniqueId());
            wearingMaskass.remove(p.getUniqueId());
            wearingChatEars.remove(p.getUniqueId());
            clearScubaAir(p);
            clearDwarf(p);
        }
        hidden.clear();
        jumping.clear();
        hasLifeBonus.clear();
        holdingZabuza.clear();
        if (hideTeam != null) {
            try { hideTeam.unregister(); } catch (Exception ignored) {}
        }
    }



    private void tickEffects() {
        oxygenTick++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateNametag(player);
            updateJump(player);
            updateLifeCrystal(player);
            updateZabuza(player);
            updateBaillon(player);
            updateUta(player);
            updateMaskass(player);
            updateChatEars(player);
            updatePaille(player);
            updateFreddySlow(player);
            updateFreddyOverlay(player);
            updateLunette(player);
            updateOxygenGear(player);
            freezeGamelles();
            if (oxygenTick % 4 == 0) updateHeldTazer(player);
        }
    }



    private void setupTeam() {
        ensureHideTeam(Bukkit.getScoreboardManager().getMainScoreboard());
        for (Player p : Bukkit.getOnlinePlayers()) {
            ensureHideTeam(p.getScoreboard());
        }
        hideTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(TEAM_NAME);
    }

    private Team ensureHideTeam(Scoreboard board) {
        if (board == null) board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            try { team = board.registerNewTeam(TEAM_NAME); }
            catch (IllegalArgumentException e) { team = board.getTeam(TEAM_NAME); }
        }
        if (team != null) {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            try { team.setCanSeeFriendlyInvisibles(false); } catch (Throwable ignored) {}
            try {
                team.prefix(Component.empty());
                team.suffix(Component.empty());
            } catch (Throwable ignored) {}
        }
        return team;
    }

    private void updateNametag(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean shouldHide = isMaskHelmet(helm);
        boolean isHidden = hidden.contains(player.getUniqueId());
        if (shouldHide) {
            hideNametag(player);
            if (!isHidden) {
                getLogger().info("[Mask] HIDE " + player.getName()
                        + " type=" + helm.getType() + " cmd=" + getCmd(helm));
            }
        } else if (isHidden) {
            showNametag(player);
            getLogger().info("[Mask] SHOW " + player.getName());
        }
    }

    /** Lillybug (15) + Dark Mask (16) on carved_pumpkin */
    private boolean isMaskHelmet(ItemStack helm) {
        return isCustomHead(helm) && HIDE_NAMETAG_CMDS.contains(getCmd(helm));
    }

    private void hideNametag(Player player) {
        String name = player.getName();
        String uuid = player.getUniqueId().toString();

        // 1) Scoreboard teams on all boards (name + uuid entries)
        applyHideEntry(Bukkit.getScoreboardManager().getMainScoreboard(), name);
        applyHideEntry(Bukkit.getScoreboardManager().getMainScoreboard(), uuid);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                Scoreboard sb = viewer.getScoreboard();
                applyHideEntry(sb, name);
                applyHideEntry(sb, uuid);
            } catch (Throwable ignored) {}
        }

        // 2) TAB plugin soft-hook (very common nametag controller)
        tryHideViaTab(player, true);

        hidden.add(player.getUniqueId());
        hideNametagExternal(player);
    }

    private void applyHideEntry(Scoreboard board, String entry) {
        if (board == null || entry == null) return;
        Team team = ensureHideTeam(board);
        if (team == null) return;
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        for (Team t : new java.util.ArrayList<>(board.getTeams())) {
            if (t.equals(team)) continue;
            if (t.hasEntry(entry)) {
                try { t.removeEntry(entry); } catch (Throwable ignored) {}
            }
        }
        if (!team.hasEntry(entry)) {
            try { team.addEntry(entry); } catch (Throwable ignored) {}
        }
    }

    private void showNametag(Player player) {
        String name = player.getName();
        String uuid = player.getUniqueId().toString();
        removeHideEntry(Bukkit.getScoreboardManager().getMainScoreboard(), name);
        removeHideEntry(Bukkit.getScoreboardManager().getMainScoreboard(), uuid);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                removeHideEntry(viewer.getScoreboard(), name);
                removeHideEntry(viewer.getScoreboard(), uuid);
            } catch (Throwable ignored) {}
        }
        hidden.remove(player.getUniqueId());
        // Restore via TAB / NametagEdit (immediate + delayed retries)
        tryHideViaTab(player, false);
        showNametagExternal(player);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (hidden.contains(player.getUniqueId())) return; // re-masked
            tryHideViaTab(player, false);
            showNametagExternal(player);
        }, 5L);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (hidden.contains(player.getUniqueId())) return;
            tryHideViaTab(player, false);
            showNametagExternal(player);
        }, 20L);
    }


    // ---- TAB / NametagEdit soft integration ----
    private void hideNametagExternal(Player player) {
        // TAB API
        try {
            Class<?> tabApi = Class.forName("me.neznamy.tab.api.TabAPI");
            Object api = tabApi.getMethod("getInstance").invoke(null);
            if (api == null) return;
            Object tabPlayer = api.getClass().getMethod("getPlayer", java.util.UUID.class)
                    .invoke(api, player.getUniqueId());
            if (tabPlayer == null) return;
            Object nameTagManager = null;
            for (String mName : new String[]{"getNameTagManager", "getNametagManager"}) {
                try {
                    nameTagManager = api.getClass().getMethod(mName).invoke(api);
                    if (nameTagManager != null) break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (nameTagManager != null) {
                for (String mName : new String[]{"hideNameTag", "hideNametag", "hideNametag"}) {
                    try {
                        nameTagManager.getClass().getMethod(mName, tabPlayer.getClass().getInterfaces().length > 0
                                ? Class.forName("me.neznamy.tab.api.TabPlayer")
                                : tabPlayer.getClass()).invoke(nameTagManager, tabPlayer);
                        return;
                    } catch (Throwable t) {
                        try {
                            // try with Object / TabPlayer interface
                            Class<?> tabPlayerCl = Class.forName("me.neznamy.tab.api.TabPlayer");
                            nameTagManager.getClass().getMethod(mName, tabPlayerCl).invoke(nameTagManager, tabPlayer);
                            return;
                        } catch (Throwable ignored) {}
                    }
                }
            }
            // Alternative: TabPlayer.hideNametag() for each viewer
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(player)) continue;
                try {
                    Object tabViewer = api.getClass().getMethod("getPlayer", java.util.UUID.class)
                            .invoke(api, viewer.getUniqueId());
                    if (tabViewer == null) continue;
                    // team-based via setCollisionRule / nametag visibility on Tab
                } catch (Throwable ignored) {}
            }
        } catch (ClassNotFoundException e) {
            // TAB not installed
        } catch (Throwable t) {
            getLogger().fine("[Mask] TAB hide: " + t.getMessage());
        }
        // NametagEdit
        try {
            Class<?> nte = Class.forName("com.nametagedit.plugin.NametagEdit");
            Object plugin = nte.getMethod("getApi").invoke(null);
            // api.clearNametag(player) then set empty
            plugin.getClass().getMethod("clearNametag", Player.class).invoke(plugin, player);
            try {
                plugin.getClass().getMethod("setNametag", Player.class, String.class, String.class)
                        .invoke(plugin, player, "", "");
            } catch (Throwable ignored) {}
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            getLogger().fine("[Mask] NTE: " + t.getMessage());
        }
    }

    private void showNametagExternal(Player player) {
        try {
            Class<?> tabApi = Class.forName("me.neznamy.tab.api.TabAPI");
            Object api = tabApi.getMethod("getInstance").invoke(null);
            if (api == null) return;
            Object tabPlayer = api.getClass().getMethod("getPlayer", java.util.UUID.class)
                    .invoke(api, player.getUniqueId());
            if (tabPlayer == null) return;
            Object nameTagManager = null;
            for (String mName : new String[]{"getNameTagManager", "getNametagManager"}) {
                try {
                    nameTagManager = api.getClass().getMethod(mName).invoke(api);
                    if (nameTagManager != null) break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (nameTagManager != null) {
                Class<?> tabPlayerCl = Class.forName("me.neznamy.tab.api.TabPlayer");
                for (String mName : new String[]{"showNameTag", "showNametag"}) {
                    try {
                        nameTagManager.getClass().getMethod(mName, tabPlayerCl).invoke(nameTagManager, tabPlayer);
                        return;
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        try {
            Class<?> nte = Class.forName("com.nametagedit.plugin.NametagEdit");
            Object plugin = nte.getMethod("getApi").invoke(null);
            plugin.getClass().getMethod("reloadNametag", Player.class).invoke(plugin, player);
        } catch (Throwable ignored) {}
    }


    private void removeHideEntry(Scoreboard board, String entry) {
        if (board == null) return;
        try {
            Team t = board.getTeam(TEAM_NAME);
            if (t != null) t.removeEntry(entry);
        } catch (Throwable ignored) {}
    }

    private void tryHideViaTab(Player player, boolean hide) {
        try {
            Class<?> tabApi = Class.forName("me.neznamy.tab.api.TabAPI");
            Object api = tabApi.getMethod("getInstance").invoke(null);
            if (api == null) return;
            Object nametagMgr = api.getClass().getMethod("getNametagManager").invoke(api);
            if (nametagMgr == null) return;
            Object tabPlayer = api.getClass().getMethod("getPlayer", java.util.UUID.class)
                    .invoke(api, player.getUniqueId());
            if (tabPlayer == null) return;
            if (hide) {
                try {
                    nametagMgr.getClass().getMethod("hideNametag", tabPlayer.getClass().getInterfaces().length > 0
                            ? Class.forName("me.neznamy.tab.api.TabPlayer") : tabPlayer.getClass())
                            .invoke(nametagMgr, tabPlayer);
                } catch (Throwable t1) {
                    for (Method m : nametagMgr.getClass().getMethods()) {
                        if (m.getName().toLowerCase().contains("hide") && m.getParameterCount() == 1) {
                            m.invoke(nametagMgr, tabPlayer);
                            break;
                        }
                    }
                }
            } else {
                boolean shown = false;
                Class<?> tabPlayerCl = Class.forName("me.neznamy.tab.api.TabPlayer");
                for (String mName : new String[]{"showNameTag", "showNametag", "showNameTagVisibility"}) {
                    try {
                        nametagMgr.getClass().getMethod(mName, tabPlayerCl).invoke(nametagMgr, tabPlayer);
                        shown = true;
                        break;
                    } catch (Throwable ignored) {}
                }
                if (!shown) {
                    for (Method m : nametagMgr.getClass().getMethods()) {
                        String n = m.getName().toLowerCase();
                        if (m.getParameterCount() == 1 && n.contains("show") && n.contains("name")) {
                            try {
                                m.invoke(nametagMgr, tabPlayer);
                                shown = true;
                                break;
                            } catch (Throwable ignored) {}
                        }
                    }
                }
                // Last resort: toggle visibility boolean if API has setNameTagVisible
                if (!shown) {
                    for (Method m : nametagMgr.getClass().getMethods()) {
                        if (m.getParameterCount() == 2) {
                            String n = m.getName().toLowerCase();
                            if (n.contains("nametag") && (n.contains("visible") || n.contains("visibility"))) {
                                try {
                                    Class<?>[] pts = m.getParameterTypes();
                                    if (pts[1] == boolean.class || pts[1] == Boolean.class) {
                                        m.invoke(nametagMgr, tabPlayer, true);
                                        break;
                                    }
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // TAB not installed - ok
        } catch (Throwable t) {
            // ignore
        }
    }


    // ---- life crystal (+3 hearts in inventory) ----

    private boolean isLifeCrystal(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        int cmd = getCmd(stack);
        if (stack.getType() == Material.EMERALD && cmd == 2) return true;
        if (stack.getType() == Material.STICK && cmd == 2) return true;
        return false;
    }

    private boolean hasLifeCrystal(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (isLifeCrystal(stack)) return true;
        }
        return isLifeCrystal(player.getInventory().getItemInOffHand());
    }

    private void updateLifeCrystal(Player player) {
        boolean should = hasLifeCrystal(player);
        boolean has = hasLifeBonus.contains(player.getUniqueId());
        if (should && !has) applyLifeBonus(player);
        else if (!should && has) removeLifeBonus(player);
    }

    private void applyLifeBonus(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        for (AttributeModifier mod : List.copyOf(maxHealth.getModifiers())) {
            if (mod.getKey().equals(lifeKey)) maxHealth.removeModifier(mod);
        }
        AttributeModifier mod = new AttributeModifier(
                lifeKey, LIFE_CRYSTAL_BONUS,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY
        );
        maxHealth.addModifier(mod);
        hasLifeBonus.add(player.getUniqueId());
    }

    private void removeLifeBonus(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            for (AttributeModifier mod : List.copyOf(maxHealth.getModifiers())) {
                if (mod.getKey().equals(lifeKey)) maxHealth.removeModifier(mod);
            }
            double max = maxHealth.getValue();
            if (player.getHealth() > max) player.setHealth(max);
        }
        hasLifeBonus.remove(player.getUniqueId());
    }

    private void updateJump(Player player) {
        boolean should = isCmdHelmet(player.getInventory().getHelmet(), JUMP_CMDS);
        boolean has = jumping.contains(player.getUniqueId());
        if (should) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP_BOOST, 40, JUMP_AMPLIFIER, true, false, false));
            if (!has) jumping.add(player.getUniqueId());
        } else if (has) {
            clearJump(player);
        }
    }

    private void clearJump(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        jumping.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!jumping.contains(player.getUniqueId())
                && !isCmdHelmet(player.getInventory().getHelmet(), JUMP_CMDS)) return;
        event.setDamage(Math.max(0, event.getDamage() / FALL_DAMAGE_DIVISOR));
    }

    // ---- helpers ----

    private boolean isCmdHelmet(ItemStack helmet, Set<Integer> cmds) {
        return isCustomHead(helmet) && cmds.contains(getCmd(helmet));
    }

    private int getCmd(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return -1;
        // 1) Paper DataComponentTypes.CUSTOM_MODEL_DATA
        try {
            Class<?> types = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
            Object cmdType = types.getField("CUSTOM_MODEL_DATA").get(null);
            Method getData = ItemStack.class.getMethod("getData", Class.forName("io.papermc.paper.datacomponent.DataComponentType"));
            // getData takes DataComponentType<?>
            for (Method m : ItemStack.class.getMethods()) {
                if (!m.getName().equals("getData") || m.getParameterCount() != 1) continue;
                Object data = m.invoke(stack, cmdType);
                if (data == null) continue;
                // CustomModelData component: floats() or getFloats()
                for (String mn : new String[]{"floats", "getFloats"}) {
                    try {
                        Method fm = data.getClass().getMethod(mn);
                        Object fl = fm.invoke(data);
                        if (fl instanceof java.util.List<?> list && !list.isEmpty()) {
                            Object v = list.get(0);
                            if (v instanceof Number n) return Math.round(n.floatValue());
                        }
                    } catch (NoSuchMethodException ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        // 2) ItemMeta component
        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasCustomModelDataComponent()) {
                List<Float> floats = meta.getCustomModelDataComponent().getFloats();
                if (floats != null && !floats.isEmpty()) return Math.round(floats.get(0));
            }
        } catch (Throwable ignored) {}
        // 3) Legacy
        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) return meta.getCustomModelData();
        } catch (Throwable ignored) {}
        return -1;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Player joined = event.getPlayer();
            ensureHideTeam(joined.getScoreboard());
            for (UUID id : new java.util.HashSet<>(hidden)) {
                Player masked = Bukkit.getPlayer(id);
                if (masked != null) {
                    Team t = ensureHideTeam(joined.getScoreboard());
                    if (t != null && !t.hasEntry(masked.getName())) t.addEntry(masked.getName());
                }
            }
            updateNametag(joined);
            updateJump(joined);
            updateLifeCrystal(joined);
            updateZabuza(joined);
            updateBaillon(joined);
            clearDwarf(joined);
        }, 10L);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        if (!hidden.contains(event.getPlayer().getUniqueId())) return;
        // While masked, keep them sneaking (nametag hide like vanilla sneak)
        if (!event.isSneaking()) {
            event.setCancelled(true);
            event.getPlayer().setSneaking(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        showNametag(event.getPlayer());
        clearJump(event.getPlayer());
        removeLifeBonus(event.getPlayer());
        clearZabuza(event.getPlayer());
        clearBaillon(event.getPlayer());
        clearScubaAir(event.getPlayer());
        stunned.remove(event.getPlayer().getUniqueId());
        clearDwarf(event.getPlayer());
    }


    
    
    
    // ---- Oxygen bottles + Gas mask + Scuba (scaphandre) ----

    private boolean isBottle(ItemStack stack) {
        return stack != null && stack.getType() == Material.CARROT_ON_A_STICK
                && BOTTLE_CMDS.contains(getCmd(stack));
    }

    private boolean isGasMask(ItemStack stack) {
        return isCustomHeadCmd(stack, GAS_MASK_CMD);
    }

    private boolean isScuba(ItemStack stack) {
        return isCustomHeadCmd(stack, SCUBA_CMD);
    }

    private void applyBottleDurability(ItemStack stack, int cmd, boolean keepDamage) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        int max = BOTTLE_MAX.getOrDefault(cmd, 100);
        int oxygen = max;
        if (keepDamage && meta.getPersistentDataContainer().has(bottleOxygenKey, PersistentDataType.INTEGER)) {
            oxygen = meta.getPersistentDataContainer().getOrDefault(bottleOxygenKey, PersistentDataType.INTEGER, max);
        }
        meta.getPersistentDataContainer().set(bottleOxygenKey, PersistentDataType.INTEGER, oxygen);
        // visual bar on vanilla durability (carrot_on_a_stick max 25)
        if (meta instanceof Damageable dmg) {
            short vanMax = stack.getType().getMaxDurability();
            if (vanMax <= 0) vanMax = 25;
            int visualDmg = (int) Math.round((1.0 - (oxygen / (double) max)) * vanMax);
            visualDmg = Math.max(0, Math.min(vanMax - 1, visualDmg)); // keep item from breaking if ox>0
            if (oxygen <= 0) visualDmg = vanMax;
            dmg.setDamage(visualDmg);
        }
        stack.setItemMeta(meta);
    }

    private int getBottleOxygen(ItemStack stack) {
        if (!isBottle(stack)) return 0;
        int cmd = getCmd(stack);
        int max = BOTTLE_MAX.getOrDefault(cmd, 100);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0;
        if (meta.getPersistentDataContainer().has(bottleOxygenKey, PersistentDataType.INTEGER)) {
            return Math.max(0, meta.getPersistentDataContainer().getOrDefault(bottleOxygenKey, PersistentDataType.INTEGER, 0));
        }
        // legacy: full tank
        return max;
    }

    private void setBottleOxygen(ItemStack stack, int oxygen) {
        if (!isBottle(stack)) return;
        int cmd = getCmd(stack);
        int max = BOTTLE_MAX.getOrDefault(cmd, 100);
        oxygen = Math.max(0, Math.min(max, oxygen));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(bottleOxygenKey, PersistentDataType.INTEGER, oxygen);
        if (meta instanceof Damageable dmg) {
            short vanMax = stack.getType().getMaxDurability();
            if (vanMax <= 0) vanMax = 25;
            int visualDmg = oxygen <= 0 ? vanMax : (int) Math.round((1.0 - (oxygen / (double) max)) * vanMax);
            visualDmg = Math.max(0, Math.min(vanMax, visualDmg));
            dmg.setDamage(visualDmg);
        }
        // lore
        java.util.List<Component> lore = meta.lore();
        if (lore == null) lore = new java.util.ArrayList<>();
        java.util.List<Component> newLore = new java.util.ArrayList<>();
        boolean replaced = false;
        for (Component c : lore) {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c);
            if (plain.startsWith("Oxygene:")) {
                newLore.add(Component.text("Oxygene: " + oxygen + "/" + max, NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                replaced = true;
            } else newLore.add(c);
        }
        if (!replaced) {
            newLore.add(0, Component.text("Oxygene: " + oxygen + "/" + max, NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(newLore);
        stack.setItemMeta(meta);
    }

    /** Consume `amount` oxygen from best bottle in inventory. Returns true if consumed. */
    private boolean consumeOxygen(Player player, int amount) {
        ItemStack best = null;
        int bestSlot = -1;
        int bestOxygen = 0;
        int bestCmd = -1;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack s = contents[i];
            if (!isBottle(s)) continue;
            int ox = getBottleOxygen(s);
            if (ox <= 0) continue;
            int cmd = getCmd(s);
            // Prefer higher tier if both have oxygen, else any with oxygen
            if (ox > 0 && (best == null || cmd > bestCmd || (cmd == bestCmd && ox > bestOxygen))) {
                best = s;
                bestSlot = i;
                bestOxygen = ox;
                bestCmd = cmd;
            }
        }
        // also offhand
        ItemStack off = player.getInventory().getItemInOffHand();
        if (isBottle(off) && getBottleOxygen(off) > 0) {
            int cmd = getCmd(off);
            int ox = getBottleOxygen(off);
            if (best == null || cmd > bestCmd) {
                best = off;
                bestSlot = -2;
                bestOxygen = ox;
                bestCmd = cmd;
            }
        }
        if (best == null || bestOxygen <= 0) return false;

        int left = Math.max(0, bestOxygen - amount);
        setBottleOxygen(best, left);
        if (bestSlot == -2) player.getInventory().setItemInOffHand(best);
        else if (bestSlot >= 0) player.getInventory().setItem(bestSlot, best);

        if (left <= 0) {
            player.sendMessage(Component.text("Bouteille O2 vide !", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.2f);
        }
        return true;
    }

    private void updateOxygenGear(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean gas = isGasMask(helm);
        boolean scuba = isScuba(helm);

        // Gas mask: poison immunity while oxygen available
        if (gas) {
            // consume 1 oxygen every 2 seconds (every 4 ticks of 5-tick timer ≈ every ~2s at 5L period)
            // tickEffects runs every 5 ticks; use counter
            if (oxygenTick % 4 == 0) {
                if (consumeOxygen(player, 1)) {
                    player.removePotionEffect(PotionEffectType.POISON);
                    player.removePotionEffect(PotionEffectType.WITHER);
                } else {
                    // no oxygen - no immunity
                    if (oxygenTick % 20 == 0) {
                        player.sendActionBar(Component.text("Masque a gaz: plus d'O2 !", NamedTextColor.RED));
                    }
                }
            } else {
                // still cleanse poison if we had oxygen this cycle - check any bottle with O2
                if (hasAnyOxygen(player)) {
                    player.removePotionEffect(PotionEffectType.POISON);
                }
            }
        }

        // Scuba / scaphandre
        if (scuba) {
            boolean inWater = player.getRemainingAir() < player.getMaximumAir()
                    || player.isInWater()
                    || player.getEyeLocation().getBlock().isLiquid();
            boolean hasO2 = hasAnyOxygen(player);
            if (hasO2 && (player.isInWater() || player.getEyeLocation().getBlock().getType() == Material.WATER
                    || player.getEyeLocation().getBlock().getType() == Material.BUBBLE_COLUMN)) {
                // full air + night vision (better see)
                if (oxygenTick % 2 == 0) { // consume slower underwater
                    consumeOxygen(player, 1);
                }
                player.setRemainingAir(player.getMaximumAir());
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION, 40, 0, false, false, true));
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WATER_BREATHING, 40, 0, false, false, true));
                // clear air boost flag if was using bottles
                clearScubaAir(player);
            } else if (!hasO2) {
                // +50% air reserve without bottles
                if (!scubaAirBoosted.contains(player.getUniqueId())) {
                    int base = 300; // vanilla
                    try {
                        // keep current remaining ratio
                        int curMax = player.getMaximumAir();
                        int cur = player.getRemainingAir();
                        player.setMaximumAir((int) (base * 1.5));
                        player.setRemainingAir(Math.min(player.getMaximumAir(), (int) (cur * 1.5)));
                    } catch (Throwable ignored) {
                        player.setMaximumAir(450);
                    }
                    scubaAirBoosted.add(player.getUniqueId());
                }
            }
        } else {
            clearScubaAir(player);
        }
    }

    private boolean hasAnyOxygen(Player player) {
        for (ItemStack s : player.getInventory().getContents()) {
            if (isBottle(s) && getBottleOxygen(s) > 0) return true;
        }
        return isBottle(player.getInventory().getItemInOffHand())
                && getBottleOxygen(player.getInventory().getItemInOffHand()) > 0;
    }

    private void clearScubaAir(Player player) {
        if (scubaAirBoosted.remove(player.getUniqueId())) {
            try {
                player.setMaximumAir(300);
                if (player.getRemainingAir() > 300) player.setRemainingAir(300);
            } catch (Throwable ignored) {}
        }
    }

    
    
    // ---- Chiantos (cookie CMD 5 ferme / 6 ouvert) ----

    private boolean isChiantosFerme(ItemStack stack) {
        return stack != null && stack.getType() == Material.COOKIE && getCmd(stack) == CHIANTOS_FERME_CMD;
    }

    private boolean isChiantosOuvert(ItemStack stack) {
        return stack != null && stack.getType() == Material.COOKIE && getCmd(stack) == CHIANTOS_OUVERT_CMD;
    }



    /** Spreadsheet lores (descriptions corrected) + reference heads. Returns null if no catalog entry. */
    private java.util.List<Component> catalogLore(String material, int cmd) {
        String key = material + ":" + cmd;
        String desc1 = null, desc2 = null, author = null, effect = null;
        // --- Citrouilles / heads ---
        if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == PETTEUX_CMD) {
            desc1 = "Une minute de silence pour un brave chat,";
            desc2 = "un ami present aupres de notre cher Bleuvette.";
            author = "Bleuvette";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == BAILLON_CMD) {
            desc1 = "Tres utile quand un de tes amis parle trop !";
            author = "Benjixh02";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == BANC_CMD) {
            desc1 = "Une couronne qui fait de toi un roi ou une reine.";
            author = "Benjixh02";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == CRYZO_BLOCK_CMD) {
            desc1 = "Afin de ne jamais le quitter,";
            desc2 = "tu peux utiliser ce block sur ta tete.";
            author = "Cryzopha";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == SANGLIER_CMD) {
            desc1 = "Parfait pour la CHARGE DU SANGLIER !";
            author = "Skyrrio";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == PAILLE_CMD) {
            desc1 = "Ancien chapeau d'un pecheur beaucoup trop chill.";
            author = "FiLoka";
            effect = "Effet: Chance II (equipe sur la tete)";
        } else if ("netherite_pickaxe".equals(material) && cmd == DESASSEMBLEUR_CMD) {
            desc1 = "La pioche la plus efficace";
            desc2 = "de tous les multivers confondus.";
            author = "Kaulose";
            effect = "Effet: one-shot pierre & minerais";
        } else if ("netherite_axe".equals(material) && cmd == PANNEAU_CMD) {
            desc1 = "Tu veux montrer ton mecontentement ?";
            desc2 = "Alors cet item est pour toi !";
            author = "Noleler";
            effect = "Effet: 0 degat, knockback tres puissant";
        } else if ("cooked_chicken".equals(material) && cmd == 2) {
            desc1 = "Tres efficace, permet meme de decolorer la laine.";
            author = "Pgalladium";
        } else if ("cooked_chicken".equals(material) && cmd == 1) {
            desc1 = "Circulez, y'a rien a voir, mais tout a manger !";
            author = "Bleuvette";
        } else if ("snowball".equals(material) && cmd == 2) {
            desc1 = "Un projectile tres utile";
            desc2 = "quand tu perds a Mario Kart.";
            author = "Pgalladium";
        } else if ("snowball".equals(material) && cmd == 1) {
            desc1 = "Projectile aussi utile que l'ecran,";
            desc2 = "mais beaucoup plus salissant.";
            author = "Cryzopha";
        } else if ("carved_pumpkin".equals(material) && cmd == FREDDY_CMD) {
            desc1 = "Il est l'heure de fermer...";
            desc2 = "Freddy Fazbear veille dans l'ombre.";
            effect = "Effet: pseudo invisible + ralentissement";
        } else if ("carved_pumpkin".equals(material) && cmd == GOLDEN_FREDDY_CMD) {
            desc1 = "It's me.";
            desc2 = "Une presence doree, figee, qui ne devrait pas etre la.";
            effect = "Effet: pseudo invisible + ralentissement";
        } else if (("carved_pumpkin".equals(material) || "leather_helmet".equals(material)) && cmd == LUNETTE_CMD) {
            desc1 = "Des lunettes de nuit un peu trop efficaces.";
            desc2 = "Attention aux maux de tete en les retirant.";
            effect = "Effet: vision nocturne (retire = trouble)";
        } else if ("carved_pumpkin".equals(material) && cmd == UTA_CMD) {
            desc1 = "Le casque d'Uta, diva d'Elegia (One Piece).";
            desc2 = "Sa voix pouvait enchanter le monde entier...";
            effect = "Effet: mute VoiceChat (micro)";
            author = "Benjixh02";
        } else if ("carved_pumpkin".equals(material) && cmd == MASKASS_CMD) {
            desc1 = "Le masque emblematique de Maskass.";
            desc2 = "Personne ne sait vraiment qui se cache derriere...";
            effect = "Effet: chat baillonne + pseudo invisible";
        } else if ("carved_pumpkin".equals(material) && cmd == RING_GAG_CMD) {
            desc1 = "Un baillon a anneau, discret mais efficace.";
            desc2 = "Difficile de parler clairement avec ca.";
            effect = "Effet: chat baillonne + micro mute";
        } else if ("bowl".equals(material) && cmd == GAMELLE_CMD_VIDE) {
            desc1 = "Une gamelle vide. Place-la, puis remplis-la.";
            desc2 = "Chair, glow berry, gapple, lait ou potions (couleurs).";
            effect = "Lait = clear effets | Potions = effets colores";
        } else if ("carved_pumpkin".equals(material) && cmd == CHAT_EARS_CMD) {
            desc1 = "Des oreilles de chat toutes douces.";
            desc2 = "Nya.";
            effect = "Effet: saute plus haut + Nya. en chat";
        } else {
            return null;
        }
        java.util.List<Component> lore = new java.util.ArrayList<>();
        if (desc1 != null) lore.add(Component.text(desc1, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (desc2 != null) lore.add(Component.text(desc2, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (effect != null) {
            lore.add(Component.empty());
            lore.add(Component.text(effect, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        }
        if (author != null) {
            lore.add(Component.empty());
            lore.add(referenceLine(author));
        }
        return lore;
    }

    private Component playerHeadByName(String name) {
        try {
            Class<?> oc = Class.forName("net.kyori.adventure.text.object.ObjectContents");
            Object contents = oc.getMethod("playerHead", String.class).invoke(null, name);
            Component head = (Component) Component.class.getMethod("object",
                    Class.forName("net.kyori.adventure.text.object.ObjectContents"))
                    .invoke(null, contents);
            return head.style(net.kyori.adventure.text.format.Style.empty())
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, false);
        } catch (Throwable t) {
            return Component.text("", NamedTextColor.WHITE);
        }
    }

    private Component referenceLine(String author) {
        return Component.text()
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(playerHeadByName(author))
                .append(Component.text(" Item reference de " + author, NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, true))
                .build();
    }

    private Component kromnirHead() {
        return playerHeadByName("Kromnir");
    }

    private java.util.List<Component> buildChiantosLore(int cmd) {
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("Une friandise a base de champignon", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("des mines enrichie en poudre de granite", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        if (cmd == CHIANTOS_FERME_CMD) {
            lore.add(Component.text("Clic droit: ouvrir", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Manger: forme nain", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Taille /2 + minage rapide", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Faim + saturation max", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text()
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(kromnirHead())
                .append(Component.text(" Item reference de Kromnir", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, true))
                .build());
        return lore;
    }

    private ItemStack makeChiantos(int cmd, String name) {
        ItemStack stack = new ItemStack(Material.COOKIE);
        ItemMeta meta = stack.getItemMeta();
        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of((float) cmd));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        NamedTextColor color = cmd == CHIANTOS_OUVERT_CMD ? NamedTextColor.GREEN : NamedTextColor.GOLD;
        meta.displayName(Component.text(name, color)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        java.util.List<Component> lore = buildChiantosLore(cmd);
        meta.lore(lore);
        // Food component only on open
        if (cmd == CHIANTOS_OUVERT_CMD) {
            try {
                FoodComponent fc = meta.getFood();
                fc.setNutrition(20);
                fc.setSaturation(20f);
                fc.setCanAlwaysEat(true);
                meta.setFood(fc);
            } catch (Throwable ignored) {}
        }
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChiantosOpen(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isChiantosFerme(hand)) return;
        event.setCancelled(true);
        ItemStack open = makeChiantos(CHIANTOS_OUVERT_CMD, "Chiantos ouvert");
        // Replace one item in stack
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
            java.util.Map<Integer, ItemStack> left = player.getInventory().addItem(open);
            for (ItemStack s : left.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), s);
            }
        } else {
            player.getInventory().setItemInMainHand(open);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 1f, 1.4f);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.8f, 1.6f);
        player.sendMessage(Component.text("Chiantos ouvert !", NamedTextColor.GREEN));
    }

    private void applyDwarf(Player player) {
        // Re-eating resets then re-applies
        clearDwarf(player);
        dwarfPlayers.add(player.getUniqueId());

        // Scale 0.5 (whole body in vanilla; head follows scale - no separate head attr)
        try {
            AttributeInstance scale = player.getAttribute(Attribute.SCALE);
            if (scale != null) {
                for (AttributeModifier mod : List.copyOf(scale.getModifiers())) {
                    if (mod.getKey().equals(dwarfScaleKey)) scale.removeModifier(mod);
                }
                scale.addModifier(new AttributeModifier(
                        dwarfScaleKey, DWARF_SCALE - 1.0, // base 1.0 + (-0.5) = 0.5
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY
                ));
            }
        } catch (Throwable t) {
            getLogger().warning("SCALE attribute unavailable: " + t.getMessage());
        }

        // Mining speed boost
        try {
            AttributeInstance mine = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
            if (mine != null) {
                for (AttributeModifier mod : List.copyOf(mine.getModifiers())) {
                    if (mod.getKey().equals(dwarfMineKey)) mine.removeModifier(mod);
                }
                mine.addModifier(new AttributeModifier(
                        dwarfMineKey, DWARF_MINE_BONUS,
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ANY
                ));
            }
        } catch (Throwable t) {
            // fallback haste
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 1, false, false, true));
        }

        // Full food + saturation
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);

        player.sendMessage(Component.text("Tu es devenu un nain Chiantos !", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEEHIVE_ENTER, 1f, 1.2f);
    }

    private void clearDwarf(Player player) {
        if (player == null) return;
        dwarfPlayers.remove(player.getUniqueId());
        try {
            AttributeInstance scale = player.getAttribute(Attribute.SCALE);
            if (scale != null) {
                for (AttributeModifier mod : List.copyOf(scale.getModifiers())) {
                    if (mod.getKey().equals(dwarfScaleKey)) scale.removeModifier(mod);
                }
            }
        } catch (Throwable ignored) {}
        try {
            AttributeInstance mine = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
            if (mine != null) {
                for (AttributeModifier mod : List.copyOf(mine.getModifiers())) {
                    if (mod.getKey().equals(dwarfMineKey)) mine.removeModifier(mod);
                }
            }
        } catch (Throwable ignored) {}
        try {
            player.removePotionEffect(PotionEffectType.HASTE);
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onDwarfDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        clearDwarf(event.getEntity());
    }

    @EventHandler
    public void onDwarfRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> clearDwarf(event.getPlayer()), 5L);
    }

    // ---- Tazer (CMD 5 commande / CMD 4 select) ----

    private boolean isTazer(ItemStack stack) {
        return stack != null && stack.getType() == Material.CARROT_ON_A_STICK
                && getCmd(stack) == TAZER_CMD;
    }

    private boolean isTazerSelect(ItemStack stack) {
        return stack != null && stack.getType() == Material.CARROT_ON_A_STICK
                && getCmd(stack) == TAZER_SELECT_CMD;
    }

    private ItemStack createTazerSelect(Player holder, Player target) {
        ItemStack stack = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = stack.getItemMeta();
        // CMD 4
        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of((float) TAZER_SELECT_CMD));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(TAZER_SELECT_CMD); } catch (Throwable ignored) {}
        }
        // store target UUID
        meta.getPersistentDataContainer().set(tazerTargetKey, PersistentDataType.STRING, target.getUniqueId().toString());

        meta.displayName(Component.text("Tazer (", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.text(") ", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(playerHeadComponent(target)));

        meta.lore(buildTazerLore(holder, target));
        stack.setItemMeta(meta);
        return stack;
    }

    /** Vanilla player-head sprite (1.21.9+ / Chat Heads compatible). Color RESET so head is not tinted. */
    private Component playerHeadComponent(Player target) {
        try {
            Class<?> oc = Class.forName("net.kyori.adventure.text.object.ObjectContents");
            Object contents;
            try {
                contents = oc.getMethod("playerHead", java.util.UUID.class).invoke(null, target.getUniqueId());
            } catch (NoSuchMethodException e) {
                contents = oc.getMethod("playerHead", String.class).invoke(null, target.getName());
            }
            Component head = (Component) Component.class.getMethod("object",
                    Class.forName("net.kyori.adventure.text.object.ObjectContents"))
                    .invoke(null, contents);
            // Critical: reset color/style so the head renders with natural skin colors
            return head.style(net.kyori.adventure.text.format.Style.empty())
                    .color(net.kyori.adventure.text.format.NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, false);
        } catch (Throwable t) {
            return Component.text("?", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
        }
    }

    private java.util.List<Component> buildTazerLore(Player holder, Player target) {
        double dist = holder.getLocation().distance(target.getLocation());
        String dim = dimensionLabel(target.getWorld());
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text()
                .append(Component.text("Cible: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(playerHeadComponent(target))
                .append(Component.text(" " + target.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .build());
        lore.add(Component.text("Distance: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(String.format("%.1f m", dist), NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("Dimension: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(dim, NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("Clic droit: STUN la cible", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Clic droit autre joueur: changer", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    private String dimensionLabel(org.bukkit.World world) {
        if (world == null) return "?";
        return switch (world.getEnvironment()) {
            case NORMAL -> "Overworld (" + world.getName() + ")";
            case NETHER -> "Nether (" + world.getName() + ")";
            case THE_END -> "End (" + world.getName() + ")";
            default -> world.getName();
        };
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStunnedMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (!stunned.contains(event.getPlayer().getUniqueId())) return;
        if (event.getTo() == null) return;
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom().clone().setDirection(event.getTo().getDirection()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTazerUse(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isTazerSelect(hand)) return;
        // Don't cancel block interact if clicking entity handled elsewhere - cancel for air/block use
        event.setCancelled(true);
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;
        String idStr = meta.getPersistentDataContainer().get(tazerTargetKey, PersistentDataType.STRING);
        if (idStr == null) {
            player.sendMessage(Component.text("Aucune cible.", NamedTextColor.RED));
            return;
        }
        Player target;
        try {
            target = Bukkit.getPlayer(java.util.UUID.fromString(idStr));
        } catch (Exception e) {
            player.sendMessage(Component.text("Cible invalide.", NamedTextColor.RED));
            return;
        }
        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text("Cible hors-ligne.", NamedTextColor.RED));
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Impossible de se tazer soi-meme.", NamedTextColor.RED));
            return;
        }
        applyTazerStun(player, target);
    }

    private void applyTazerStun(Player attacker, Player target) {
        if (stunned.contains(target.getUniqueId())) {
            attacker.sendMessage(Component.text(target.getName() + " est deja stun.", NamedTextColor.GRAY));
            return;
        }
        final int durationTicks = 60; // 3 seconds
        stunned.add(target.getUniqueId());

        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks + 20, 1, false, false, true));
        try {
            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationTicks + 10, 0, false, false, true));
        } catch (Throwable ignored) {}
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 7, false, false, true));
        // Jump boost 128 disables jump on many versions
        try {
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, durationTicks, 200, false, false, false));
        } catch (Throwable ignored) {}

        target.playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9f, 1.6f);
        target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1.2f);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.8f);
        attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);

        target.sendMessage(Component.text("TZAAAP ! Tu es stun !", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
        attacker.sendMessage(Component.text("Tazer utilise sur ", NamedTextColor.GOLD)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW)));

        final float oldSpeed = target.getWalkSpeed();
        target.setWalkSpeed(0f);
        int taskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!target.isOnline() || !stunned.contains(target.getUniqueId())) return;
            org.bukkit.util.Vector v = target.getVelocity();
            target.setVelocity(new org.bukkit.util.Vector(0, Math.min(v.getY(), 0), 0));
        }, 1L, 1L).getTaskId();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            stunned.remove(target.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskId);
            if (target.isOnline()) {
                target.setWalkSpeed(oldSpeed > 0 ? oldSpeed : 0.2f);
                target.sendMessage(Component.text("Tu peux a nouveau bouger.", NamedTextColor.GREEN));
            }
        }, durationTicks);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTazerSelect(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isTazer(hand) && !isTazerSelect(hand)) return;
        event.setCancelled(true);

        // If already selected this target -> stun
        if (isTazerSelect(hand)) {
            ItemMeta meta = hand.getItemMeta();
            String idStr = meta != null ? meta.getPersistentDataContainer().get(tazerTargetKey, PersistentDataType.STRING) : null;
            if (idStr != null && idStr.equals(target.getUniqueId().toString())) {
                applyTazerStun(player, target);
                return;
            }
        }

        ItemStack selected = createTazerSelect(player, target);
        player.getInventory().setItemInMainHand(selected);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        player.sendMessage(Component.text("Cible: ", NamedTextColor.GOLD)
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(playerHeadComponent(target))
                .append(Component.text(" " + target.getName(), NamedTextColor.YELLOW)));
    }

    /** Refresh distance on held tazer select. */
    private void updateHeldTazer(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isTazerSelect(hand)) return;
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;
        String idStr = meta.getPersistentDataContainer().get(tazerTargetKey, PersistentDataType.STRING);
        if (idStr == null) return;
        java.util.UUID id;
        try { id = java.util.UUID.fromString(idStr); } catch (Exception e) { return; }
        Player target = Bukkit.getPlayer(id);
        if (target == null || !target.isOnline()) {
            // keep last lore but mark offline
            java.util.List<Component> lore = meta.lore();
            if (lore == null) lore = new java.util.ArrayList<>();
            // update distance line if present
            meta.lore(List.of(
                    Component.text("Cible hors-ligne", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
            ));
            hand.setItemMeta(meta);
            return;
        }
        meta.displayName(Component.text("Tazer (", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.text(") ", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.empty().color(NamedTextColor.WHITE))
                .append(playerHeadComponent(target)));
        meta.lore(buildTazerLore(player, target));
        hand.setItemMeta(meta);
    }

    
    
    // ---- Freddy / Golden Freddy: slow while worn ----

    // ---- Freddy POV overlay (resource pack only while Freddy helmet equipped) ----

    private void setupFreddyOverlay() {
        if (!getConfig().getBoolean("freddy-overlay.enabled", false)) {
            getLogger().info("[Freddy] Overlay POV desactive (config)");
            return;
        }
        try {
            saveResource("freddy_pov_overlay.zip", true);
        } catch (Throwable t) {
            getLogger().warning("[Freddy] Impossible d'extraire overlay: " + t.getMessage());
        }
        java.io.File packFile = new java.io.File(getDataFolder(), "freddy_pov_overlay.zip");
        if (!packFile.exists()) {
            getLogger().warning("[Freddy] freddy_pov_overlay.zip manquant");
            return;
        }
        try {
            overlayPackBytes = java.nio.file.Files.readAllBytes(packFile.toPath());
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] dig = md.digest(overlayPackBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            overlaySha1 = sb.toString();
        } catch (Throwable t) {
            getLogger().warning("[Freddy] Hash overlay: " + t.getMessage());
            return;
        }
        String cfgUrl = getConfig().getString("freddy-overlay.url", "");
        if (cfgUrl != null && !cfgUrl.isBlank()) {
            overlayUrl = cfgUrl.trim();
            getLogger().info("[Freddy] Overlay URL config: " + overlayUrl);
            return;
        }
        if (overlayServer != null) return; // already running
        int port = getConfig().getInt("freddy-overlay.port", 8765);
        try {
            overlayServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("0.0.0.0", port), 0);
            overlayServer.createContext("/freddy_pov_overlay.zip", exchange -> {
                try {
                    byte[] data = overlayPackBytes;
                    exchange.getResponseHeaders().add("Content-Type", "application/zip");
                    exchange.sendResponseHeaders(200, data.length);
                    exchange.getResponseBody().write(data);
                } finally {
                    exchange.close();
                }
            });
            overlayServer.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor());
            overlayServer.start();
            overlayUrl = "http://{host}:" + port + "/freddy_pov_overlay.zip";
            getLogger().info("[Freddy] Overlay HTTP sur port " + port + " (POV uniquement si Freddy equipe)");
        } catch (Throwable t) {
            getLogger().warning("[Freddy] Serveur overlay: " + t.getMessage());
        }
    }

    private void stopFreddyOverlay() {
        if (overlayServer != null) {
            try { overlayServer.stop(0); } catch (Throwable ignored) {}
            overlayServer = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            try { p.removeResourcePack(freddyOverlayUuid); } catch (Throwable ignored) {}
        }
        freddyOverlayActive.clear();
    }

    private String resolveOverlayUrl(Player player) {
        if (overlayUrl == null || overlayUrl.isEmpty()) return null;
        if (!overlayUrl.contains("{host}")) return overlayUrl;
        String host = "127.0.0.1";
        try {
            // Host the client used to connect (works in LAN)
            if (player.getAddress() != null) {
                // Prefer server bound address from virtual host if any
            }
            String cfgHost = getConfig().getString("freddy-overlay.host", "");
            if (cfgHost != null && !cfgHost.isBlank()) {
                host = cfgHost.trim();
            } else {
                String ip = Bukkit.getIp();
                if (ip != null && !ip.isBlank() && !ip.equals("0.0.0.0")) host = ip;
                else host = java.net.InetAddress.getLocalHost().getHostAddress();
            }
        } catch (Throwable ignored) {}
        return overlayUrl.replace("{host}", host);
    }

    private void updateFreddyOverlay(Player player) {
        if (overlayPackBytes == null && (overlayUrl == null || overlayUrl.isEmpty())) return;
        ItemStack helm = player.getInventory().getHelmet();
        boolean wearing = helm != null && helm.getType() == Material.CARVED_PUMPKIN && FREDDY_CMDS.contains(getCmd(helm));
        UUID id = player.getUniqueId();
        if (wearing) {
            if (freddyOverlayActive.contains(id)) return;
            String url = resolveOverlayUrl(player);
            if (url == null) return;
            try {
                byte[] hash = null;
                if (overlaySha1 != null && overlaySha1.length() == 40) {
                    hash = new byte[20];
                    for (int i = 0; i < 20; i++) {
                        hash[i] = (byte) Integer.parseInt(overlaySha1.substring(i * 2, i * 2 + 2), 16);
                    }
                }
                player.addResourcePack(freddyOverlayUuid, url, hash, "Freddy POV", true);
                freddyOverlayActive.add(id);
            } catch (Throwable t) {
                getLogger().warning("[Freddy] addResourcePack: " + t.getMessage());
            }
        } else if (freddyOverlayActive.remove(id)) {
            try { player.removeResourcePack(freddyOverlayUuid); } catch (Throwable ignored) {}
        }
    }

    private void updateFreddySlow(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean wearing = helm != null && helm.getType() == Material.CARVED_PUMPKIN && FREDDY_CMDS.contains(getCmd(helm));
        try {
            AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed == null) return;
            for (AttributeModifier mod : List.copyOf(speed.getModifiers())) {
                if (mod.getKey().equals(freddySlowKey)) speed.removeModifier(mod);
            }
            if (wearing) {
                speed.addModifier(new AttributeModifier(
                        freddySlowKey, -0.25, // -25% speed
                        AttributeModifier.Operation.ADD_SCALAR,
                        EquipmentSlotGroup.ANY
                ));
            }
        } catch (Throwable ignored) {}
    }

    // ---- Lunette: night vision while worn, blur when removed ----
    private void updateLunette(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean wearing = isCustomHeadCmd(helm, LUNETTE_CMD);
        UUID id = player.getUniqueId();
        if (wearing) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, true, false, true));
            lunetteWearers.add(id);
        } else if (lunetteWearers.remove(id)) {
            // just removed -> difficult vision for a few seconds
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
            try {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0, false, false, true));
            } catch (Throwable ignored) {}
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 0.7f);
        }
    }

    // ---- Paille (CMD 8): Luck II while worn ----
    private void updatePaille(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean wearing = isCustomHeadCmd(helm, PAILLE_CMD);
        if (wearing) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 1, true, false, true));
        }
    }

    // ---- Panneau (netherite axe CMD 4): 0 damage + strong knockback ----
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPanneauHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        ItemStack hand = attacker.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.NETHERITE_AXE) return;
        if (getCmd(hand) != PANNEAU_CMD) return;
        event.setDamage(0);
        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity victim) {
            org.bukkit.util.Vector dir = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector());
            dir.setY(0);
            if (dir.lengthSquared() < 0.001) {
                dir = attacker.getLocation().getDirection().setY(0);
            }
            if (dir.lengthSquared() > 0.001) {
                dir.normalize().multiply(2.2).setY(0.55);
                victim.setVelocity(dir);
            }
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.6f);
        }
    }

    
    /** Custom head cosmetics: leather_helmet (most) or carved_pumpkin (Freddy only). */
    private boolean isCustomHead(ItemStack stack) {
        if (stack == null) return false;
        Material t = stack.getType();
        return t == Material.LEATHER_HELMET || t == Material.CARVED_PUMPKIN;
    }

    private boolean isCustomHeadCmd(ItemStack stack, int cmd) {
        return isCustomHead(stack) && getCmd(stack) == cmd;
    }

    private boolean isCustomHeadCmd(ItemStack stack, java.util.Collection<Integer> cmds) {
        return isCustomHead(stack) && cmds.contains(getCmd(stack));
    }

    // ---- Baillon (billyhead / carved_pumpkin CMD 2) ----

    private boolean isBaillonHelmet(ItemStack stack) {
        // Baillon + Ring Gag: meme effets (chat gag + mute voice)
        return isCustomHeadCmd(stack, BAILLON_CMD) || isCustomHeadCmd(stack, RING_GAG_CMD);
    }

    private void updateBaillon(Player player) {
        boolean should = isBaillonHelmet(player.getInventory().getHelmet());
        boolean has = wearingBaillon.contains(player.getUniqueId());
        if (should && !has) {
            wearingBaillon.add(player.getUniqueId());
        } else if (!should && has) {
            wearingBaillon.remove(player.getUniqueId());
        }
        refreshVoiceMute(player);
    }

    private boolean isUtaHelmet(ItemStack stack) {
        return isCustomHeadCmd(stack, UTA_CMD);
    }

    private void updateUta(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean should = isUtaHelmet(helm);
        UUID id = player.getUniqueId();
        if (should) wearingUta.add(id);
        else wearingUta.remove(id);
        refreshVoiceMute(player);
    }

    /** Mute VoiceChat micro si Baillon OU Casque UTA equipe. */
    private void refreshVoiceMute(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        boolean mute = isBaillonHelmet(helm) || isUtaHelmet(helm);
        // setMuted est silencieux; l'appel reste idempotent
        try { BaillonVoiceHook.setMuted(player.getUniqueId(), mute); } catch (Throwable ignored) {}
    }


    private boolean isMaskassHelmet(ItemStack stack) {
        return isCustomHeadCmd(stack, MASKASS_CMD);
    }

    private void updateMaskass(Player player) {
        boolean should = isMaskassHelmet(player.getInventory().getHelmet());
        UUID id = player.getUniqueId();
        if (should) wearingMaskass.add(id);
        else wearingMaskass.remove(id);
    }

    private void updateChatEars(Player player) {
        boolean should = isCustomHeadCmd(player.getInventory().getHelmet(), CHAT_EARS_CMD);
        UUID id = player.getUniqueId();
        if (should) wearingChatEars.add(id);
        else wearingChatEars.remove(id);
    }

    private void clearBaillon(Player player) {
        wearingBaillon.remove(player.getUniqueId());
        refreshVoiceMute(player);
    }


    /**
     * Deforme le texte comme avec quelque chose dans la bouche :
     * L/P/B → é ou a, C étouffé, etc. Les voyelles passent mieux.
     */
    private String muffleSpeech(String input) {
        if (input == null || input.isBlank()) return input;
        Random r = ThreadLocalRandom.current();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c) || !Character.isLetter(c)) {
                out.append(c);
                continue;
            }
            out.append(muffleChar(c, r));
        }
        String result = out.toString().replaceAll(" {2,}", " ").trim();
        // parfois un petit bruit de gorge
        if (r.nextInt(6) == 0 && result.length() > 2) {
            result = result + (r.nextBoolean() ? "..." : " mmh");
        }
        return result;
    }

    private char pickVowel(Random r, boolean upper) {
        char v = r.nextBoolean() ? 'e' : (r.nextBoolean() ? 'a' : 'e');
        // e sonne comme "é" a l'oral dans ce contexte
        return upper ? Character.toUpperCase(v) : v;
    }

    private String muffleChar(char c, Random r) {
        boolean upper = Character.isUpperCase(c);
        char low = Character.toLowerCase(c);

        // Voyelles : passent souvent telles quelles
        if ("aeiouyàâäéèêëïîôùûü".indexOf(low) >= 0) {
            if (r.nextInt(100) < 80) return String.valueOf(c);
            return String.valueOf(pickVowel(r, upper));
        }

        // L → é / a
        if (low == 'l') {
            return String.valueOf(pickVowel(r, upper));
        }
        // P, B → é / a (labiales bloquees)
        if (low == 'p' || low == 'b') {
            return String.valueOf(pickVowel(r, upper));
        }
        // M → parfois m (nasal possible), sinon é/a
        if (low == 'm') {
            if (r.nextInt(100) < 40) return upper ? "M" : "m";
            return String.valueOf(pickVowel(r, upper));
        }
        // C, K, Q → etouffes (h, f, ou voyelle)
        if (low == 'c' || low == 'k' || low == 'q') {
            int n = r.nextInt(100);
            if (n < 40) return upper ? "H" : "h";
            if (n < 70) return String.valueOf(pickVowel(r, upper));
            return upper ? "F" : "f";
        }
        // G → etouffe
        if (low == 'g') {
            return r.nextInt(100) < 50 ? (upper ? "H" : "h") : String.valueOf(pickVowel(r, upper));
        }
        // D, T → souvent etouffes / adoucis
        if (low == 'd' || low == 't') {
            int n = r.nextInt(100);
            if (n < 35) return String.valueOf(c); // parfois passe
            if (n < 65) return String.valueOf(pickVowel(r, upper));
            return upper ? "H" : "h";
        }
        // R → difficile, souvent etouffe
        if (low == 'r') {
            int n = r.nextInt(100);
            if (n < 25) return String.valueOf(c);
            if (n < 60) return String.valueOf(pickVowel(r, upper));
            return upper ? "H" : "h";
        }
        // F, V → etouffes
        if (low == 'f' || low == 'v') {
            return r.nextInt(100) < 40 ? (upper ? "H" : "h") : String.valueOf(pickVowel(r, upper));
        }
        // S, Z, X → chuintes / etouffes
        if (low == 's' || low == 'z' || low == 'x') {
            int n = r.nextInt(100);
            if (n < 30) return upper ? "H" : "h";
            if (n < 60) return String.valueOf(pickVowel(r, upper));
            return upper ? "F" : "f";
        }
        // N → nasal, passe assez bien
        if (low == 'n') {
            if (r.nextInt(100) < 70) return String.valueOf(c);
            return String.valueOf(pickVowel(r, upper));
        }
        // J, W → etouffes
        if (low == 'j' || low == 'w') {
            return r.nextInt(100) < 50 ? String.valueOf(pickVowel(r, upper)) : (upper ? "H" : "h");
        }
        // H deja etouffe
        if (low == 'h') {
            return r.nextInt(100) < 50 ? String.valueOf(c) : "";
        }
        // autres lettres
        if (r.nextInt(100) < 40) return String.valueOf(c);
        return String.valueOf(pickVowel(r, upper));
    }





    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private boolean isGamelleItem(ItemStack stack) {
        if (stack == null || stack.getType() != Material.BOWL) return false;
        int cmd = getCmd(stack);
        if (cmd == 1) return true;
        if (cmd >= 2 && cmd <= 5) return true;
        if (cmd >= 12 && cmd <= 15) return true;
        if (cmd >= 22 && cmd <= 25) return true;
        return cmd >= 32 && cmd <= 125;
    }

    private int cmdToLevel(int cmd) {
        if (cmd == 1) return 0;
        if (cmd >= 2 && cmd <= 5) return cmd - 1;
        if (cmd >= 12 && cmd <= 15) return cmd - 11;
        if (cmd >= 22 && cmd <= 25) return cmd - 21;
        if (cmd >= 32) return Math.min(4, ((cmd - 32) % 10) + 1);
        return 0;
    }

    private int cmdToFoodType(int cmd) {
        if (cmd >= 12 && cmd <= 15) return 1; // glow
        if (cmd >= 22 && cmd <= 25) return 2; // gapple
        if (cmd >= 32) return 3 + ((cmd - 32) / 10); // variants
        return 0;
    }

    private int levelToCmd(int level, int foodType) {
        level = Math.max(0, Math.min(4, level));
        if (level <= 0) return 1;
        if (foodType == 1) return 11 + level;
        if (foodType == 2) return 21 + level;
        if (foodType >= 3) return 32 + (foodType - 3) * 10 + (level - 1);
        return 1 + level;
    }

    private int getFoodTypeFromItem(ItemStack stack) {
        if (stack == null) return -1;
        Material t = stack.getType();
        if (t == Material.GLOW_BERRIES || t == Material.SWEET_BERRIES) return 1;
        if (t == Material.GOLDEN_APPLE || t == Material.ENCHANTED_GOLDEN_APPLE) return 2;
        if (t == Material.MILK_BUCKET) return 3;
        if (t == Material.ROTTEN_FLESH || t == Material.COD || t == Material.SALMON
                || t == Material.TROPICAL_FISH || t == Material.PUFFERFISH) return 0;
        return -1;
    }

    private ItemStack gamelleStack(int level, int foodType) {
        int cmd = levelToCmd(level, foodType);
        ItemStack stack = new ItemStack(Material.BOWL);
        ItemMeta meta = stack.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        String name = level <= 0 ? "Gamelle vide" : "Gamelle";
        meta.displayName(Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack gamelleStack(int level) {
        return gamelleStack(level, 0);
    }

    private void freezeGamelles() {
        gamelleFreezeTick++;
        if (gamelleFreezeTick % 5 != 0) return;
        for (UUID id : new java.util.ArrayList<>(gamelleAt.values())) {
            Entity e = Bukkit.getEntity(id);
            if (e instanceof org.bukkit.entity.Item item) {
                item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                item.setGravity(false);
            }
        }
    }

    private void updateGamelleDisplay(Location blockLoc, int level, int foodType) {
        level = Math.max(0, Math.min(4, level));
        if (level <= 0) foodType = 0;
        String key = locKey(blockLoc);
        UUID old = gamelleAt.get(key);
        if (old != null) {
            Entity e = Bukkit.getEntity(old);
            if (e != null) e.remove();
        }
        // Spawn au pied du bloc + 8px (0.5) — pose au sol sans etre trop haut
        Location dloc = blockLoc.clone().add(0.5, 0.0, 0.5);
        ItemDisplay disp = blockLoc.getWorld().spawn(dloc, ItemDisplay.class);
        disp.setPersistent(true);
        disp.setInvulnerable(true);
        disp.setGravity(false);
        try { disp.setBillboard(org.bukkit.entity.Display.Billboard.FIXED); } catch (Throwable ignored) {}
        try { disp.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
        try {
            disp.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(0f, 0.5f, 0f),
                    new org.joml.AxisAngle4f(0f, 0f, 1f, 0f),
                    new org.joml.Vector3f(1f, 1f, 1f),
                    new org.joml.AxisAngle4f(0f, 0f, 1f, 0f)
            ));
        } catch (Throwable ignored) {}
        disp.setItemStack(gamelleStack(level, foodType));
        disp.getPersistentDataContainer().set(KEY_GAMELLE, PersistentDataType.BYTE, (byte) 1);
        disp.getPersistentDataContainer().set(KEY_GAMELLE_LEVEL, PersistentDataType.INTEGER, level);
        disp.getPersistentDataContainer().set(KEY_GAMELLE_FOOD, PersistentDataType.INTEGER, foodType);
        gamelleAt.put(key, disp.getUniqueId());
    }

    private void removeGamelle(Location blockLoc) {
        String key = locKey(blockLoc);
        UUID id = gamelleAt.remove(key);
        if (id != null) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        // clean nearby gamelle displays
        for (Entity e : blockLoc.getWorld().getNearbyEntities(blockLoc.clone().add(0.5, 0.5, 0.5), 0.6, 0.6, 0.6)) {
            if (e.getPersistentDataContainer().has(KEY_GAMELLE, PersistentDataType.BYTE)) {
                e.remove();
            }
        }
        if (blockLoc.getBlock().getType() == Material.STRUCTURE_VOID
                || blockLoc.getBlock().getType() == Material.BARRIER
                || blockLoc.getBlock().getType().name().contains("PRESSURE_PLATE")) {
            blockLoc.getBlock().setType(Material.AIR);
        }
    }

    private Integer getGamelleLevel(Location blockLoc) {
        String key = locKey(blockLoc);
        UUID id = gamelleAt.get(key);
        if (id != null) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) {
                Integer lv = e.getPersistentDataContainer().get(KEY_GAMELLE_LEVEL, PersistentDataType.INTEGER);
                if (lv != null) return lv;
            }
        }
        return null;
    }

    private void setGamelleLevel(Location blockLoc, int level, int foodType) {
        level = Math.max(0, Math.min(4, level));
        if (level <= 0) foodType = 0;
        updateGamelleDisplay(blockLoc, level, foodType);
    }

    private int getGamelleFoodType(Location blockLoc) {
        String key = locKey(blockLoc);
        UUID id = gamelleAt.get(key);
        if (id != null) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) {
                Integer ft = e.getPersistentDataContainer().get(KEY_GAMELLE_FOOD, PersistentDataType.INTEGER);
                if (ft != null) return ft;
            }
        }
        return 0;
    }

    private void spawnGamelleInteraction(Location blockLoc) {
        Location iloc = blockLoc.clone().add(0.5, 0.0, 0.5);
        for (Entity e : blockLoc.getWorld().getNearbyEntities(iloc, 0.4, 0.4, 0.4)) {
            if (e instanceof Interaction inter
                    && inter.getPersistentDataContainer().has(KEY_GAMELLE, PersistentDataType.BYTE)) {
                return;
            }
        }
        Interaction inter = blockLoc.getWorld().spawn(iloc, Interaction.class);
        inter.setPersistent(true);
        inter.setInteractionWidth(0.9f);
        inter.setInteractionHeight(0.0625f);
        try { inter.setResponsive(true); } catch (Throwable ignored) {}
        inter.getPersistentDataContainer().set(KEY_GAMELLE, PersistentDataType.BYTE, (byte) 1);
        inter.getPersistentDataContainer().set(KEY_GAMELLE_DISPLAY, PersistentDataType.STRING, locKey(blockLoc));
    }

    private Location gamelleLocFromInteraction(Interaction inter) {
        String key = inter.getPersistentDataContainer().get(KEY_GAMELLE_DISPLAY, PersistentDataType.STRING);
        if (key == null) return null;
        String[] p = key.split(":");
        if (p.length != 4) return null;
        World w = Bukkit.getWorld(p[0]);
        if (w == null) return null;
        return new Location(w, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGamellePlace(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isGamelleItem(hand)) return;
        if (event.getClickedBlock() == null) return;

        event.setCancelled(true);
        Location place = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        if (!place.getBlock().getType().isAir()) {
            player.sendActionBar(Component.text("Pas la place", NamedTextColor.RED));
            return;
        }

        int level = cmdToLevel(getCmd(hand));
        // petit marker invisible pour le sol (structure void = pas de collision visuelle)
        // Block invisible — la hitbox vient de l'Interaction (plate comme une plaque)
        place.getBlock().setType(Material.STRUCTURE_VOID);
        updateGamelleDisplay(place, level, cmdToFoodType(getCmd(hand)));
        spawnGamelleInteraction(place);

        hand.setAmount(hand.getAmount() - 1);
        player.playSound(place, Sound.BLOCK_STONE_PLACE, 0.6f, 1.2f);
        player.sendActionBar(Component.text("Gamelle placee", NamedTextColor.GREEN));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGamelleUseEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction inter)) return;
        if (!inter.getPersistentDataContainer().has(KEY_GAMELLE, PersistentDataType.BYTE)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        event.setCancelled(true);

        Location loc = gamelleLocFromInteraction(inter);
        if (loc == null) return;
        Integer level = getGamelleLevel(loc);
        if (level == null) level = 0;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        handleGamelleInteract(player, loc, level, hand);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGamelleUseBlock(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        Material type = event.getClickedBlock().getType();
        if (type != Material.BARRIER && type != Material.STRUCTURE_VOID
                && type != Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                && type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && type != Material.STONE_PRESSURE_PLATE) return;

        Location loc = event.getClickedBlock().getLocation();
        Integer level = getGamelleLevel(loc);
        if (level == null) return;

        // ne pas re-declencher si on place une gamelle
        if (isGamelleItem(event.getPlayer().getInventory().getItemInMainHand())) return;

        event.setCancelled(true);
        handleGamelleInteract(event.getPlayer(), loc, level, event.getPlayer().getInventory().getItemInMainHand());
    }


    private String foodTypeName(int foodType) {
        return switch (foodType) {
            case 1 -> "Glow Berry";
            case 2 -> "Gapple";
            case 3 -> "Lait";
            default -> "Standard";
        };
    }

    private void applyGamelleFoodEffects(Player player, int foodType) {
        if (foodType == 1) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.GLOWING, 20 * 60 * 3, 0, false, true, true));
        } else if (foodType == 2) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION, 20 * 5, 1, false, true, true));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.ABSORPTION, 20 * 120, 0, false, true, true));
        } else if (foodType == 3) {
            player.clearActivePotionEffects();
        }
    }

    private void handleGamelleInteract(Player player, Location loc, int level, ItemStack hand) {
        int foodType = getGamelleFoodType(loc);

        // Remplir avec une nourriture compatible
        int fillType = getFoodTypeFromItem(hand);
        if (fillType >= 0) {
            if (level >= 4 && foodType == fillType) {
                player.sendActionBar(Component.text("La gamelle est deja pleine", NamedTextColor.GRAY));
                return;
            }
            Material handType = hand.getType();
            hand.setAmount(hand.getAmount() - 1);
            if (handType == Material.MILK_BUCKET) {
                player.getInventory().addItem(new ItemStack(Material.BUCKET));
            } else if (handType == Material.POTION) {
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            }
            setGamelleLevel(loc, 4, fillType);
            player.playSound(loc, Sound.ITEM_BUCKET_FILL, 0.7f, 1.3f);
            player.sendActionBar(Component.text("Gamelle remplie (" + foodTypeName(fillType) + ")", NamedTextColor.GREEN));
            return;
        }

        // Manger (main vide)
        if (hand == null || hand.getType().isAir() || hand.getAmount() == 0) {
            if (level <= 0) {
                player.sendActionBar(Component.text("La gamelle est vide", NamedTextColor.GRAY));
                return;
            }
            // faim de base
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 7));
            player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + 5.5f));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            applyGamelleFoodEffects(player, foodType);
            player.sendActionBar(Component.text(foodTypeName(foodType) + " (" + (level - 1) + "/4)", NamedTextColor.YELLOW));

            int newLevel = level - 1;
            setGamelleLevel(loc, newLevel, newLevel <= 0 ? 0 : foodType);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onGamelleBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type != Material.BARRIER && type != Material.STRUCTURE_VOID
                && type != Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                && type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && type != Material.STONE_PRESSURE_PLATE) return;
        Location loc = event.getBlock().getLocation();
        if (getGamelleLevel(loc) == null && !gamelleAt.containsKey(locKey(loc))) return;
        event.setDropItems(false);
        Integer level = getGamelleLevel(loc);
        int foodType = getGamelleFoodType(loc);
        removeGamelle(loc);
        ItemStack drop = gamelleStack(level != null ? level : 0, level != null && level > 0 ? foodType : 0);
        loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.2, 0.5), drop);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        try {
            Player player = event.getPlayer();
            UUID id = player.getUniqueId();
            // Baillon / Ring Gag / Maskass: texte deforme (pas totalement remplace)
            if (wearingBaillon.contains(id) || wearingMaskass.contains(id)) {
                String plain = event.signedMessage() != null ? event.signedMessage().message() : event.message().toString();
                event.message(Component.text(muffleSpeech(plain)));
                return;
            }
            // Oreilles de chat: ajoute "Nya." a la fin
            if (wearingChatEars.contains(id)) {
                event.message(event.message().append(Component.text(" Nya.")));
            }
        } catch (Throwable ignored) {
        }
    }

    // ---- zabuza (netherite_axe CMD 3): -20% speed, +10% damage ----

    private boolean isZabuza(ItemStack stack) {
        return stack != null && stack.getType() == Material.NETHERITE_AXE && getCmd(stack) == ZABUZA_CMD;
    }

    private void updateZabuza(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        boolean should = isZabuza(hand);
        boolean has = holdingZabuza.contains(player.getUniqueId());
        if (should && !has) {
            applyZabuzaSlow(player);
        } else if (!should && has) {
            clearZabuza(player);
        }
    }

    private void applyZabuzaSlow(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) return;
        for (AttributeModifier mod : List.copyOf(speed.getModifiers())) {
            if (mod.getKey().equals(zabuzaKey)) speed.removeModifier(mod);
        }
        AttributeModifier mod = new AttributeModifier(
                zabuzaKey, ZABUZA_SLOW_FACTOR,
                AttributeModifier.Operation.ADD_SCALAR,
                EquipmentSlotGroup.ANY
        );
        speed.addModifier(mod);
        holdingZabuza.add(player.getUniqueId());
    }

    private void clearZabuza(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            for (AttributeModifier mod : List.copyOf(speed.getModifiers())) {
                if (mod.getKey().equals(zabuzaKey)) speed.removeModifier(mod);
            }
        }
        holdingZabuza.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZabuzaDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isZabuza(attacker.getInventory().getItemInMainHand())) return;
        // +10% damage over current (netherite axe baseline already applied)
        event.setDamage(event.getDamage() * ZABUZA_DAMAGE_MULT);
    }

    // ---- desassembleur neutronique (netherite_pickaxe CMD 1): one-shot stone + ores ----

    private boolean isDesassembleur(ItemStack stack) {
        return stack != null && stack.getType() == Material.NETHERITE_PICKAXE
                && getCmd(stack) == DESASSEMBLEUR_CMD;
    }

    private boolean isStoneOrOre(Material type) {
        if (type == null || type.isAir()) return false;
        String n = type.name();
        if (n.endsWith("_ORE") || n.equals("ANCIENT_DEBRIS") || n.equals("NETHER_QUARTZ_ORE")) return true;
        if (n.equals("STONE") || n.equals("COBBLESTONE") || n.equals("MOSSY_COBBLESTONE")
                || n.equals("SMOOTH_STONE") || n.equals("STONE_BRICKS") || n.equals("MOSSY_STONE_BRICKS")
                || n.equals("CRACKED_STONE_BRICKS") || n.equals("CHISELED_STONE_BRICKS")
                || n.equals("DEEPSLATE") || n.equals("COBBLED_DEEPSLATE") || n.equals("POLISHED_DEEPSLATE")
                || n.equals("DEEPSLATE_BRICKS") || n.equals("DEEPSLATE_TILES") || n.equals("CHISELED_DEEPSLATE")
                || n.equals("REINFORCED_DEEPSLATE") || n.equals("TUFF") || n.equals("POLISHED_TUFF")
                || n.equals("TUFF_BRICKS") || n.equals("CHISELED_TUFF") || n.equals("CHISELED_TUFF_BRICKS")
                || n.equals("ANDESITE") || n.equals("POLISHED_ANDESITE")
                || n.equals("DIORITE") || n.equals("POLISHED_DIORITE")
                || n.equals("GRANITE") || n.equals("POLISHED_GRANITE")
                || n.equals("BLACKSTONE") || n.equals("POLISHED_BLACKSTONE")
                || n.equals("POLISHED_BLACKSTONE_BRICKS") || n.equals("GILDED_BLACKSTONE")
                || n.equals("BASALT") || n.equals("SMOOTH_BASALT") || n.equals("POLISHED_BASALT")
                || n.equals("NETHERRACK") || n.equals("END_STONE") || n.equals("END_STONE_BRICKS")
                || n.equals("CALCITE") || n.equals("DRIPSTONE_BLOCK") || n.equals("POINTED_DRIPSTONE")
                || n.equals("OBSIDIAN") || n.equals("CRYING_OBSIDIAN") || n.equals("MAGMA_BLOCK")) {
            return true;
        }
        try {
            if (Tag.BASE_STONE_OVERWORLD.isTagged(type)) return true;
            if (Tag.BASE_STONE_NETHER.isTagged(type)) return true;
            if (Tag.COAL_ORES.isTagged(type) || Tag.COPPER_ORES.isTagged(type)
                    || Tag.IRON_ORES.isTagged(type) || Tag.GOLD_ORES.isTagged(type)
                    || Tag.DIAMOND_ORES.isTagged(type) || Tag.EMERALD_ORES.isTagged(type)
                    || Tag.LAPIS_ORES.isTagged(type) || Tag.REDSTONE_ORES.isTagged(type)) return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDesassembleurMine(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (!isDesassembleur(player.getInventory().getItemInMainHand())) return;
        Block block = event.getBlock();
        if (!isStoneOrOre(block.getType())) return;
        event.setInstaBreak(true);
    }

    
    // ---- food nutrition / saturation ----

    private void applyFoodComponent(ItemMeta meta, ItemData data) {
        FoodData food = FoodData.match(data.material, data.cmd);
        if (food == null) return;
        try {
            FoodComponent fc = meta.getFood();
            fc.setNutrition(food.nutrition);
            fc.setSaturation(food.saturation);
            try { fc.setCanAlwaysEat(false); } catch (Throwable ignored) {}
            meta.setFood(fc);
        } catch (Throwable t) {
            getLogger().warning("Food component failed for " + data.name + ": " + t.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        Player player = event.getPlayer();
        int cmd = getCmd(item);

        // Chiantos ouvert: dwarf + full food
        if (item.getType() == Material.COOKIE && cmd == CHIANTOS_OUVERT_CMD) {
            Bukkit.getScheduler().runTask(this, () -> {
                applyDwarf(player);
                player.setFoodLevel(20);
                player.setSaturation(20f);
                player.setExhaustion(0f);
            });
            return;
        }

        // Closed should not be edible (cancel if somehow eaten)
        if (item.getType() == Material.COOKIE && cmd == CHIANTOS_FERME_CMD) {
            event.setCancelled(true);
            return;
        }

        FoodData food = FoodData.match(item.getType(), cmd);
        if (food == null) return;
        Bukkit.getScheduler().runTask(this, () -> {
            float sat = Math.min(20f, player.getSaturation() + food.saturation);
            if (player.getSaturation() < sat) {
                player.setSaturation(sat);
            }
        });
    }

    private void registerFoodRecipes() {
        int count = 0;
        // Diamond Cookie: cookie + diamond
        count += addShapeless("food_diamond_cookie", foodResult("apple", 1, "Diamond Cookie"),
                Material.COOKIE, Material.DIAMOND);
        // Orange: apple + orange dye (proxy)
        count += addShapeless("food_orange", foodResult("apple", 2, "Orange"),
                Material.APPLE, Material.ORANGE_DYE);
        // Banane: apple + yellow dye
        count += addShapeless("food_banane", foodResult("apple", 3, "Banane"),
                Material.APPLE, Material.YELLOW_DYE);
        // Grapes: apple + purple dye + sweet berries
        count += addShapeless("food_grapes", foodResult("apple", 4, "Grapes"),
                Material.APPLE, Material.PURPLE_DYE, Material.SWEET_BERRIES);
        // Tomate: snowball + red dye + apple
        count += addShapeless("food_tomate", foodResult("snowball", 1, "Tomate"),
                Material.APPLE, Material.RED_DYE);
        // Salade: bowl + tomate ingredients
        count += addShapeless("food_salade", foodResult("rabbit_stew", 1, "Salade"),
                Material.BOWL, Material.APPLE, Material.OAK_LEAVES, Material.RED_DYE);
        // CRFC: cooked chicken + bread + baked potato
        count += addShapeless("food_crfc", foodResult("baked_potato", 1, "CRFC"),
                Material.COOKED_CHICKEN, Material.BREAD, Material.BAKED_POTATO);
        // McDo: cooked beef + bread + baked potato
        count += addShapeless("food_mcdo", foodResult("baked_potato", 2, "McDo"),
                Material.COOKED_BEEF, Material.BREAD, Material.BAKED_POTATO);
        // Pwasson: cooked cod + wheat seeds (fish dish)
        count += addShapeless("food_pwasson", foodResult("cooked_cod", 1, "Pwasson"),
                Material.COOKED_COD, Material.WHEAT_SEEDS, Material.KELP);
        // Cooked Chokobo: chicken + blaze powder (fantasy)
        count += addShapeless("food_chokobo", foodResult("cooked_chicken", 1, "Cooked Chokobo"),
                Material.COOKED_CHICKEN, Material.BLAZE_POWDER);
        // Mandragor: cookie + nether wart + wheat
        count += addShapeless("food_mandragor", foodResult("cookie", 4, "Mandragor"),
                Material.COOKIE, Material.NETHER_WART, Material.WHEAT);

        getLogger().info("Registered " + count + " food recipes (visible in recipe book / JEI if supported)");
    }

    private ItemStack foodResult(String material, int cmd, String displayName) {
        Material mat = Material.matchMaterial(material);
        if (mat == null) mat = Material.APPLE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(displayName, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        FoodData food = FoodData.match(material, cmd);
        if (food != null) {
            try {
                FoodComponent fc = meta.getFood();
                fc.setNutrition(food.nutrition);
                fc.setSaturation(food.saturation);
                meta.setFood(fc);
            } catch (Throwable ignored) {}
            meta.lore(List.of(
                    Component.text("Faim: " + (food.nutrition / 2.0) + " barre(s)", NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Saturation: " + food.saturation, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }
        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of((float) cmd));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private int addShapeless(String id, ItemStack result, Material... ingredients) {
        NamespacedKey key = new NamespacedKey(this, id);
        try { Bukkit.removeRecipe(key); } catch (Throwable ignored) {}
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (Material m : ingredients) {
            recipe.addIngredient(m);
        }
        boolean ok = Bukkit.addRecipe(recipe);
        return ok ? 1 : 0;
    }

    // ---- shampoing (gallashower) recipes: colored wool -> white wool ----

    private ItemStack shampooItem() {
        ItemStack stack = new ItemStack(Material.COOKED_CHICKEN);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("Shampoing", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of(2.0f));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(2); } catch (Throwable ignored) {}
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private void registerShampooRecipes() {
        Material[] coloredWools = {
                Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL,
                Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL,
                Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL,
                Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
        };
        ItemStack shampoo = shampooItem();
        int i = 0;
        for (Material wool : coloredWools) {
            NamespacedKey key = new NamespacedKey(this, "shampoing_wool_" + (i++));
            // remove old if reload
            try { Bukkit.removeRecipe(key); } catch (Throwable ignored) {}
            ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.WHITE_WOOL));
            recipe.addIngredient(new RecipeChoice.ExactChoice(shampoo.clone()));
            recipe.addIngredient(wool);
            Bukkit.addRecipe(recipe);
        }
        getLogger().info("Registered " + coloredWools.length + " Shampoing wool recipes");
    }

    // ---- menu ----

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if (name.equals("crmaskdebug")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only");
                return true;
            }
            ItemStack helm = player.getInventory().getHelmet();
            sender.sendMessage(Component.text("Helmet: " + (helm == null ? "null" : helm.getType().name()), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("CMD: " + getCmd(helm), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Is mask: " + isCmdHelmet(helm, HIDE_NAMETAG_CMDS), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Hidden set: " + hidden.contains(player.getUniqueId()), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Mask CMDs: " + HIDE_NAMETAG_CMDS, NamedTextColor.GRAY));
            updateNametag(player);
            return true;
        }
        if (name.equals("crclear") || name.equals("crv6clear")) {
            if (!sender.hasPermission("crv6.reload")) {
                sender.sendMessage(Component.text("Permission manquante", NamedTextColor.RED));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Joueurs only");
                return true;
            }
            if (furniture == null) {
                player.sendMessage(Component.text("Furniture non charge", NamedTextColor.RED));
                return true;
            }
            int n = furniture.clearDisplays(player.getLocation(), 3.0);
            player.sendMessage(Component.text("Supprime " + n + " entit(e)s display dans 3 blocs", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 0.8f, 1.2f);
            return true;
        }
        if (name.equals("crfurniture") || name.equals("crfurn") || name.equals("meuble")) {
            if (!sender.hasPermission("crv6.give")) {
                sender.sendMessage(Component.text("Permission manquante", NamedTextColor.RED));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Joueurs only");
                return true;
            }
            if (furniture == null) {
                player.sendMessage(Component.text("Furniture non charge", NamedTextColor.RED));
                return true;
            }
            player.getInventory().addItem(furniture.socleItem());
            player.getInventory().addItem(furniture.tableItem());
            player.getInventory().addItem(furniture.chaiseItem());
            player.getInventory().addItem(furniture.mailboxItem());
            player.sendMessage(Component.text("+ Socle, Table, Chaise, Boite aux lettres", NamedTextColor.GREEN));
            return true;
        }
        if (name.equals("crreload") || name.equals("crv6reload")) {
            if (!sender.hasPermission("crv6.reload")) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return true;
            }
            reloadPlugin(sender);
            return true;
        }
        // /crv6
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("crv6.menu")) {
            player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        try {
            openMainMenu(player);
        } catch (Throwable t) {
            player.sendMessage(Component.text("Erreur menu: " + t.getMessage(), NamedTextColor.RED));
            getLogger().severe("openMainMenu failed: " + t.getMessage());
            t.printStackTrace();
        }
        return true;
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            // re-register recipes
            registerShampooRecipes();
            registerFoodRecipes();
            setupTeam();
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateNametag(p);
                updateJump(p);
                updateLifeCrystal(p);
                updateZabuza(p);
                updateBaillon(p);
            }
            sender.sendMessage(Component.text("CRV6Menu recharge.", NamedTextColor.GREEN));
            getLogger().info("Reloaded by " + sender.getName());
        } catch (Throwable t) {
            sender.sendMessage(Component.text("Erreur reload: " + t.getMessage(), NamedTextColor.RED));
            t.printStackTrace();
        }
    }


    // ---- custom GUI titles (resource pack font \uE000 / \uE001) ----

    private static final String NEG = "\uE005"; // -8px nudge left

    private Component guiTitle(boolean main, String label) {
        String bg = main ? "\uE000" : "\uE001";
        return Component.text()
                .append(Component.text(NEG + bg)
                        .font(Key.key("minecraft", "default"))
                        .color(NamedTextColor.WHITE))
                .append(Component.text("  " + label)
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD))
                .build();
    }

    private ItemStack filler() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack categoryIcon(String cat, int count, int index) {
        int cmd = menuCmdForCategory(cat);
        ItemStack icon = menuIcon(cmd, cat);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Component.text(cat, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Component.text("---------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(count + " items disponibles", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("▶ Clique pour ouvrir", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                Component.text("---------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        icon.setItemMeta(meta);
        return icon;
    }

    private int menuCmdForCategory(String cat) {
        if (cat.contains("Divers")) return 9101;
        if (cat.contains("Arme")) return 9102;
        if (cat.contains("Echec") || cat.contains("Échec")) return 9103;
        if (cat.contains("Boost")) return 9104;
        if (cat.contains("Totem")) return 9105;
        if (cat.contains("Citrouille")) return 9106;
        if (cat.contains("Nourriture")) return 9107;
        return 9101;
    }

    private ItemStack menuIcon(int cmd, String name) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of((float) cmd));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        stack.setItemMeta(meta);
        return stack;
    }




    // ---- Tetes CR (tous les joueurs du document, ordre alpha) ----

    private static final int PGALLADIUM_CMD = 3;
    private static final NamespacedKey TETES_PLAYER_KEY = new NamespacedKey("crv6", "tetes_player");
    private static final NamespacedKey TETES_ACTION_KEY = new NamespacedKey("crv6", "tetes_action");

    /** Cache des icones menu (pas de lookup Mojang a l'ouverture). */
    private final java.util.Map<String, ItemStack> tetesIconCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, ItemStack> poupeeItemCache = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile boolean tetesCacheReady = false;
    /** playerName (exact) -> variantes */
    private final java.util.Map<String, java.util.List<BenjiVariant>> variantsByPlayer = new java.util.concurrent.ConcurrentHashMap<>();

    private static final class BenjiVariant {
        final String id, label; final int cmd;
        BenjiVariant(String id, String label, int cmd) { this.id = id; this.label = label; this.cmd = cmd; }
    }

    private java.util.List<BenjiVariant> variantsOf(String playerName) {
        if (playerName == null) return java.util.List.of();
        java.util.List<BenjiVariant> list = variantsByPlayer.get(playerName);
        if (list != null) return list;
        for (var e : variantsByPlayer.entrySet()) {
            if (e.getKey().equalsIgnoreCase(playerName)) return e.getValue();
        }
        return java.util.List.of();
    }

    private void loadPlayerVariants(String resource, String playerName) {
        try (java.io.InputStream in = getResource(resource)) {
            if (in == null) return;
            StringBuilder sb = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) sb.append(line);
            }
            String json = sb.toString();
            java.util.List<BenjiVariant> list = new java.util.ArrayList<>();
            int idx = 0;
            while ((idx = json.indexOf("\"id\"", idx)) >= 0) {
                int i1 = json.indexOf('"', idx + 4);
                int i2 = json.indexOf('"', i1 + 1);
                String id = json.substring(i1 + 1, i2);
                int lIdx = json.indexOf("\"label\"", i2);
                int l1 = json.indexOf('"', lIdx + 7);
                int l2 = json.indexOf('"', l1 + 1);
                String label = json.substring(l1 + 1, l2);
                int cIdx = json.indexOf("\"cmd\"", l2);
                int colon = json.indexOf(':', cIdx);
                int end = colon + 1;
                while (end < json.length() && !Character.isDigit(json.charAt(end))) end++;
                int numEnd = end;
                while (numEnd < json.length() && Character.isDigit(json.charAt(numEnd))) numEnd++;
                int cmd = Integer.parseInt(json.substring(end, numEnd));
                list.add(new BenjiVariant(id, label, cmd));
                idx = numEnd;
            }
            if (!list.isEmpty()) {
                variantsByPlayer.put(playerName, list);
                getLogger().info("[" + playerName + "] " + list.size() + " variantes chargees");
            }
        } catch (Exception ex) {
            getLogger().warning("[" + playerName + "] variants: " + ex.getMessage());
        }
    }

    private ItemStack tetesMenuButton(Player opener) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            try { skull.setOwningPlayer(opener); } catch (Throwable t) {
                try { skull.setPlayerProfile(opener.getPlayerProfile()); } catch (Throwable ignored) {}
            }
        }
        meta.displayName(Component.text("Tetes CR", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Component.text(Crv6Players.all().size() + " joueurs Cube Reborn", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Ordre alphabetique", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Clique pour ouvrir", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        stack.setItemMeta(meta);
        return stack;
    }

    private void openTetesMenu(Player player, int page) {
        List<Crv6Players.Entry> list = Crv6Players.all();
        final int perPage = 45;
        int maxPage = Math.max(0, (list.size() - 1) / perPage);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MenuHolder.Type.TETES, "Tetes", page), 54,
                guiTitle(false, "Tetes CR · " + (page + 1) + "/" + (maxPage + 1)));

        int start = page * perPage;
        int end = Math.min(start + perPage, list.size());
        for (int i = start; i < end; i++) {
            inv.setItem(i - start, tetesListIcon(list.get(i)));
        }

        ItemStack back = menuIcon(9110, "Retour");
        ItemMeta bm = back.getItemMeta();
        bm.displayName(Component.text("Retour (Divers)", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(bm);
        inv.setItem(49, back);

        if (page > 0) {
            ItemStack prev = menuIcon(9111, "Page precedente");
            ItemMeta pm = prev.getItemMeta();
            pm.displayName(Component.text("Page precedente", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            prev.setItemMeta(pm);
            inv.setItem(45, prev);
        }
        if (page < maxPage) {
            ItemStack next = menuIcon(9112, "Page suivante");
            ItemMeta nm = next.getItemMeta();
            nm.displayName(Component.text("Page suivante", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    private void prebuildTetesCache() {
        long t0 = System.currentTimeMillis();
        for (Crv6Players.Entry e : Crv6Players.all()) {
            try {
                tetesIconCache.put(e.name.toLowerCase(java.util.Locale.ROOT), buildTetesListIcon(e));
                poupeeItemCache.put(e.name.toLowerCase(java.util.Locale.ROOT), buildPoupeeItem(e.name));
            } catch (Throwable ignored) {}
        }
        tetesCacheReady = true;
        getLogger().info("[Tetes] cache pret: " + tetesIconCache.size() + " icones en "
                + (System.currentTimeMillis() - t0) + "ms");
    }

    private ItemStack tetesListIcon(Crv6Players.Entry e) {
        ItemStack cached = tetesIconCache.get(e.name.toLowerCase(java.util.Locale.ROOT));
        if (cached != null) return cached.clone();
        return buildTetesListIcon(e);
    }

    /** Icone menu = modele poupee pack (skin local), AUCUN appel reseau. */
    private ItemStack buildTetesListIcon(Crv6Players.Entry e) {
        ItemStack head = new ItemStack(Material.ARMOR_STAND);
        ItemMeta meta = head.getItemMeta();
        int cmd = Crv6Players.poupeeCmd(e.name);
        if (cmd <= 0) cmd = 3;
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(e.name, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("Versions: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(e.versionsLabel(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("Carte: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(e.carteLabel(),
                        e.carte == null ? NamedTextColor.DARK_GRAY : (e.carte ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("Actif: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(e.actifLabel(),
                        e.actif == null ? NamedTextColor.DARK_GRAY : (e.actif ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Clique pour le menu joueur", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(TETES_PLAYER_KEY, PersistentDataType.STRING, e.name);
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        head.setItemMeta(meta);
        return head;
    }


    private void openTeteDetail(Player player, String playerName) {
        Crv6Players.Entry e = Crv6Players.get(playerName);
        if (e == null) {
            player.sendMessage(Component.text("Joueur inconnu.", NamedTextColor.RED));
            return;
        }
        java.util.List<BenjiVariant> vars = variantsOf(playerName);
        boolean hasVariants = !vars.isEmpty();
        int size = hasVariants ? 54 : 27;
        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MenuHolder.Type.TETE_DETAIL, "Tetes", 0, e.name), size,
                guiTitle(false, e.name));

        ItemStack filler = invisibleMenuFiller();
        for (int i = 0; i < size; i++) inv.setItem(i, filler.clone());

        ItemStack info = tetesListIcon(e);
        ItemMeta im = info.getItemMeta();
        if (im != null) {
            java.util.List<Component> lore = im.lore() != null ? new java.util.ArrayList<>(im.lore()) : new java.util.ArrayList<>();
            lore.removeIf(c -> c.toString().contains("Clique"));
            im.lore(lore);
            info.setItemMeta(im);
        }
        inv.setItem(4, info);

        ItemStack giveHead = tetesListIcon(e).clone();
        ItemMeta hm = giveHead.getItemMeta();
        hm.displayName(Component.text("Give Tete", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        hm.lore(java.util.List.of(
                Component.text("Tete de " + e.name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Versions: " + e.versionsLabel(), NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false),
                Component.text("▶ Clique pour recevoir", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
        ));
        hm.getPersistentDataContainer().set(TETES_PLAYER_KEY, PersistentDataType.STRING, e.name);
        hm.getPersistentDataContainer().set(TETES_ACTION_KEY, PersistentDataType.STRING, "head");
        giveHead.setItemMeta(hm);
        inv.setItem(11, giveHead);

        ItemStack doll = createPoupeeItem(e.name);
        ItemMeta dm = doll.getItemMeta();
        dm.displayName(Component.text("Give Poupee (defaut)", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        java.util.List<Component> dlore = dm.lore() != null ? new java.util.ArrayList<>(dm.lore()) : new java.util.ArrayList<>();
        dlore.add(Component.text("▶ Clique pour recevoir", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        dm.lore(dlore);
        dm.getPersistentDataContainer().set(TETES_PLAYER_KEY, PersistentDataType.STRING, e.name);
        dm.getPersistentDataContainer().set(TETES_ACTION_KEY, PersistentDataType.STRING, "doll");
        doll.setItemMeta(dm);
        inv.setItem(15, doll);

        // Variantes a partir de la ligne 4 (slot 27) — ne touche pas tete/poupee (11/15) ni retour (49)
        int slot = 27;
        for (BenjiVariant v : vars) {
            if (slot >= 45) break;
            if (slot == 49) { slot++; continue; }
            ItemStack varItem = createVariantItem(playerName, v);
            ItemMeta vm = varItem.getItemMeta();
            java.util.List<Component> vl = vm.lore() != null ? new java.util.ArrayList<>(vm.lore()) : new java.util.ArrayList<>();
            vl.add(Component.text("▶ Clique pour recevoir", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            vm.lore(vl);
            vm.getPersistentDataContainer().set(TETES_ACTION_KEY, PersistentDataType.STRING, "var:" + playerName + ":" + v.id);
            varItem.setItemMeta(vm);
            inv.setItem(slot++, varItem);
        }

        ItemStack back = menuIcon(9110, "Retour");
        ItemMeta bm = back.getItemMeta();
        bm.displayName(Component.text("Retour", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        bm.getPersistentDataContainer().set(TETES_ACTION_KEY, PersistentDataType.STRING, "back");
        back.setItemMeta(bm);
        // Retour UNIQUEMENT en bas centre — ne jamais mettre le retour sur 22 en menu 54 (sinon bloque peluches)
        inv.setItem(hasVariants ? 49 : 22, back);


        player.openInventory(inv);
    }


    private ItemStack invisibleMenuFiller() {
        ItemStack s = new ItemStack(Material.PAPER);
        ItemMeta meta = s.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(List.of(9204f));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(9204); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        try { meta.setHideTooltip(true); } catch (Throwable ignored) {}
        s.setItemMeta(meta);
        return s;
    }

    /** Applique le skin offline via OfflinePlayer / profile Mojang. */
    private void applyOfflineSkin(SkullMeta skull, String playerName) {
        // JAMAIS profile.complete(true) sur le thread principal (lag serveur)
        try {
            org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayerIfCached(playerName);
            if (off != null) {
                skull.setOwningPlayer(off);
                return;
            }
        } catch (Throwable ignored) {}
        try {
            // Profile non-complete : le client resout parfois le skin sans bloquer le serveur
            var profile = Bukkit.createProfile(playerName);
            skull.setPlayerProfile(profile);
        } catch (Throwable t) {
            try {
                skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            } catch (Throwable ignored) {}
        }
    }

    private ItemStack createOfflinePlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            applyOfflineSkin(skull, playerName);
        }
        Crv6Players.Entry e = Crv6Players.get(playerName);
        meta.displayName(Component.text("Tete de " + playerName, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new java.util.ArrayList<>();
        if (e != null) {
            lore.add(Component.text("Versions: " + e.versionsLabel(), NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Carte: " + e.carteLabel(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Cube Reborn", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        head.setItemMeta(meta);
        return head;
    }






    private ItemStack createVariantItem(String playerName, BenjiVariant v) {
        ItemStack stack = new ItemStack(Material.ARMOR_STAND);
        ItemMeta meta = stack.getItemMeta();
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) v.cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(v.cmd); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text(playerName + " — " + v.label, NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(java.util.List.of(
                Component.text("Variante de " + playerName, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(v.label, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
                Component.text("Clic droit: placer", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(TETES_PLAYER_KEY, PersistentDataType.STRING, playerName);
        meta.getPersistentDataContainer().set(new NamespacedKey("crv6", "variant_id"), PersistentDataType.STRING, v.id);
        meta.getPersistentDataContainer().set(new NamespacedKey("crv6", "variant_owner"), PersistentDataType.STRING, playerName);
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createPoupeeItem(String playerName) {
        ItemStack cached = poupeeItemCache.get(playerName.toLowerCase(java.util.Locale.ROOT));
        if (cached != null) return cached.clone();
        return buildPoupeeItem(playerName);
    }

    private ItemStack buildPoupeeItem(String playerName) {
        ItemStack stack = new ItemStack(Material.ARMOR_STAND);
        ItemMeta meta = stack.getItemMeta();
        int cmd = Crv6Players.poupeeCmd(playerName);
        if (cmd <= 0) cmd = 3;
        try {
            CustomModelDataComponent c = meta.getCustomModelDataComponent();
            c.setFloats(java.util.List.of((float) cmd));
            meta.setCustomModelDataComponent(c);
        } catch (Throwable t) {
            try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {}
        }
        meta.displayName(Component.text("Poupee de " + playerName, NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        Crv6Players.Entry e = Crv6Players.get(playerName);
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("Petite poupee kawaii", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (e != null) {
            lore.add(Component.text("Versions: " + e.versionsLabel(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Carte: " + e.carteLabel(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text("Clic droit: placer", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Clic gauche: recuperer", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(TETES_PLAYER_KEY, PersistentDataType.STRING, playerName);
        try { meta.setEnchantmentGlintOverride(false); } catch (Throwable ignored) {}
        stack.setItemMeta(meta);
        return stack;
    }


    // ---- Poupees posables (ItemDisplay + barrier) ----
    private final java.util.Map<String, org.bukkit.entity.ItemDisplay> poupeeDisplays = new java.util.concurrent.ConcurrentHashMap<>();
    private final NamespacedKey POUPEE_KEY = new NamespacedKey("crv6", "poupee");
    private final NamespacedKey POUPEE_YAW = new NamespacedKey("crv6", "poupee_yaw");

    private String poupeeLocKey(org.bukkit.Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private boolean isPoupeeItem(ItemStack stack) {
        if (stack == null || stack.getType() != Material.ARMOR_STAND) return false;
        int cmd = getCmd(stack);
        return Crv6Players.isPoupeeCmd(cmd) || cmd == 3 || (cmd >= 11001 && cmd <= 11699);
    }

    /** Trouve la poupee liee a un barrier (map ou scan nearby). */
    private org.bukkit.entity.ItemDisplay findPoupeeAt(org.bukkit.block.Block block) {
        if (block == null) return null;
        String k = poupeeLocKey(block.getLocation());
        org.bukkit.entity.ItemDisplay disp = poupeeDisplays.get(k);
        if (disp != null && !disp.isDead()) return disp;
        for (var e : block.getWorld().getNearbyEntities(
                block.getLocation().add(0.5, 0.4, 0.5), 0.75, 0.9, 0.75)) {
            if (e instanceof org.bukkit.entity.ItemDisplay id
                    && id.getPersistentDataContainer().has(POUPEE_KEY, PersistentDataType.STRING)) {
                poupeeDisplays.put(k, id);
                return id;
            }
        }
        return null;
    }

    private void applyPoupeeTransform(org.bukkit.entity.ItemDisplay disp, float yaw) {
        try {
            float yawRad = (float) Math.toRadians(-yaw);
            disp.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(0f, 0.15f, 0f),
                    new org.joml.AxisAngle4f(yawRad, 0f, 1f, 0f),
                    new org.joml.Vector3f(0.45f, 0.45f, 0.45f),
                    new org.joml.AxisAngle4f(0f, 0f, 1f, 0f)
            ));
            disp.setRotation(yaw, 0f);
        } catch (Throwable ignored) {}
        try {
            disp.getPersistentDataContainer().set(POUPEE_YAW, PersistentDataType.FLOAT, yaw);
        } catch (Throwable ignored) {}
    }

    private float getPoupeeYaw(org.bukkit.entity.ItemDisplay disp) {
        try {
            if (disp.getPersistentDataContainer().has(POUPEE_YAW, PersistentDataType.FLOAT)) {
                return disp.getPersistentDataContainer().get(POUPEE_YAW, PersistentDataType.FLOAT);
            }
        } catch (Throwable ignored) {}
        return disp.getLocation().getYaw();
    }

    /** Au demarrage: re-associe toutes les poupees persistantes. */
    private void rebindPoupes() {
        poupeeDisplays.clear();
        int n = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (var e : world.getEntities()) {
                if (!(e instanceof org.bukkit.entity.ItemDisplay d)) continue;
                if (!d.getPersistentDataContainer().has(POUPEE_KEY, PersistentDataType.STRING)) continue;
                org.bukkit.Location base = d.getLocation().clone();
                org.bukkit.block.Block b = base.getBlock();
                // chercher barrier sous / autour
                if (b.getType() != Material.BARRIER) {
                    org.bukkit.block.Block under = base.clone().add(0, -0.2, 0).getBlock();
                    if (under.getType() == Material.BARRIER) b = under;
                    else {
                        // recreer barrier a la position arrondie
                        b = new org.bukkit.Location(world,
                                Math.floor(base.getX()), Math.floor(base.getY()), Math.floor(base.getZ())).getBlock();
                        if (b.getType().isAir() || b.getType() == Material.BARRIER) {
                            b.setType(Material.BARRIER);
                        } else {
                            // barrier a y-1 si le display est legerement au dessus
                            org.bukkit.block.Block b2 = b.getRelative(0, -1, 0);
                            if (b2.getType().isAir()) {
                                b2.setType(Material.BARRIER);
                                b = b2;
                            } else {
                                b.setType(Material.BARRIER);
                            }
                        }
                    }
                }
                // recentrer display + re-appliquer transform
                float yaw = getPoupeeYaw(d);
                org.bukkit.Location dloc = b.getLocation().clone().add(0.5, 0.0, 0.5);
                d.teleport(dloc);
                try { d.setBillboard(org.bukkit.entity.Display.Billboard.FIXED); } catch (Throwable ignored) {}
                try { d.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}
                applyPoupeeTransform(d, yaw);
                // re-set item stack si perdu (CMD depuis nom)
                String pname = d.getPersistentDataContainer().get(POUPEE_KEY, PersistentDataType.STRING);
                if (pname != null) {
                    ItemStack cur = d.getItemStack();
                    if (cur == null || cur.getType().isAir() || getCmd(cur) <= 0) {
                        d.setItemStack(createPoupeeItem(pname));
                    }
                }
                poupeeDisplays.put(poupeeLocKey(b.getLocation()), d);
                n++;
            }
        }
        getLogger().info("[Poupee] rebind " + n + " poupee(s)");
    }

    /** Rebind gamelles (displays + barriers). */
    private void rebindGamelles() {
        gamelleAt.clear();
        int n = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (var e : world.getEntities()) {
                if (!e.getPersistentDataContainer().has(KEY_GAMELLE, PersistentDataType.BYTE)) continue;
                if (!(e instanceof org.bukkit.entity.ItemDisplay) && !(e instanceof org.bukkit.entity.Item)) continue;
                org.bukkit.Location base = e.getLocation().clone().add(0, -0.05, 0);
                org.bukkit.block.Block b = base.getBlock();
                if (b.getType() != Material.BARRIER && b.getType() != Material.STRUCTURE_VOID) {
                    b = e.getLocation().getBlock();
                    if (b.getType().isAir() || b.getType().name().contains("PRESSURE_PLATE")) {
                        b.setType(Material.STRUCTURE_VOID);
                    } else {
                        org.bukkit.block.Block under = e.getLocation().clone().add(0, -0.2, 0).getBlock();
                        if (under.getType().isAir()) {
                            under.setType(Material.STRUCTURE_VOID);
                            b = under;
                        }
                    }
                }
                gamelleAt.put(locKey(b.getLocation()), e.getUniqueId());
                n++;
                if (e instanceof org.bukkit.entity.Item item) {
                    item.setGravity(false);
                    item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    item.setPickupDelay(Integer.MAX_VALUE);
                    try { item.setUnlimitedLifetime(true); } catch (Throwable ignored) {}
                    try { item.setCanPlayerPickup(false); } catch (Throwable ignored) {}
                }
            }
        }
        getLogger().info("[Gamelle] rebind " + n + " / map=" + gamelleAt.size());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPoupeePlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        // 1) Clic droit sur une poupee existante → rotation 45°
        org.bukkit.entity.ItemDisplay existing = null;
        if (event.getClickedBlock().getType() == Material.BARRIER) {
            existing = findPoupeeAt(event.getClickedBlock());
        }
        if (existing != null) {
            event.setCancelled(true);
            float yaw = getPoupeeYaw(existing) + 45f;
            while (yaw > 180f) yaw -= 360f;
            while (yaw < -180f) yaw += 360f;
            applyPoupeeTransform(existing, yaw);
            event.getPlayer().playSound(existing.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.3f);
            event.getPlayer().sendActionBar(Component.text("Poupee tournee", NamedTextColor.LIGHT_PURPLE));
            return;
        }

        // 2) Placement nouvelle poupee
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (!isPoupeeItem(hand)) return;
        event.setCancelled(true);

        org.bukkit.Location place = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        if (!place.getBlock().getType().isAir()) {
            event.getPlayer().sendActionBar(Component.text("Pas la place", NamedTextColor.RED));
            return;
        }
        // face au joueur
        float playerYaw = event.getPlayer().getLocation().getYaw();
        float yaw = playerYaw + 180f;
        while (yaw > 180f) yaw -= 360f;
        while (yaw < -180f) yaw += 360f;
        yaw = Math.round(yaw / 45f) * 45f;

        place.getBlock().setType(Material.BARRIER);
        org.bukkit.Location dloc = place.clone().add(0.5, 0.0, 0.5);
        org.bukkit.entity.ItemDisplay disp = place.getWorld().spawn(dloc, org.bukkit.entity.ItemDisplay.class);
        disp.setPersistent(true);
        disp.setInvulnerable(true);
        disp.setGravity(false);
        try { disp.setBillboard(org.bukkit.entity.Display.Billboard.FIXED); } catch (Throwable ignored) {}
        try { disp.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED); } catch (Throwable ignored) {}

        ItemStack visual = hand.clone();
        visual.setAmount(1);
        disp.setItemStack(visual);
        String pname = null;
        String variantId = null;
        String variantOwner = null;
        if (hand.getItemMeta() != null) {
            pname = hand.getItemMeta().getPersistentDataContainer().get(TETES_PLAYER_KEY, PersistentDataType.STRING);
            variantId = hand.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("crv6", "variant_id"), PersistentDataType.STRING);
            variantOwner = hand.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("crv6", "variant_owner"), PersistentDataType.STRING);
            // legacy keys
            if (variantId == null) {
                String[] keys = {"benji_variant", "bulbily_variant", "cryzopha_variant", "kaulose_variant"};
                String[] owners = {"Benjixh02", "Bulbily", "Cryzopha", "Kaulose"};
                for (int i = 0; i < keys.length; i++) {
                    String vid = hand.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("crv6", keys[i]), PersistentDataType.STRING);
                    if (vid != null) { variantId = vid; variantOwner = owners[i]; break; }
                }
            }
        }
        if (pname == null) pname = Crv6Players.nameFromPoupeeCmd(getCmd(hand));
        if (variantId != null && variantOwner != null) {
            disp.getPersistentDataContainer().set(POUPEE_KEY, PersistentDataType.STRING, "var:" + variantOwner + ":" + variantId);
        } else if (pname != null) {
            disp.getPersistentDataContainer().set(POUPEE_KEY, PersistentDataType.STRING, pname);
        }
                applyPoupeeTransform(disp, yaw);
        poupeeDisplays.put(poupeeLocKey(place), disp);
        hand.setAmount(hand.getAmount() - 1);
        event.getPlayer().playSound(place, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.6f, 1.4f);
        event.getPlayer().sendActionBar(Component.text("Poupee placee", NamedTextColor.LIGHT_PURPLE));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPoupeeBreak(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.BARRIER) return;
        // ne pas casser une machine RF
        org.bukkit.entity.ItemDisplay disp = findPoupeeAt(event.getClickedBlock());
        if (disp == null) return;
        event.setCancelled(true);
        String k = poupeeLocKey(event.getClickedBlock().getLocation());
        poupeeDisplays.remove(k);
        String pname = disp.getPersistentDataContainer().get(POUPEE_KEY, PersistentDataType.STRING);
        disp.remove();
        event.getClickedBlock().setType(Material.AIR);
        if (pname != null) {
            ItemStack give;
            if (pname.startsWith("var:")) {
                String rest = pname.substring(4);
                int sep = rest.indexOf(':');
                if (sep > 0) {
                    String owner = rest.substring(0, sep);
                    String vid = rest.substring(sep + 1);
                    BenjiVariant found = null;
                    for (BenjiVariant v : variantsOf(owner)) if (v.id.equals(vid)) { found = v; break; }
                    give = found != null ? createVariantItem(owner, found) : createPoupeeItem(owner);
                } else {
                    give = createPoupeeItem(pname);
                }
            } else if (pname.startsWith("benji:") || pname.startsWith("bulbily:") || pname.startsWith("cryzopha:") || pname.startsWith("kaulose:")) {
                String owner = pname.startsWith("benji:") ? "Benjixh02" : pname.startsWith("bulbily:") ? "Bulbily" : pname.startsWith("cryzopha:") ? "Cryzopha" : "Kaulose";
                String vid = pname.substring(pname.indexOf(':') + 1);
                BenjiVariant found = null;
                for (BenjiVariant v : variantsOf(owner)) if (v.id.equals(vid)) { found = v; break; }
                give = found != null ? createVariantItem(owner, found) : createPoupeeItem(owner);
            } else {
                give = createPoupeeItem(pname);
            }
            event.getPlayer().getInventory().addItem(give);
            event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 0.6f, 1.2f);
            event.getPlayer().sendActionBar(Component.text("Poupee recuperee", NamedTextColor.GREEN));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPoupeeBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.BARRIER) return;
        org.bukkit.entity.ItemDisplay disp = findPoupeeAt(event.getBlock());
        if (disp == null) return;
        event.setDropItems(false);
        poupeeDisplays.remove(poupeeLocKey(event.getBlock().getLocation()));
        String pname = disp.getPersistentDataContainer().get(POUPEE_KEY, PersistentDataType.STRING);
        disp.remove();
        event.getBlock().setType(Material.AIR);
        if (pname != null) {
            ItemStack give;
            if (pname.startsWith("var:")) {
                String rest = pname.substring(4);
                int sep = rest.indexOf(':');
                if (sep > 0) {
                    String owner = rest.substring(0, sep);
                    String vid = rest.substring(sep + 1);
                    BenjiVariant found = null;
                    for (BenjiVariant v : variantsOf(owner)) if (v.id.equals(vid)) { found = v; break; }
                    give = found != null ? createVariantItem(owner, found) : createPoupeeItem(owner);
                } else {
                    give = createPoupeeItem(pname);
                }
            } else if (pname.startsWith("benji:") || pname.startsWith("bulbily:") || pname.startsWith("cryzopha:") || pname.startsWith("kaulose:")) {
                String owner = pname.startsWith("benji:") ? "Benjixh02" : pname.startsWith("bulbily:") ? "Bulbily" : pname.startsWith("cryzopha:") ? "Cryzopha" : "Kaulose";
                String vid = pname.substring(pname.indexOf(':') + 1);
                BenjiVariant found = null;
                for (BenjiVariant v : variantsOf(owner)) if (v.id.equals(vid)) { found = v; break; }
                give = found != null ? createVariantItem(owner, found) : createPoupeeItem(owner);
            } else {
                give = createPoupeeItem(pname);
            }
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.3, 0.5), give);
        }
    }


    private void openMainMenu(Player player) {
        int count = ItemData.CATEGORIES.size();
        int size = 36; // 4 rows admin layout
        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MenuHolder.Type.MAIN, null, 0), size,
                guiTitle(true, ""));
        // place categories (no glass fillers)

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < count && i < slots.length; i++) {
            String cat = ItemData.CATEGORIES.get(i);
            List<ItemData> list = ItemData.BY_CATEGORY.get(cat);
            inv.setItem(slots[i], categoryIcon(cat, list.size(), i));
        }
        // Close button
        ItemStack close = menuIcon(9110, "Fermer");
        ItemMeta cm = close.getItemMeta();
        cm.displayName(Component.text("Fermer", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        cm.lore(List.of(
                Component.text("Ferme le menu", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        close.setItemMeta(cm);
        inv.setItem(22, close);
        player.openInventory(inv);
    }

    private void openCategory(Player player, String category, int page) {
        List<ItemData> list = ItemData.BY_CATEGORY.get(category);
        if (list == null) return;
        final int perPage = 45;
        int maxPage = Math.max(0, (list.size() - 1) / perPage);
        page = Math.max(0, Math.min(page, maxPage));
        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MenuHolder.Type.CATEGORY, category, page), 54,
                guiTitle(false, category + " · " + (page + 1) + "/" + (maxPage + 1)));
        int start = page * perPage;
        int end = Math.min(start + perPage, list.size());
        for (int i = start; i < end; i++) inv.setItem(i - start, toStack(list.get(i)));

        ItemStack back = menuIcon(9110, "Retour");
        ItemMeta bm = back.getItemMeta();
        bm.displayName(Component.text("Retour", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(bm);
        inv.setItem(49, back);
        // Tetes submenu button (2 slots after back) — Divers only
        if ("Divers".equals(category)) {
            inv.setItem(51, tetesMenuButton(player));
        }
        if (page > 0) {
            ItemStack prev = menuIcon(9111, "Page precedente");
            ItemMeta pm = prev.getItemMeta();
            pm.displayName(Component.text("Page precedente", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            prev.setItemMeta(pm);
            inv.setItem(45, prev);
        }
        if (page < maxPage) {
            ItemStack next = menuIcon(9112, "Page suivante");
            ItemMeta nm = next.getItemMeta();
            nm.displayName(Component.text("Page suivante", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }
        player.openInventory(inv);
    }

    private ItemStack toStack(ItemData data) {
        Material mat = Material.matchMaterial(data.material);
        if (mat == null) mat = Material.PAPER;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();

        // Rename special items
        String display = data.name;
        boolean isGod = ("stick".equals(data.material) && data.cmd == 1)
                || ("wooden_sword".equals(data.material) && data.cmd == 2);
        if (isGod) display = "Paddle";
        boolean isShampoo = "cooked_chicken".equals(data.material) && data.cmd == 2;
        if (isShampoo) display = "Shampoing";
        boolean isBaillonItem = "carved_pumpkin".equals(data.material) && (data.cmd == BAILLON_CMD || data.cmd == RING_GAG_CMD);
        if (isBaillonItem) display = "Baillon";
        if (("carved_pumpkin".equals(data.material) || "leather_helmet".equals(data.material)) && data.cmd == GAS_MASK_CMD) display = "Masque a gaz";
        if (("carved_pumpkin".equals(data.material) || "leather_helmet".equals(data.material)) && data.cmd == SCUBA_CMD) display = "Scaphandre";
        if ("carved_pumpkin".equals(data.material) && data.cmd == FREDDY_CMD) display = "Freddy";
        if ("carved_pumpkin".equals(data.material) && data.cmd == GOLDEN_FREDDY_CMD) display = "Golden Freddy";
        if ("carved_pumpkin".equals(data.material) && data.cmd == LUNETTE_CMD) display = "Lunette";
        if ("carved_pumpkin".equals(data.material) && data.cmd == UTA_CMD) display = "Casque de UTA";
        if ("carved_pumpkin".equals(data.material) && data.cmd == MASKASS_CMD) display = "Masque de Maskass";
        if ("carved_pumpkin".equals(data.material) && data.cmd == RING_GAG_CMD) display = "Ring Gag";
        if ("bowl".equals(data.material) && data.cmd == GAMELLE_CMD_VIDE) display = "Gamelle";
        if ("dropper".equals(data.material) && data.cmd == 1) display = "Generateur a charbon";
        if ("dropper".equals(data.material) && data.cmd == 2) display = "Panneau solaire";
        if ("dropper".equals(data.material) && data.cmd == 3) display = "Batterie RF";
        if ("dropper".equals(data.material) && data.cmd == 4) display = "Collecteur d'oxygen";
        if ("carrot_on_a_stick".equals(data.material) && data.cmd == TAZER_CMD) display = "Tazer";
        if ("cookie".equals(data.material) && data.cmd == CHIANTOS_FERME_CMD) display = "Chiantos ferme";
        if ("cookie".equals(data.material) && data.cmd == CHIANTOS_OUVERT_CMD) display = "Chiantos ouvert";
        if ("carrot_on_a_stick".equals(data.material) && data.cmd == TAZER_SELECT_CMD) display = "Tazer Select";
        if ("carrot_on_a_stick".equals(data.material) && BOTTLE_CMDS.contains(data.cmd)) {
            display = switch (data.cmd) {
                case 10 -> "Bouteille O2";
                case 11 -> "Bouteille O2 I";
                case 12 -> "Bouteille O2 II";
                case 13 -> "Bouteille O2 III";
                default -> display;
            };
        }

        meta.displayName(Component.text(display, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

        java.util.ArrayList<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("CMD: " + data.cmd, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));

        if ("carved_pumpkin".equals(data.material) && HIDE_NAMETAG_CMDS.contains(data.cmd)) {
            lore.add(Component.text("Effet: pseudo invisible", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(equipe sur la tete)", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
        }
        if ("carved_pumpkin".equals(data.material) && JUMP_CMDS.contains(data.cmd)) {
            lore.add(Component.text("Effet: super saut", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Effet: -50% degats de chute", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(equipe sur la tete)", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false));
        }
        // life crystal lore
        if (("emerald".equals(data.material) && data.cmd == 2)
                || ("stick".equals(data.material) && data.cmd == 2)) {
            lore.add(Component.text("Effet: +3 coeurs", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(dans l'inventaire)", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
        }
        // shampoing lore
        if (isShampoo) {
            lore.add(Component.text("Craft: + laine coloree", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("-> laine blanche", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }

        if ("carrot_on_a_stick".equals(data.material) && data.cmd == TAZER_CMD) {
            lore.add(Component.text("Clic droit sur un joueur", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("pour le selectionner", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if ("carrot_on_a_stick".equals(data.material) && BOTTLE_CMDS.contains(data.cmd)) {
            int max = BOTTLE_MAX.getOrDefault(data.cmd, 100);
            lore.add(Component.text("Oxygene: " + max + "/" + max, NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Rechargeable (craft /admin)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if ("carved_pumpkin".equals(data.material) && data.cmd == GAS_MASK_CMD) {
            lore.add(Component.text("Immunite poison (consomme O2)", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Necessite une Bouteille O2", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if ("carved_pumpkin".equals(data.material) && data.cmd == SCUBA_CMD) {
            lore.add(Component.text("Respiration sous-marine (O2)", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Sans bouteille: +50% reserve air", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if ("carved_pumpkin".equals(data.material) && (data.cmd == BAILLON_CMD || data.cmd == RING_GAG_CMD)) {
            lore.add(Component.text("Effet: chat = Mphhh...", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Effet: micro mute (VoiceChat)", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(equipe sur la tete)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        // zabuza lore
        if ("netherite_axe".equals(data.material) && data.cmd == ZABUZA_CMD) {
            lore.add(Component.text("Effet: -20% vitesse", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Effet: +10% degats (vs hache netherite)", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(en main)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        // desassembleur lore
        if ("netherite_pickaxe".equals(data.material) && data.cmd == DESASSEMBLEUR_CMD) {
            lore.add(Component.text("Effet: one-shot pierre & minerais", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(en main)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        // paddle lore
        if (isGod) {
            lore.add(Component.text("Effet: 0 degat", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Effet: obscurite 0.5s", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        }
        // Spreadsheet catalog overrides / extends description + reference head
        java.util.List<Component> cat = catalogLore(data.material, data.cmd);
        if (cat != null) {
            // Put catalog description first, keep effect lines already added, then catalog ref at end
            java.util.List<Component> merged = new java.util.ArrayList<>(cat);
            // Avoid duplicating if catalog already has effects - append only non-catalog effect lines that are unique
            for (Component c : lore) {
                String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c);
                if (plain.startsWith("Effet:") || plain.startsWith("(equipe") || plain.startsWith("(en main")
                        || plain.startsWith("(dans")) {
                    // skip if catalog already has an Effet line
                    boolean hasEffet = cat.stream().anyMatch(x ->
                            net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                                    .serialize(x).startsWith("Effet:"));
                    if (hasEffet && plain.startsWith("Effet:")) continue;
                    if (!merged.contains(c)) merged.add(c);
                }
            }
            lore.clear();
            lore.addAll(merged);
        }
        lore.add(Component.text("Click to receive", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        try {
            CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setFloats(List.of((float) data.cmd));
            meta.setCustomModelDataComponent(cmdComp);
        } catch (Throwable t) {
            try { meta.setCustomModelData(data.cmd); } catch (Throwable ignored) {}
        }
        applyFoodComponent(meta, data);
        FoodData fd = FoodData.match(data.material, data.cmd);
        if (fd != null) {
            java.util.List<Component> lore2 = meta.lore();
            if (lore2 == null) lore2 = new java.util.ArrayList<>();
            else lore2 = new java.util.ArrayList<>(lore2);
            lore2.add(0, Component.text("Faim: " + (fd.nutrition / 2.0) + " | Sat: " + fd.saturation, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore2);
        }
        stack.setItemMeta(meta);
        if ("leather_helmet".equals(data.material) && meta instanceof LeatherArmorMeta leather) {
            leather.setColor(Color.fromRGB(255, 255, 255));
            stack.setItemMeta(leather);
            // re-apply display after color (leather setColor may need meta refresh)
            ItemMeta m2 = stack.getItemMeta();
            if (m2 != null) {
                m2.displayName(meta.displayName());
                m2.lore(meta.lore());
                try {
                    CustomModelDataComponent cmdComp = m2.getCustomModelDataComponent();
                    cmdComp.setFloats(List.of((float) data.cmd));
                    m2.setCustomModelDataComponent(cmdComp);
                } catch (Throwable t) {
                    try { m2.setCustomModelData(data.cmd); } catch (Throwable ignored) {}
                }
                stack.setItemMeta(m2);
            }
        }
                if ("carrot_on_a_stick".equals(data.material) && BOTTLE_CMDS.contains(data.cmd)) {
            applyBottleDurability(stack, data.cmd, false);
        }
        if ("cookie".equals(data.material) && (data.cmd == CHIANTOS_FERME_CMD || data.cmd == CHIANTOS_OUVERT_CMD)) {
            ItemMeta cm = stack.getItemMeta();
            if (cm != null) {
                cm.lore(buildChiantosLore(data.cmd));
                if (data.cmd == CHIANTOS_OUVERT_CMD) {
                    try {
                        FoodComponent fc = cm.getFood();
                        fc.setNutrition(20);
                        fc.setSaturation(20f);
                        fc.setCanAlwaysEat(true);
                        cm.setFood(fc);
                    } catch (Throwable ignored) {}
                }
                stack.setItemMeta(cm);
            }
        }
        return stack;
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // helmet slot is 39 in player inventory, or raw slot in armor
        Bukkit.getScheduler().runTask(this, () -> {
            updateNametag(player);
            updateBaillon(player);
            updateUta(player);
            updateMaskass(player);
            updateChatEars(player);
            updatePaille(player);
            updateFreddySlow(player);
            updateFreddyOverlay(player);
            updateLunette(player);
            updateOxygenGear(player);
            freezeGamelles();
            if (oxygenTick % 4 == 0) updateHeldTazer(player);
            updateJump(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Bukkit.getScheduler().runTask(this, () -> {
            updateNametag(player);
            updateBaillon(player);
            updateUta(player);
            updateMaskass(player);
            updateChatEars(player);
            updatePaille(player);
            updateFreddySlow(player);
            updateFreddyOverlay(player);
            updateLunette(player);
            updateOxygenGear(player);
            freezeGamelles();
            if (oxygenTick % 4 == 0) updateHeldTazer(player);
            updateJump(player);
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (holder.type == MenuHolder.Type.MAIN) {
            int slot = event.getSlot();
            if (slot == 22) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
                player.closeInventory();
                return;
            }
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
            for (int i = 0; i < slots.length && i < ItemData.CATEGORIES.size(); i++) {
                if (slots[i] == slot) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    openCategory(player, ItemData.CATEGORIES.get(i), 0);
                    return;
                }
            }
            return;
        }
        if (holder.type == MenuHolder.Type.CATEGORY) {
            int slot = event.getSlot();
            if (slot == 49) { player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openMainMenu(player); return; }
            if (slot == 51 && "Divers".equals(holder.category)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openTetesMenu(player, 0);
                return;
            }
            if (slot == 45) { player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openCategory(player, holder.category, holder.page - 1); return; }
            if (slot == 53) { player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openCategory(player, holder.category, holder.page + 1); return; }
            if (slot < 45 && player.hasPermission("crv6.give")) {
                List<ItemData> list = ItemData.BY_CATEGORY.get(holder.category);
                int index = holder.page * 45 + slot;
                if (list != null && index >= 0 && index < list.size()) {
                    ItemStack give = toStack(list.get(index));
                    give.setAmount(1);
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(give);
                    if (!overflow.isEmpty()) {
                        for (ItemStack left : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), left);
                        }
                    }
                    String name = list.get(index).name;
                    if (("stick".equals(list.get(index).material) && list.get(index).cmd == 1)
                            || ("wooden_sword".equals(list.get(index).material) && list.get(index).cmd == 2)) {
                        name = "Paddle";
                    }
                    if ("cooked_chicken".equals(list.get(index).material) && list.get(index).cmd == 2) {
                        name = "Shampoing";
                    }
                    if ("carved_pumpkin".equals(list.get(index).material) && list.get(index).cmd == 2) {
                        name = "Baillon";
                    }
                    if ("carved_pumpkin".equals(list.get(index).material) && list.get(index).cmd == 7) {
                        name = "Masque a gaz";
                    }
                    if ("carved_pumpkin".equals(list.get(index).material) && list.get(index).cmd == 11) {
                        name = "Scaphandre";
                    }
                    if ("carrot_on_a_stick".equals(list.get(index).material) && list.get(index).cmd == 5) {
                        name = "Tazer";
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    player.sendMessage(Component.text("+ ", NamedTextColor.GREEN)
                            .append(Component.text(name, NamedTextColor.GOLD)));
                }
            }
        }

        if (holder.type == MenuHolder.Type.TETES) {
            int slot = event.getSlot();
            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openCategory(player, "Divers", 0);
                return;
            }
            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openTetesMenu(player, Math.max(0, holder.page - 1));
                return;
            }
            if (slot == 53) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openTetesMenu(player, holder.page + 1);
                return;
            }
            if (slot < 45) {
                ItemStack icon = event.getCurrentItem();
                if (icon == null || icon.getType().isAir()) return;
                ItemMeta cm = icon.getItemMeta();
                if (cm == null) return;
                String pname = cm.getPersistentDataContainer().get(TETES_PLAYER_KEY, PersistentDataType.STRING);
                if (pname == null) return;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openTeteDetail(player, pname);
            }
            return;
        }

        if (holder.type == MenuHolder.Type.TETE_DETAIL) {
            int slot = event.getSlot();
            ItemStack cur = event.getCurrentItem();
            if (cur == null || cur.getType().isAir()) return;
            ItemMeta cm = cur.getItemMeta();
            if (cm == null) return;
            String action = cm.getPersistentDataContainer().get(TETES_ACTION_KEY, PersistentDataType.STRING);
            String pname = cm.getPersistentDataContainer().get(TETES_PLAYER_KEY, PersistentDataType.STRING);
            // Retour UNIQUEMENT si action == back (le bouton retour le porte)
            if ("back".equals(action)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openTetesMenu(player, 0);
                return;
            }
            if (action == null) return;
            if (!player.hasPermission("crv6.give")) {
                player.sendMessage(Component.text("Permission manquante: crv6.give", NamedTextColor.RED));
                return;
            }
            if (action.startsWith("var:")) {
                // var:PlayerName:variantId
                String rest = action.substring(4);
                int sep = rest.indexOf(':');
                if (sep > 0) {
                    String owner = rest.substring(0, sep);
                    String vid = rest.substring(sep + 1);
                    BenjiVariant found = null;
                    for (BenjiVariant v : variantsOf(owner)) {
                        if (v.id.equals(vid)) { found = v; break; }
                    }
                    if (found != null) {
                        player.getInventory().addItem(createVariantItem(owner, found));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                        player.sendMessage(Component.text("+ " + owner + " — " + found.label, NamedTextColor.LIGHT_PURPLE));
                    }
                }
                return;
            }
            // legacy prefixes
            if (action.startsWith("benji:") || action.startsWith("bulbily:") || action.startsWith("cryzopha:") || action.startsWith("kaulose:")) {
                String owner = action.startsWith("benji:") ? "Benjixh02" : action.startsWith("bulbily:") ? "Bulbily" : action.startsWith("cryzopha:") ? "Cryzopha" : "Kaulose";
                int cut = action.indexOf(':') + 1;
                String vid = action.substring(cut);
                BenjiVariant found = null;
                for (BenjiVariant v : variantsOf(owner)) {
                    if (v.id.equals(vid)) { found = v; break; }
                }
                if (found != null) {
                    player.getInventory().addItem(createVariantItem(owner, found));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    player.sendMessage(Component.text("+ " + owner + " — " + found.label, NamedTextColor.LIGHT_PURPLE));
                }
                return;
            }
            if (pname == null) return;
            if ("doll".equals(action)) {
                player.getInventory().addItem(createPoupeeItem(pname));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                player.sendMessage(Component.text("+ Poupee de " + pname, NamedTextColor.GOLD));
            } else {
                final String nameFinal = pname;
                player.sendMessage(Component.text("Preparation de la tete...", NamedTextColor.GRAY));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    ItemStack head;
                    try {
                        head = createOfflinePlayerHead(nameFinal);
                    } catch (Throwable t) {
                        head = createPoupeeItem(nameFinal);
                    }
                    final ItemStack toGive = head;
                    Bukkit.getScheduler().runTask(this, () -> {
                        if (!player.isOnline()) return;
                        player.getInventory().addItem(toGive);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                        player.sendMessage(Component.text("+ Tete de " + nameFinal, NamedTextColor.AQUA));
                    });
                });
            }
            return;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }
}
