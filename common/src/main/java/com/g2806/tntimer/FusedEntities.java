package com.g2806.tntimer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;

import java.util.ArrayList;
import java.util.List;

/** Finds everything with a ticking explosion fuse (primed TNT). */
public final class FusedEntities {

    /** Fuse value for entities that are not counting down. */
    public static final int NO_FUSE = -1;

    private FusedEntities() {
    }

    /** All entities currently counting down, in no particular order. */
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
        if (entity instanceof PrimedTnt) {
            return ((PrimedTnt) entity).getLife();
        }
        return NO_FUSE;
    }
}
