package com.g2806.tntimer.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A floating text display riding each fused entity (Minecraft 1.19.4+), so it follows the
 * entity smoothly and never touches its name. Displays aren't saved with the world, and
 * players can hide them for themselves. Only loaded when the server has TextDisplay.
 */
final class DisplayLabels implements LabelRenderer {

    private final Plugin plugin;
    private final Settings settings;
    private final Map<Entity, TextDisplay> displays = new HashMap<>();
    private final Set<Entity> shownThisTick = new HashSet<>();
    private final Set<UUID> hiddenFor = new HashSet<>();

    DisplayLabels(Plugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    static boolean supported() {
        try {
            Class.forName("org.bukkit.entity.TextDisplay");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void show(Entity entity, String text) {
        TextDisplay display = displays.get(entity);
        if (display == null || !display.isValid()) {
            display = spawn(entity);
            displays.put(entity, display);
        }
        if (!text.equals(display.getText())) display.setText(text);
        shownThisTick.add(entity);
    }

    private TextDisplay spawn(Entity entity) {
        // No spawn(..., Consumer) here: its Consumer type changed in 1.20.2, which would break 1.19.4 - 1.20.1.
        TextDisplay display = entity.getWorld().spawn(entity.getLocation(), TextDisplay.class);
        display.setPersistent(false);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(settings.seeThrough);
        display.setShadowed(settings.shadow);
        display.setDefaultBackground(false);
        display.setBackgroundColor(settings.background ? Color.fromARGB(0x40, 0, 0, 0) : Color.fromARGB(0, 0, 0, 0));
        display.setTransformation(new Transformation(new Vector3f(0, settings.offset, 0), new AxisAngle4f(),
                new Vector3f(1, 1, 1), new AxisAngle4f()));
        entity.addPassenger(display);
        for (UUID id : hiddenFor) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) player.hideEntity(plugin, display);
        }
        return display;
    }

    @Override
    public void endTick() {
        for (Iterator<Map.Entry<Entity, TextDisplay>> it = displays.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Entity, TextDisplay> entry = it.next();
            if (!shownThisTick.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
        shownThisTick.clear();
    }

    @Override
    public void removeAll() {
        for (TextDisplay display : displays.values()) {
            display.remove();
        }
        displays.clear();
        shownThisTick.clear();
    }

    @Override
    public boolean supportsPerPlayer() {
        return true;
    }

    @Override
    public void setHidden(Player player, boolean hidden) {
        if (hidden) hiddenFor.add(player.getUniqueId());
        else hiddenFor.remove(player.getUniqueId());
        for (TextDisplay display : displays.values()) {
            if (hidden) player.hideEntity(plugin, display);
            else player.showEntity(plugin, display);
        }
    }

    /** Re-applies a player's hidden state after they rejoin. */
    void onJoin(Player player) {
        if (!hiddenFor.contains(player.getUniqueId())) return;
        for (TextDisplay display : displays.values()) {
            player.hideEntity(plugin, display);
        }
    }
}
