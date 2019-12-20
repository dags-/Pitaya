package me.dags.pitaya.schematic.history;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class WeakHistory implements History {

    private final List<WeakReference<Entity>> entities = new LinkedList<>();
    private final List<WeakReference<TileEntity>> tiles = new LinkedList<>();

    @Override
    public void record(TileEntity tile) {
        tiles.add(new WeakReference<>(tile));
    }

    @Override
    public void record(Entity entity) {
        entities.add(new WeakReference<>(entity));
    }

    @Override
    public void tiles(Consumer<TileEntity> consumer) {
        for (WeakReference<TileEntity> ref : tiles) {
            TileEntity tile = ref.get();
            if (tile != null) {
                consumer.accept(tile);
            }
        }
    }

    @Override
    public void entities(Consumer<Entity> consumer) {
        for (WeakReference<Entity> ref : entities) {
            Entity entity = ref.get();
            if (entity != null) {
                consumer.accept(entity);
            }
        }
    }
}
