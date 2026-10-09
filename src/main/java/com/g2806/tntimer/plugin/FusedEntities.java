package com.g2806.tntimer.plugin;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

import java.lang.reflect.Method;

/**
 * Finds entities with a ticking fuse: primed TNT on every version, plus primed sulfur cubes
 * on servers that have them (Minecraft 26.2+, looked up by reflection so the plugin still
 * loads on older servers). An unlit sulfur cube reports a fuse of -1.
 */
final class FusedEntities {

    /** org.bukkit.entity.SulfurCube and its getFuseTicks(), or null before 26.2. */
    private static final Class<? extends Entity> SULFUR_CUBE;
    private static final Method SULFUR_CUBE_FUSE;

    static {
        Class<? extends Entity> type;
        Method fuse;
        try {
            type = Class.forName("org.bukkit.entity.SulfurCube").asSubclass(Entity.class);
            fuse = type.getMethod("getFuseTicks");
        } catch (ReflectiveOperationException | ClassCastException e) {
            type = null;
            fuse = null;
        }
        SULFUR_CUBE = type;
        SULFUR_CUBE_FUSE = fuse;
    }

    private FusedEntities() {
    }

    static boolean sulfurCubesSupported() {
        return SULFUR_CUBE != null;
    }

    /** Calls {@code visitor} for every entity in {@code world} that is counting down. */
    static void forEach(World world, boolean includeSulfurCubes, Visitor visitor) {
        for (TNTPrimed tnt : world.getEntitiesByClass(TNTPrimed.class)) {
            visitor.visit(tnt, tnt.getFuseTicks());
        }
        if (includeSulfurCubes && SULFUR_CUBE != null) {
            for (Entity cube : world.getEntitiesByClass(SULFUR_CUBE)) {
                int fuse = cubeFuse(cube);
                if (fuse > 0) visitor.visit(cube, fuse);
            }
        }
    }

    private static int cubeFuse(Entity cube) {
        try {
            return (Integer) SULFUR_CUBE_FUSE.invoke(cube);
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }

    interface Visitor {
        void visit(Entity entity, int fuseTicks);
    }
}
