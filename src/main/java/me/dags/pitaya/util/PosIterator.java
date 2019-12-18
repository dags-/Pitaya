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

    public boolean next() {
        if (dx + 1 < width) {
            dx++;
            return true;
        }
        if (dz + 1 < length) {
            dx = 0;
            dz++;
            return true;
        }
        if (dy + 1 < height) {
            dx = 0;
            dz = 0;
            dy++;
            return true;
        }
        return false;
    }

    public int index() {
        int index = getDeltaY() * width * length;
        index += getDeltaZ() * width;
        return index + getDeltaX();
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public int getDeltaZ() {
        return dz;
    }

    public int getX() {
        return origin.getX() + dx;
    }

    public int getY() {
        return origin.getY() + dy;
    }

    public int getZ() {
        return origin.getZ() + dz;
    }

    public Vector3i get3i() {
        return new Vector3i(getX(), getY(), getZ());
    }

    public Vector2i get2i() {
        return new Vector2i(getX(), getZ());
    }

    public <T extends Extent> Location<T> get(T extent) {
        return new Location<>(extent, get3i());
    }

    /**
     * Create a PosIterator that iterates over the area between (x1,z1) & (x2,z2) in the X & Z axes (in that order)
     */
    public static PosIterator create(int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxZ = Math.max(z1, z2);
        int width = maxX - minX;
        int length = maxZ - minZ;
        return new PosIterator(new Vector3i(minX, 0, minZ), width, 0, length);
    }

    /**
     * Create a PosIterator that iterates over the area between pos1 & pos2 in the X & Z axes (in that order)
     */
    public static PosIterator create(Vector2i pos1, Vector2i pos2) {
        return create(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
    }

    /**
     * Create a PosIterator that iterates over the volume between (x1,y1,z1) & (x2,y2,z2) in the X, Z,
     * & Y axes (in that order)
     */
    public static PosIterator create(int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.min(y1, y2);
        int maxZ = Math.max(z1, z2);
        int width = maxX - minX;
        int height = maxY - minY;
        int length = maxZ - minZ;
        return new PosIterator(new Vector3i(minX, minY, minZ), width, height, length);
    }

    /**
     * Create a PosIterator that iterates over the volume between pos1 & pos2 in the X, Z, & Y axes (in that order)
     */
    public static PosIterator create(Vector3i pos1, Vector3i pos2) {
        return create(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    /**
     * Create a PosIterator that iterates over an area around point (x, z) with the given radius,
     * in the X & Z axes (in that order)
     */
    public static PosIterator radius(int x, int z, int radius) {
        int width = radius + radius + 1;
        int length = radius + radius + 1;
        Vector3i origin = new Vector3i(x - radius, 0, z - radius);
        return new PosIterator(origin, width, 0, length);
    }

    /**
     * Create a PosIterator that iterates over an area around the center point with the given radius,
     * in the X & Z axes (in that order)
     */
    public static PosIterator radius(Vector2i center, int radius) {
        return radius(center.getX(), center.getY(), radius);
    }

    /**
     * Create a PosIterator that iterates over a volume around the point (x, y, z) with the given radius,
     * in the X, Z & Y axes (in that order)
     */
    public static PosIterator radius(int x, int y, int z, int radius) {
        int width = radius + radius + 1;
        int height = radius + radius + 1;
        int length = radius + radius + 1;
        Vector3i origin = new Vector3i(x - radius, y - radius, z - radius);
        return new PosIterator(origin, width, height, length);
    }

    /**
     * Create a PosIterator that iterates over a volume around the center point with the given radius,
     * in the X, Z & Y axes (in that order)
     */
    public static PosIterator radius(Vector3i center, int radius) {
        return radius(center.getX(), center.getY(), center.getZ(), radius);
    }
}
