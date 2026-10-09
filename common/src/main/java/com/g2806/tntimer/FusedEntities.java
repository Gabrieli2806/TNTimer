package com.g2806.tntimer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Finds everything with a ticking explosion fuse (primed TNT). */
public final class FusedEntities {

    /** Fuse value for entities that are not counting down. */
    public static final int NO_FUSE = -1;

    private FusedEntities() {
    }

    /** All entities currently counting down, in no particular order. */
    public static List<Entity> collect(World world) {
        List<Entity> list = new ArrayList<>();
        for (Entity entity : world.loadedEntityList) {
            if (fuseOf(entity) != NO_FUSE) {
                list.add(entity);
            }
        }
        return list;
    }

    /** Remaining fuse in ticks, or {@link #NO_FUSE} if the entity is not about to explode. */
    public static int fuseOf(Entity entity) {
        if (entity instanceof EntityTNTPrimed) {
            return ((EntityTNTPrimed) entity).fuse;
        }
        return NO_FUSE;
    }
}
