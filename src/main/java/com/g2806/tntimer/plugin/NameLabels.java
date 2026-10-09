package com.g2806.tntimer.plugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Uses the entity's own custom name, which every client draws as a nametag. Works on every
 * server version, but the label is the same for all players. The original name is restored
 * when the countdown ends.
 */
final class NameLabels implements LabelRenderer {

    private final Map<Entity, Original> labelled = new HashMap<>();
    private final Set<Entity> shownThisTick = new HashSet<>();

    @Override
    public void show(Entity entity, String text) {
        if (!labelled.containsKey(entity)) {
            labelled.put(entity, new Original(entity.getCustomName(), entity.isCustomNameVisible()));
        }
        entity.setCustomName(text);
        entity.setCustomNameVisible(true);
        shownThisTick.add(entity);
    }

    @Override
    public void endTick() {
        for (Iterator<Map.Entry<Entity, Original>> it = labelled.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Entity, Original> entry = it.next();
            if (!shownThisTick.contains(entry.getKey())) {
                restore(entry.getKey(), entry.getValue());
                it.remove();
            }
        }
        shownThisTick.clear();
    }

    @Override
    public void removeAll() {
        for (Map.Entry<Entity, Original> entry : labelled.entrySet()) {
            restore(entry.getKey(), entry.getValue());
        }
        labelled.clear();
        shownThisTick.clear();
    }

    private static void restore(Entity entity, Original original) {
        if (!entity.isValid()) return;
        entity.setCustomName(original.name);
        entity.setCustomNameVisible(original.visible);
    }

    @Override
    public boolean supportsPerPlayer() {
        return false;
    }

    @Override
    public void setHidden(Player player, boolean hidden) {
    }

    private static final class Original {
        final String name;
        final boolean visible;

        Original(String name, boolean visible) {
            this.name = name;
            this.visible = visible;
        }
    }
}
