package me.dags.pitaya.util;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

public class PosIterator {

    private final int width;
    private final int height;
    private final int length;
    private final Vector3i origin;

    private int dx = -1;
    private int dy = 0;
    private int dz = 0;

    private PosIterator(Vector3i origin, int width, int height, int length) {
        this.origin = origin;
        this.width = width;
        this.length = length;
        this.height = height;
    }

    /**
     * Check if the iterator has a new position to move to
     */
    public boolean hasNext() {
        return dx + 1 < width || dz + 1 < length || dy + 1 < height;
    }

    /**
     * Iterate to the next position and return as a 2D (X, Z) point
     */
    public Vector2i next2i() {
        next();
        return new Vector2i(origin.getX() + dz, origin.getZ() + dz);
    }

    /**
     * Iterate to the next position and return as 3D (X, Y, Z) point
     */
    public Vector3i next3i() {
        next();
        return origin.add(dx, dy, dz);
    }

    /**
     * Iterate to the next position, returning as Location object using the provide World
     */
    public <E extends Extent> Location<E> next(E extent) {
        return new Location<>(extent, next3i());
    }

    private void next() {
        if (dx + 1 < width) {
            dx++;
            return;
        }
        if (dz + 1 < length) {
            dx = 0;
            dz++;
            return;
        }
        if (dy + 1 < height) {
            dx = 0;
            dz = 0;
            dy++;
        }
    }

    /**
     * Create a PosIterator that iterates over the area between pos1 & pos2 in the X & Z axes (in that order)
     */
    public static PosIterator create(Vector2i pos1, Vector2i pos2) {
        Vector2i min = pos1.min(pos2);
        Vector2i max = pos1.max(pos2);
        Vector2i size = max.sub(min);
        Vector3i origin = new Vector3i(min.getX(), 0, min.getY());
        return new PosIterator(origin, size.getX(), 0, size.getY());
    }

    /**
     * Create a PosIterator that iterates over the volume between pos1 & pos2 in the X, Z, & Y axes (in that order)
     */
    public static PosIterator create(Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        Vector3i size = max.sub(min);
        return new PosIterator(min, size.getX(), size.getY(), size.getZ());
    }

    /**
     * Create a PosIterator that iterates over an area around the center point with the given radius,
     * in the X & Z axes (in that order)
     */
    public static PosIterator radius(Vector2i center, int radius) {
        int width = radius + radius + 1;
        int length = radius + radius + 1;
        Vector3i origin = new Vector3i(center.getX() - radius, 0, center.getY() - radius);
        return new PosIterator(origin, width, 0, length);
    }

    /**
     * Create a PosIterator that iterates over a volume around the center point with the given radius,
     * in the X, Z & Y axes (in that order)
     */
    public static PosIterator radius(Vector3i center, int radius) {
        int width = radius + radius + 1;
        int length = radius + radius + 1;
        int height = radius + radius + 1;
        Vector3i origin = center.sub(radius, radius, radius);
        return new PosIterator(origin, width, height, length);
    }
}
