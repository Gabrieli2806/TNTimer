package com.g2806.tntimer.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side TNTimer: every tick, labels each lit TNT (and primed sulfur cube) with its
 * remaining fuse. Players see it without installing anything.
 */
public final class TNTimerPlugin extends JavaPlugin implements Listener {

    private final Set<UUID> hidden = new HashSet<>();
    private Settings settings;
    private LabelRenderer renderer;
    private BukkitTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        start();
    }

    @Override
    public void onDisable() {
        stop();
    }

    private void start() {
        reloadConfig();
        settings = new Settings(getConfig());
        boolean displays = settings.mode != Settings.Mode.NAME && DisplayLabels.supported();
        if (settings.mode == Settings.Mode.DISPLAY && !displays) {
            getLogger().warning("mode: display needs Minecraft 1.19.4+; using entity names instead.");
        }
        renderer = displays ? new DisplayLabels(this, settings) : new NameLabels();
        for (UUID id : hidden) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) renderer.setHidden(player, true);
        }
        getLogger().info("Showing timers with " + (displays ? "text displays" : "entity names")
                + (FusedEntities.sulfurCubesSupported() && settings.sulfurCubes ? ", including sulfur cubes" : ""));
        if (settings.enabled) {
            task = Bukkit.getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
        }
    }

    private void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (renderer != null) renderer.removeAll();
    }

    private void tick() {
        for (World world : Bukkit.getWorlds()) {
            FusedEntities.forEach(world, settings.sulfurCubes, (Entity entity, int fuse) ->
                    renderer.show(entity, TimerFormat.format(fuse, settings.showOnlySeconds)));
        }
        renderer.endTick();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (renderer instanceof DisplayLabels) ((DisplayLabels) renderer).onJoin(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "toggle";
        if (sub.equals("reload")) {
            if (!sender.hasPermission("tntimer.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                return true;
            }
            stop();
            start();
            sender.sendMessage(ChatColor.GREEN + "TNTimer reloaded.");
            return true;
        }
        if (sub.equals("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can toggle their timers.");
                return true;
            }
            if (!sender.hasPermission("tntimer.toggle")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                return true;
            }
            if (!renderer.supportsPerPlayer()) {
                sender.sendMessage(ChatColor.YELLOW + "Timers can only be hidden per player on Minecraft 1.19.4+ (mode: display).");
                return true;
            }
            Player player = (Player) sender;
            boolean hide = hidden.add(player.getUniqueId());
            if (!hide) hidden.remove(player.getUniqueId());
            renderer.setHidden(player, hide);
            sender.sendMessage(ChatColor.GOLD + "TNT timers " + (hide ? "hidden" : "shown") + ".");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return Collections.emptyList();
        List<String> options = new ArrayList<>();
        if ("toggle".startsWith(args[0])) options.add("toggle");
        if (sender.hasPermission("tntimer.reload") && "reload".startsWith(args[0])) options.add("reload");
        return options;
    }
}
