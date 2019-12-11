package me.dags.pitaya.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import me.dags.config.Node;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.*;
import java.util.function.Function;

public class Translators {

    public static Vector3i vec3i(Node node) {
        int x = node.get("x", 0);
        int y = node.get("y", 0);
        int z = node.get("z", 0);
        return new Vector3i(x, y, z);
    }

    public static void vec3i(Node node, Vector3i vec) {
        node.set("x", vec.getX());
        node.set("y", vec.getY());
        node.set("z", vec.getZ());
    }

    public static String getString(DataView view, DataQuery path) throws InvalidDataException {
        return get(path, view::getString);
    }

    public static boolean getBool(DataView view, DataQuery path) throws InvalidDataException {
        return get(path, view::getBoolean);
    }

    public static int getInt(DataView view, DataQuery path) throws InvalidDataException {
        return get(path, view::getInt);
    }

    public static long getLong(DataView view, DataQuery path) throws InvalidDataException {
        return get(path, view::getLong);
    }

    public static <T> T get(DataQuery query, Function<DataQuery, Optional<T>> getter) {
        return getter.apply(query).orElseThrow(() -> err(query));
    }

    public static <T extends Enum<T>> T getEnum(DataView view, DataQuery path, Class<T> type) throws InvalidDataException {
        return Enum.valueOf(type, getString(view, path));
    }

    public static <T> T get(DataView view, DataQuery path, DataTranslator<T> translator) throws InvalidDataException {
        DataView child = view.getView(path).orElseThrow(() -> err(path));
        return translator.translate(child);
    }

    public static <T> List<T> getList(DataView view, DataQuery path, DataTranslator<T> translator) throws InvalidDataException {
        List<DataView> data = view.getViewList(path).orElseThrow(() -> err(path));
        List<T> list = new LinkedList<>();
        for (DataView dataView : data) {
            list.add(translator.translate(dataView));
        }
        return ImmutableList.copyOf(list);
    }

    public static <T> List<DataContainer> toList(List<T> list, DataTranslator<T> translator) {
        List<DataContainer> data = new LinkedList<>();
        for (T t : list) {
            data.add(translator.translate(t));
        }
        return data;
    }

    public static void setVec3iArray(DataView view, DataQuery path, Vector3i vec) {
        view.set(path, Arrays.asList(vec.getX(), vec.getY(), vec.getZ()));
    }

    public static void setVec3dArray(DataView view, DataQuery path, Vector3d vec) {
        view.set(path, Arrays.asList(vec.getX(), vec.getY(), vec.getZ()));
    }

    public static void setVec3i(DataView view, Vector3i vec, DataQuery x, DataQuery y, DataQuery z) {
        view.set(x, vec.getX());
        view.set(y, vec.getY());
        view.set(z, vec.getZ());
    }

    public static void setVec3d(DataView view, Vector3d vec, DataQuery x, DataQuery y, DataQuery z) {
        view.set(x, vec.getX());
        view.set(y, vec.getY());
        view.set(z, vec.getZ());
    }

    public static Optional<Vector3i> vec3iFromArray(DataView view, DataQuery query) {
        List<Integer> array = view.getIntegerList(query).orElse(Collections.emptyList());
        if (array.size() != 3) {
            return Optional.empty();
        }
        return Optional.of(new Vector3i(array.get(0), array.get(1), array.get(2)));
    }

    public static Optional<Vector3d> vec3dFromArray(DataView view, DataQuery query) {
        List<Double> array = view.getDoubleList(query).orElse(Collections.emptyList());
        if (array.size() != 3) {
            return Optional.empty();
        }
        return Optional.of(new Vector3d(array.get(0), array.get(1), array.get(2)));
    }

    public static Optional<Vector3i> vec3iFromKeys(DataView view, DataQuery qx, DataQuery qy, DataQuery qz) {
        Optional<Integer> x = view.getInt(qx);
        if (!x.isPresent()) {
            return Optional.empty();
        }
        Optional<Integer> y = view.getInt(qy);
        if (!y.isPresent()) {
            return Optional.empty();
        }
        Optional<Integer> z = view.getInt(qz);
        if (!z.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new Vector3i(x.get(), y.get(), z.get()));
    }

    public static Optional<Vector3d> vec3dFromKeys(DataView view, DataQuery qx, DataQuery qy, DataQuery qz) {
        Optional<Double> x = view.getDouble(qx);
        if (!x.isPresent()) {
            return Optional.empty();
        }
        Optional<Double> y = view.getDouble(qy);
        if (!y.isPresent()) {
            return Optional.empty();
        }
        Optional<Double> z = view.getDouble(qz);
        if (!z.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new Vector3d(x.get(), y.get(), z.get()));
    }

    public static InvalidDataException err(DataQuery query) {
        return new InvalidDataException("Missing data: " + query);
    }
}
