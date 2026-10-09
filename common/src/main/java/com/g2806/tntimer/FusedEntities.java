package com.g2806.tntimer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds everything with a ticking explosion fuse: primed TNT and primed sulfur cubes
 * carrying TNT. The sulfur cube's fuse is synced to the client when it gets primed and
 * then counts down locally, just like PrimedTnt.
 */
public final class FusedEntities {

    /** Fuse value for entities that are not counting down. */
    public static final int NO_FUSE = -1;

    private FusedEntities() {
    }

    public static List<Entity> collect(ClientLevel level) {
        List<Entity> list = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (fuseOf(entity) != NO_FUSE) {
                list.add(entity);
            }
        }
        return list;
    }

    /** Remaining fuse in ticks, or {@link #NO_FUSE} if the entity is not about to explode. */
    public static int fuseOf(Entity entity) {
        if (entity instanceof PrimedTnt tnt) {
            return tnt.getFuse();
        }
        if (entity instanceof SulfurCube cube && cube.canExplode() && cube.isPrimed() && cube.getFuse() > 0) {
            return cube.getFuse();
        }
        return NO_FUSE;
    }
}
