package io.github.fabricators_of_create.porting_lib.features.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;

/**
 * An entity which is part of a parent entity, like the Ender Dragon.
 * <p>
 * These entities are not saved and are not synced. Their parent is responsible for managing them.
 * </p>
 *
 * @param <T> the type of the parent entity
 */
public abstract class PartEntity<T extends Entity> extends Entity {
    private final T parent;

    public PartEntity(T parent) {
        super(parent.getType(), parent.level());
        this.parent = parent;
    }

    public T getParent() {
        return parent;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }
}
