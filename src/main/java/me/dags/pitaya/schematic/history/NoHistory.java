package me.dags.pitaya.schematic.history;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;

import java.util.function.Consumer;

public class NoHistory implements History {

    @Override
    public void tiles(Consumer<TileEntity> consumer) {

    }

    @Override
    public void record(Entity entity) {

    }

    @Override
    public void record(TileEntity tile) {

    }

    @Override
    public void entities(Consumer<Entity> consumer) {

    }
}
