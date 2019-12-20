package me.dags.pitaya.schematic;

import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.schematic.Schematic;

public interface PitSchematic extends Schematic {

    Schematic getBacking();

    void applyBlocks(Location<World> location, BlockChangeFlag flags);

    void applyTiles(Location<World> location);

    void applyEntities(Location<World> location);

    void applyBiomes(Location<World> location);

    @Override
    default void apply(Location<World> location, BlockChangeFlag flags) {
        applyBlocks(location, flags);
        applyTiles(location);
        applyEntities(location);
        applyBiomes(location);
    }
}
