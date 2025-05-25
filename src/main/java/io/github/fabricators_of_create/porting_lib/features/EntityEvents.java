package io.github.fabricators_of_create.porting_lib.features;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class EntityEvents {
    public static final Event<JoinWorld> ON_JOIN_WORLD = EventFactory.createArrayBacked(JoinWorld.class, callbacks -> (entity, world, loadedFromDisk) -> {
        for (JoinWorld callback : callbacks)
            if (!callback.onJoinWorld(entity, world, loadedFromDisk))
                return true;
        return false;
    });

    @FunctionalInterface
    public interface JoinWorld {
        boolean onJoinWorld(Entity entity, Level world, boolean loadedFromDisk);
    }

}
