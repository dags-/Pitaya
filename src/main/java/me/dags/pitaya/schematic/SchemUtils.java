package me.dags.pitaya.schematic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SchemUtils {

    public static final DataQuery TILE_X = DataQuery.of("TileX");
    public static final DataQuery TILE_Y = DataQuery.of("TileY");
    public static final DataQuery TILE_Z = DataQuery.of("TileZ");
    public static final List<Double> NO_POS = ImmutableList.of(0D, 0D, 0D);

    public static Schematic wrap(Schematic schematic) {
        if (schematic instanceof SchematicWrapper) {
            return schematic;
        }
        return new SchematicWrapper(schematic);
    }

    public static Schematic translate(DataView view) {
        Schematic schematic = DataTranslators.SCHEMATIC.translate(view);
        return new SchematicWrapper(schematic);
    }

    public static DataView translate(Schematic schematic) {
        if (schematic instanceof SchematicWrapper) {
            SchematicWrapper wrapper = (SchematicWrapper) schematic;
            return translate(wrapper.getSchematic());
        }
        return DataTranslators.SCHEMATIC.translate(schematic);
    }

    public static Schematic createGlobal(Location<? extends Extent> origin, Vector3i pos1, Vector3i pos2) {
        ArchetypeVolume volume = createVolume(origin, pos1, pos2);

        Schematic schematic = Schematic.builder()
                .volume(volume)
                .blockPalette(PaletteTypes.GLOBAL_BLOCKS.create())
                .biomePalette(PaletteTypes.GLOBAL_BIOMES.create())
                .build();

        return wrap(schematic);
    }

    public static Schematic createLocal(Location<? extends Extent> origin, Vector3i pos1, Vector3i pos2) {
        ArchetypeVolume volume = createVolume(origin, pos1, pos2);

        Schematic schematic = Schematic.builder()
                .volume(volume)
                .blockPalette(PaletteTypes.LOCAL_BLOCKS.create())
                .biomePalette(PaletteTypes.LOCAL_BIOMES.create())
                .build();

        return wrap(schematic);
    }

    public static ArchetypeVolume createVolume(Location<? extends Extent> origin, Vector3i pos1, Vector3i pos2) {
        return createVolume(origin.getExtent(), origin.getBlockPosition(), pos1, pos2);
    }

    public static ArchetypeVolume createVolume(Extent extent, Vector3i origin, Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        Vector3i offset = min.sub(origin).mul(-1);
        Vector3i size = max.sub(min).add(Vector3i.ONE);

        final int ox = origin.getX();
        final int oy = origin.getY();
        final int oz = origin.getZ();

        ArchetypeVolume volume = Sponge.getRegistry().getExtentBufferFactory().createArchetypeVolume(size, offset);

        extent.getExtentView(min, max).getBlockWorker().iterate((source, x, y, z) -> {
            BlockState state = extent.getBlock(x, y, z);
            volume.setBlock(x - ox, y - oy, z - oz, state);
            source.getTileEntity(x, y, z).ifPresent(tile -> {
                Vector3i position = new Vector3i(x - ox, y - oy, z - oz);
                TileEntityArchetype archetype = createTileArchetype(tile, origin, min);
                volume.getTileEntityArchetypes().put(position, archetype);
            });
        });


        // Expand bounds to the outer bounds of the block at 'max'
        AABB bounds = new AABB(min, max.add(Vector3i.ONE));

        // Note - getIntersectingEntities is fucked -_-
        for (Entity entity : extent.getEntities(e -> !(e instanceof Player))) {
            Vector3d position = entity.getLocation().getPosition();
            Optional<AABB> entityBounds = entity.getBoundingBox();
            if (entityBounds.map(bounds::intersects).orElseGet(() -> bounds.contains(position))) {
                EntityArchetype archetype = createEntityArchetype(entity, origin);
                volume.getEntityArchetypes().add(archetype);
            }
        }

        return volume;
    }

    private static TileEntityArchetype createTileArchetype(TileEntity tile, Vector3i origin, Vector3i min) {
        TileEntityArchetype archetype = tile.createArchetype();
        DataContainer data = archetype.getTileData();
        Vector3i pos = tile.getLocation().getBlockPosition();
        int x = (pos.getX() - origin.getX()) - min.getX();
        int y = pos.getY() - min.getY();
        int z = (pos.getZ() - origin.getZ()) - min.getZ();
        data.set(Queries.POSITION, Arrays.asList(x, y, z));
        archetype.setRawData(data);
        return archetype;
    }

    private static EntityArchetype createEntityArchetype(Entity entity, Vector3i origin) {
        Vector3d pos = entity.getLocation().getPosition();
        double x = pos.getX() - origin.getX();
        double y = pos.getY() - origin.getY();
        double z = pos.getZ() - origin.getZ();
        EntityArchetype archetype = entity.createArchetype();
        DataView data = entity.createArchetype().getEntityData();
        Optional<Integer> tileX = data.getInt(TILE_X);
        Optional<Integer> tileY = data.getInt(TILE_Y);
        Optional<Integer> tileZ = data.getInt(TILE_Z);
        if (tileX.isPresent() && tileY.isPresent() && tileZ.isPresent()) {
            int tx = tileX.get() - origin.getX();
            int ty = tileY.get() - origin.getY();
            int tz = tileZ.get() - origin.getZ();
            data.set(TILE_X, tx);
            data.set(TILE_Y, ty);
            data.set(TILE_Z, tz);
        }
        data.set(Queries.POSITION, Arrays.asList(x, y, z));
        archetype.setRawData(data);
        return archetype;
    }
}
