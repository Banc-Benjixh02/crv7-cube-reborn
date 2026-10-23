package fr.crv6.menu;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple Voice Chat mute via BukkitVoicechatService.registerPlugin (correct API path).
 */
public final class BaillonVoiceHook {

    private static final Set<UUID> MUTED = ConcurrentHashMap.newKeySet();
    private static volatile boolean eventsRegistered = false;
    private static Plugin host;

    private BaillonVoiceHook() {}

    public static void setMuted(UUID uuid, boolean muted) {
        // Pas de log ici: appele souvent (tick) → spam console
        if (muted) MUTED.add(uuid);
        else MUTED.remove(uuid);
    }

    public static boolean isMuted(UUID uuid) {
        return MUTED.contains(uuid);
    }

    public static boolean isActive() {
        return eventsRegistered;
    }

    public static void tryRegister(Plugin plugin) {
        host = plugin;
        if (Bukkit.getPluginManager().getPlugin("voicechat") == null) {
            plugin.getLogger().info("[Baillon] Simple Voice Chat plugin not found");
            return;
        }

        // Method 1: BukkitVoicechatService.registerPlugin (official)
        try {
            Class<?> serviceClass = Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(serviceClass);
            if (rsp != null) {
                Object service = rsp.getProvider();
                Object proxy = createPluginProxy(plugin);
                Method registerPlugin = serviceClass.getMethod("registerPlugin",
                        Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin"));
                registerPlugin.invoke(service, proxy);
                plugin.getLogger().info("[Baillon] registerPlugin() via BukkitVoicechatService OK");
                return;
            } else {
                plugin.getLogger().info("[Baillon] BukkitVoicechatService not available yet");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[Baillon] BukkitVoicechatService: " + t.getMessage());
        }

        // Method 2: ServicesManager + hope SVC rescans (fallback)
        try {
            Class<?> pluginIface = Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin");
            Object proxy = createPluginProxy(plugin);
            Bukkit.getServicesManager().register(
                    (Class) pluginIface, proxy, plugin, org.bukkit.plugin.ServicePriority.Highest);
            plugin.getLogger().info("[Baillon] Fallback ServicesManager register");
        } catch (Throwable t) {
            plugin.getLogger().warning("[Baillon] Fallback failed: " + t.getMessage());
        }
    }

    private static Object createPluginProxy(Plugin plugin) throws Exception {
        Class<?> pluginIface = Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin");
        return Proxy.newProxyInstance(
                pluginIface.getClassLoader(),
                new Class<?>[]{pluginIface},
                new VoiceHandler(plugin)
        );
    }

    private static final class VoiceHandler implements InvocationHandler {
        private final Plugin plugin;

        VoiceHandler(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            try {
                String name = method.getName();
                if ("getPluginId".equals(name)) return "crv6_baillon";
                if ("initialize".equals(name)) {
                    plugin.getLogger().info("[Baillon] VoicechatPlugin.initialize()");
                    return null;
                }
                if ("registerEvents".equals(name) && args != null && args.length >= 1) {
                    registerMicEvent(args[0]);
                    return null;
                }
                if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                if ("equals".equals(name)) return proxy == (args != null ? args[0] : null);
                if ("toString".equals(name)) return "CRV6BaillonVoiceHook";
            } catch (Throwable t) {
                plugin.getLogger().warning("[Baillon] invoke " + method.getName() + ": " + t.getMessage());
            }
            return null;
        }

        private void registerMicEvent(Object registration) throws Exception {
            Class<?> micEvent = Class.forName("de.maxhenkel.voicechat.api.events.MicrophonePacketEvent");
            Consumer<Object> handler = BaillonVoiceHook::onMicPacket;
            for (Method m : registration.getClass().getMethods()) {
                if (!"registerEvent".equals(m.getName()) || m.getParameterCount() != 2) continue;
                Class<?>[] pts = m.getParameterTypes();
                if (!pts[0].isAssignableFrom(Class.class)) continue;
                m.invoke(registration, micEvent, handler);
                eventsRegistered = true;
                plugin.getLogger().info("[Baillon] MicrophonePacketEvent REGISTERED - mute should work");
                return;
            }
            plugin.getLogger().warning("[Baillon] registerEvent method not found on " + registration.getClass().getName());
        }
    }

    private static void onMicPacket(Object event) {
        try {
            Object conn = event.getClass().getMethod("getSenderConnection").invoke(event);
            if (conn == null) return;
            Object player = conn.getClass().getMethod("getPlayer").invoke(conn);
            Object uuidObj = player.getClass().getMethod("getUuid").invoke(player);
            if (!(uuidObj instanceof UUID id)) return;
            if (!MUTED.contains(id)) return;

            boolean cancelled = false;
            try {
                event.getClass().getMethod("cancel").invoke(event);
                cancelled = true;
            } catch (NoSuchMethodException e) {
                try {
                    event.getClass().getMethod("setCancelled", boolean.class).invoke(event, true);
                    cancelled = true;
                } catch (Throwable ignored) {}
            }
            if (host != null && cancelled) {
                // throttle log? skip spam
            }
        } catch (Throwable ignored) {}
    }
}
