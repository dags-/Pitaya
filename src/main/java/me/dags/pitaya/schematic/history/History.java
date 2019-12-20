package me.dags.pitaya.schematic.history;

import me.dags.pitaya.util.optional.OptionalValue;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;

import java.util.function.Consumer;

public interface History extends AutoCloseable, OptionalValue {

    History NONE = new NoHistory();

    void record(Entity entity);

    void record(TileEntity tile);

    void entities(Consumer<Entity> consumer);

    void tiles(Consumer<TileEntity> consumer);

    @Override
    default boolean isPresent() {
        return this != NONE;
    }

    @Override
    default void close() {
        dispose();
    }

    default void dispose() {
        HistoryManager.pop(this);
    }
}
