package me.dags.pitaya.util.region;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;

import java.util.*;
import java.util.function.Consumer;

public class RegionMap<T extends Positioned> {

    private final int size;
    private final Set<T> empty = Collections.emptySet();
    private final Map<Long, Set<T>> map = new HashMap<>();

    public RegionMap(int regionSize) {
        this.size = regionSize;
    }

    public void add(T t) {
        int rx = t.getPosition().getX() >> size;
        int rz = t.getPosition().getZ() >> size;
        long id = getId(rx, rz);
        map.computeIfAbsent(id, l -> new HashSet<>()).add(t);
    }

    public void add(T t, int radius) {
        int minX = (t.getPosition().getX() - radius) >> size;
        int minZ = (t.getPosition().getZ() - radius) >> size;
        int maxX = (t.getPosition().getX() + radius) >> size;
        int maxZ = (t.getPosition().getZ() + radius) >> size;
        for (int rz = minZ; rz <= maxZ; rz++) {
            for (int rx = minX; rx <= maxX; rx++) {
                long id = getId(rx, rz);
                map.computeIfAbsent(id, l -> new HashSet<>()).add(t);
            }
        }
    }

    public void visit(Vector3i position, int radius, Consumer<T> consumer) {
        visit(position.getX(), position.getZ(), radius, consumer);
    }

    public void visit(Vector2i position, int radius, Consumer<T> consumer) {
        visit(position.getX(), position.getY(), radius, consumer);
    }

    public void visit(int x, int z, int radius, Consumer<T> consumer) {
        int regionX = x >> size;
        int regionZ = z >> size;
        int regionRadius = radius >> size;
        int minX = regionX - regionRadius;
        int minZ = regionZ - regionRadius;
        int maxX = regionX + regionRadius;
        int maxZ = regionZ + regionRadius;
        Set<T> visited = new HashSet<>();
        for (int rz = minZ; rz <= maxZ; rz++) {
            for (int rx = minX; rx <= maxX; rx++) {
                long id = getId(rx, rz);
                Collection<T> set = map.getOrDefault(id, empty);
                for (T t : set) {
                    if (visited.add(t)) {
                        consumer.accept(t);
                    }
                }
            }
        }
    }

    public Collection<T> get(Vector3i position) {
        return get(position.getX(), position.getZ());
    }

    public Collection<T> get(Vector2i position) {
        return get(position.getX(), position.getY());
    }

    public Collection<T> get(int x, int z) {
        int regionX = x >> size;
        int regionZ = z >> size;
        long id = getId(regionX, regionZ);
        return map.getOrDefault(id, empty);
    }

    public static long getId(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }
}
