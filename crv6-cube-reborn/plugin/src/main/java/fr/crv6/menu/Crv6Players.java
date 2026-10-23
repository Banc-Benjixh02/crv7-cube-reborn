package fr.crv6.menu;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Crv6Players {

    public static final class Entry {
        public final String name;
        public final List<String> versions;
        public final Boolean carte;
        public final Boolean actif;

        public Entry(String name, List<String> versions, Boolean carte, Boolean actif) {
            this.name = name;
            this.versions = versions != null ? versions : List.of();
            this.carte = carte;
            this.actif = actif;
        }

        public String versionsLabel() {
            if (versions.isEmpty()) return "—";
            return String.join(", ", versions);
        }

        public String carteLabel() {
            if (carte == null) return "Inconnu";
            return carte ? "Oui" : "Non";
        }

        public String actifLabel() {
            if (actif == null) return "Inconnu";
            return actif ? "Oui" : "Non";
        }
    }

    private static final List<Entry> ALL = new ArrayList<>();
    private static final Map<String, Entry> BY_NAME = new HashMap<>();
    private static final Map<String, Integer> POUPEE_CMD = new HashMap<>();
    private static final Map<Integer, String> CMD_TO_NAME = new HashMap<>();

    public static void load(JavaPlugin plugin) {
        ALL.clear();
        BY_NAME.clear();
        try (InputStream in = plugin.getResource("crv6_players.json")) {
            if (in == null) {
                plugin.getLogger().warning("[Tetes] crv6_players.json introuvable");
                return;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
            parseJsonArray(sb.toString());
        } catch (Exception ex) {
            plugin.getLogger().warning("[Tetes] load: " + ex.getMessage());
        }
        ALL.sort(Comparator.comparing(e -> e.name.toLowerCase(Locale.ROOT)));
        for (Entry e : ALL) {
            BY_NAME.put(e.name.toLowerCase(Locale.ROOT), e);
        }

        // poupee CMD mapping
        try (InputStream pin = plugin.getResource("poupee_mapping.json")) {
            if (pin != null) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(pin, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                }
                String json = sb.toString();
                int idx = 0;
                while ((idx = json.indexOf("\"name\"", idx)) >= 0) {
                    int n1 = json.indexOf('"', idx + 6);
                    if (n1 < 0) break;
                    int n2 = json.indexOf('"', n1 + 1);
                    if (n2 < 0) break;
                    String n = json.substring(n1 + 1, n2);
                    int cIdx = json.indexOf("\"cmd\"", n2);
                    if (cIdx < 0 || cIdx > n2 + 80) { idx = n2 + 1; continue; }
                    int colon = json.indexOf(':', cIdx);
                    int end = colon + 1;
                    while (end < json.length() && (Character.isWhitespace(json.charAt(end)) || json.charAt(end) == ':')) end++;
                    int numEnd = end;
                    while (numEnd < json.length() && Character.isDigit(json.charAt(numEnd))) numEnd++;
                    if (numEnd > end) {
                        int cmd = Integer.parseInt(json.substring(end, numEnd));
                        POUPEE_CMD.put(n.toLowerCase(Locale.ROOT), cmd);
                        CMD_TO_NAME.put(cmd, n);
                    }
                    idx = n2 + 1;
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("[Poupee] mapping: " + ex.getMessage());
        }
        plugin.getLogger().info("[Tetes] " + ALL.size() + " joueurs CR, " + POUPEE_CMD.size() + " poupees");
    }

    /** Parse minimal JSON array of player objects without Gson. */
    private static void parseJsonArray(String json) {
        // split objects roughly
        Pattern obj = Pattern.compile("\\{([^{}]*)\\}");
        Matcher m = obj.matcher(json);
        while (m.find()) {
            String body = m.group(1);
            String name = extractString(body, "name");
            if (name == null || name.isBlank()) continue;
            List<String> versions = extractStringArray(body, "versions");
            Boolean carte = extractBool(body, "carte");
            Boolean actif = extractBool(body, "actif");
            ALL.add(new Entry(name, versions, carte, actif));
        }
    }

    private static String extractString(String body, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(body);
        return m.find() ? m.group(1) : null;
    }

    private static List<String> extractStringArray(String body, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher m = p.matcher(body);
        if (!m.find()) return List.of();
        List<String> out = new ArrayList<>();
        Matcher sm = Pattern.compile("\"([^\"]+)\"").matcher(m.group(1));
        while (sm.find()) out.add(sm.group(1));
        return out;
    }

    private static Boolean extractBool(String body, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false|null)");
        Matcher m = p.matcher(body);
        if (!m.find()) return null;
        String v = m.group(1);
        if ("null".equals(v)) return null;
        return Boolean.parseBoolean(v);
    }

    public static List<Entry> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static Entry get(String name) {
        if (name == null) return null;
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    public static int poupeeCmd(String name) {
        if (name == null) return 0;
        return POUPEE_CMD.getOrDefault(name.toLowerCase(Locale.ROOT), 0);
    }

    public static String nameFromPoupeeCmd(int cmd) {
        return CMD_TO_NAME.get(cmd);
    }

    public static boolean isPoupeeCmd(int cmd) {
        return CMD_TO_NAME.containsKey(cmd);
    }
}
