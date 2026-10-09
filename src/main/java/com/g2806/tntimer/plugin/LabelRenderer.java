package com.g2806.tntimer.plugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** Shows a text label above an entity. Called every tick for each fused entity. */
interface LabelRenderer {

    void show(Entity entity, String text);

    /** Removes labels of entities that weren't shown this tick (exploded, unloaded, defused). */
    void endTick();

    void removeAll();

    /** Whether this renderer can hide labels from single players. */
    boolean supportsPerPlayer();

    void setHidden(Player player, boolean hidden);
}
