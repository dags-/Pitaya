package me.dags.pitaya.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SchemHelper {

    private static final DataQuery TILE_X = DataQuery.of("TileX");
    private static final DataQuery TILE_Y = DataQuery.of("TileY");
    private static final DataQuery TILE_Z = DataQuery.of("TileZ");
    private static final DataQuery ENTITIES = DataQuery.of("Entities");

    public static Schematic createGlobal(Location<World> origin, Vector3i pos1, Vector3i pos2) {
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(pos1, pos2, origin.getBlockPosition());

        Schematic schematic = Schematic.builder()
                .volume(volume)
                .blockPalette(PaletteTypes.GLOBAL_BLOCKS.create())
                .biomePalette(PaletteTypes.GLOBAL_BIOMES.create())
                .build();

        return fix(schematic, origin.getBlockPosition());
    }

    public static Schematic createLocal(Location<World> origin, Vector3i pos1, Vector3i pos2) {
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(pos1, pos2, origin.getBlockPosition());

        Schematic schematic = Schematic.builder()
                .volume(volume)
                .blockPalette(PaletteTypes.LOCAL_BLOCKS.create())
                .biomePalette(PaletteTypes.LOCAL_BIOMES.create())
                .build();

        return fix(schematic, origin.getBlockPosition());
    }

    public static Schematic fix(Schematic schematic, Vector3i origin) {
        for (EntityArchetype entity : schematic.getEntityArchetypes()) {
            Class<?> type = entity.getType().getEntityClass();
            if (Hanging.class.isAssignableFrom(type)) {
                return fixEntities(schematic, origin);
            }
        }
        return schematic;
    }

    private static Schematic fixEntities(Schematic schematic, Vector3i origin) {
        DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);
        List<DataView> entities = container.getViewList(SchemHelper.ENTITIES).orElse(Collections.emptyList());

        if (entities.isEmpty()) {
            return schematic;
        }

        // can't mutate the 'entities' list
        List<DataView> fixed = new LinkedList<>();
        for (DataView entityData : entities) {
            fixed.add(fixEntity(entityData, origin));
        }

        container.set(SchemHelper.ENTITIES, fixed);

        return DataTranslators.SCHEMATIC.translate(container);
    }

    private static DataView fixEntity(DataView view, Vector3i origin) {
        Optional<Vector3i> blockPos = Translators.vec3iFromKeys(view, TILE_X, TILE_Y, TILE_Z);
        if (!blockPos.isPresent()) {
            // entity doesn't support a tile position
            return view;
        }

        Optional<Vector3d> pos = Translators.vec3dFromArray(view, Queries.POSITION);
        if (!pos.isPresent()) {
            // shouldn't happen as all entities have a Pos
            return view;
        }

        // get the fractional block position of the entity (0.0 - 1.0 of a block)
        double dx = pos.get().getX() - pos.get().getFloorX();
        double dy = pos.get().getY() - pos.get().getFloorY();
        double dz = pos.get().getY() - pos.get().getFloorZ();

        // offset is the block position of the entity relative to the origin, plus the above deltas
        Vector3d offset = blockPos.get().sub(origin).toDouble().add(dx, dy, dz);

        Translators.setVec3dArray(view, Queries.POSITION, offset);

        return view;
    }
}
