package me.dags.pitaya.schematic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ListMultimap;
import me.dags.pitaya.schematic.history.History;
import me.dags.pitaya.schematic.history.HistoryManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.*;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchematicWrapper implements PitSchematic {

    private final Schematic schematic;

    public SchematicWrapper(Schematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public Schematic getBacking() {
        return schematic;
    }

    @Override
    public void applyBlocks(Location<World> location, BlockChangeFlag blockChangeFlag) {
        getBacking().getBlockWorker().iterate((volume, x, y, z) -> {
            BlockState state = volume.getBlock(x, y, z);
            int px = location.getBlockX() + x;
            int py = location.getBlockY() + y;
            int pz = location.getBlockZ() + z;
            location.getExtent().setBlock(px, py, pz, state, blockChangeFlag);
        });
    }

    @Override
    public void applyTiles(Location<World> location) {
        History history = HistoryManager.getHistory();
        getBacking().getTileEntityArchetypes().forEach((offset, archetype) -> {
            Location<World> target = location.add(offset);
            archetype.apply(target).ifPresent(history::record);
        });
    }

    @Override
    public void applyEntities(Location<World> location) {
        History history = HistoryManager.getHistory();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);

            schematic.getEntityArchetypes().forEach(archetype -> {
                // Record the archetype's original data because SpongeCommon mutates it
                DataView data = archetype.getEntityData();

                // Record the relative tile position data
                Optional<Integer> tileX = data.getInt(SchemUtils.TILE_X);
                Optional<Integer> tileY = data.getInt(SchemUtils.TILE_Y);
                Optional<Integer> tileZ = data.getInt(SchemUtils.TILE_Z);

                // Use the tile position as the offset if present
                if (tileX.isPresent() && tileY.isPresent() && tileZ.isPresent()) {
                    // Set the absolute tile position
                    data.set(SchemUtils.TILE_X, location.getBlockX() + tileX.get());
                    data.set(SchemUtils.TILE_Y, location.getBlockY() + tileY.get());
                    data.set(SchemUtils.TILE_Z, location.getBlockZ() + tileZ.get());

                    // 'data' is a copy so must apply it back onto the archetype
                    archetype.setRawData(data);

                    // Offset by the relative tile position
                    archetype.apply(location.add(tileX.get(), tileY.get(), tileZ.get())).ifPresent(history::record);
                } else {
                    // Apply the archetype at the offset location
                    List<Double> pos = data.getDoubleList(Queries.POSITION).orElse(SchemUtils.NO_POS);

                    // Offset by the relative entity position
                    archetype.apply(location.add(pos.get(0), pos.get(1), pos.get(2))).ifPresent(history::record);
                }

                // Restore the relative tile position data if present
                if (tileX.isPresent() && tileY.isPresent() && tileZ.isPresent()) {
                    data.set(SchemUtils.TILE_X, tileX.get());
                    data.set(SchemUtils.TILE_Y, tileY.get());
                    data.set(SchemUtils.TILE_Z, tileZ.get());
                }

                // Restore the archetype's original data
                archetype.setRawData(data);
            });
        }
    }

    @Override
    public void applyBiomes(Location<World> location) {
        if (getBacking().getBiomes().isPresent()) {
            getBacking().getBiomes().get().getBiomeWorker().iterate((volume, x, y, z) -> {
                BiomeType biome = volume.getBiome(x, y, z);
                int px = location.getBlockX() + x;
                int py = location.getBlockY() + y;
                int pz = location.getBlockZ() + z;
                location.getExtent().setBiome(px, py, pz, biome);
            });
        }
    }

    @Override
    @Deprecated
    public BlockPalette getPalette() {
        return schematic.getPalette();
    }

    @Override
    public Palette<BlockState> getBlockPalette() {
        return schematic.getBlockPalette();
    }

    @Override
    public Palette<BiomeType> getBiomePalette() {
        return schematic.getBiomePalette();
    }

    @Override
    public DataView getMetadata() {
        return schematic.getMetadata();
    }

    @Override
    public MutableBlockVolumeWorker<Schematic> getBlockWorker() {
        return schematic.getBlockWorker();
    }

    @Override
    public Optional<MutableBiomeVolume> getBiomes() {
        return schematic.getBiomes();
    }

    @Override
    public ListMultimap<Vector3d, EntityArchetype> getEntitiesByPosition() {
        return schematic.getEntitiesByPosition();
    }

    @Override
    public Collection<EntityArchetype> getEntityArchetypes() {
        return schematic.getEntityArchetypes();
    }

    @Override
    public Optional<TileEntityArchetype> getTileEntityArchetype(int x, int y, int z) {
        return schematic.getTileEntityArchetype(x, y, z);
    }

    @Override
    public Optional<TileEntityArchetype> getTileEntityArchetype(Vector3i position) {
        return schematic.getTileEntityArchetype(position);
    }

    @Override
    public Map<Vector3i, TileEntityArchetype> getTileEntityArchetypes() {
        return schematic.getTileEntityArchetypes();
    }

    @Override
    public Optional<EntityArchetype> getEntityArchetype(double x, double y, double z) {
        return schematic.getEntityArchetype(x, y, z);
    }

    @Override
    public boolean setBlock(Vector3i position, BlockState block) {
        return schematic.setBlock(position, block);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        return schematic.setBlock(x, y, z, block);
    }

    @Override
    public boolean setBlockType(Vector3i position, BlockType type) {
        return schematic.setBlockType(position, type);
    }

    @Override
    public boolean setBlockType(int x, int y, int z, BlockType type) {
        return schematic.setBlockType(x, y, z, type);
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return schematic.getBlockView(newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return schematic.getBlockView(transform);
    }

    @Override
    public MutableBlockVolume getRelativeBlockView() {
        return schematic.getRelativeBlockView();
    }

    @Override
    public Vector3i getBlockMin() {
        return schematic.getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return schematic.getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return schematic.getBlockSize();
    }

    @Override
    public boolean containsBlock(Vector3i position) {
        return schematic.containsBlock(position);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return schematic.containsBlock(x, y, z);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return schematic.getBlock(position);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return schematic.getBlock(x, y, z);
    }

    @Override
    public BlockType getBlockType(Vector3i position) {
        return schematic.getBlockType(position);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return schematic.getBlockType(x, y, z);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return schematic.getUnmodifiableBlockView();
    }

    @Override
    public MutableBlockVolume getBlockCopy() {
        return schematic.getBlockCopy();
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        return schematic.getBlockCopy(type);
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return schematic.getImmutableBlockCopy();
    }
}
