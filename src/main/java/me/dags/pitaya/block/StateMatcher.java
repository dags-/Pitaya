package me.dags.pitaya.block;

import com.google.common.collect.ImmutableMap;
import me.dags.pitaya.util.OptionalValue;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateMatcher implements OptionalValue, Predicate<BlockState> {

    private static final StateMatcher ANY = new StateMatcher(BlockAny.TYPE, Collections.emptyMap(), "*");
    private static final StateMatcher EMPTY = new StateMatcher(BlockTypes.AIR, Collections.emptyMap(), "empty");

    private static final Object ANY_VALUE = new Object() {
        @Override
        public String toString() {
            return "*";
        }
    };

    private final String string;
    private final BlockType type;
    private final Map<String, Object> properties;

    private StateMatcher(BlockType type, Map<String, Object> properties, String string) {
        this.type = type;
        this.string = string;
        this.properties = properties;
    }

    public BlockType getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean matches(BlockState state) {
        if (!isPresent()) {
            return false;
        }

        if (state.getType() != getType() && getType() != BlockAny.TYPE) {
            return false;
        }

        Map<BlockTrait<?>, ?> traits = state.getTraitMap();

        outer:
        for (Map.Entry<String, Object> entry : getProperties().entrySet()) {
            for (Map.Entry<BlockTrait<?>, ?> trait : traits.entrySet()) {
                if (trait.getKey().getName().equals(entry.getKey())) {
                    if (entry.getValue() == StateMatcher.ANY_VALUE) {
                        continue outer;
                    }

                    if (trait.getValue().toString().equals(entry.getValue().toString())) {
                        continue outer;
                    }
                }
            }
            return false;
        }

        return true;
    }

    public BlockState find(BlockType fallback) {
        return find(fallback.getDefaultState());
    }

    public BlockState find(BlockState fallback) {
        return stream().findFirst().orElse(fallback);
    }

    public Optional<BlockState> find() {
        return stream().findFirst();
    }

    public Stream<BlockState> stream() {
        if (getType() == BlockAny.TYPE) {
            return Sponge.getRegistry().getAllOf(BlockState.class).stream().filter(this);
        }
        return getType().getAllBlockStates().stream().filter(this);
    }

    public List<BlockState> toList() {
        return stream().collect(Collectors.toList());
    }

    public Set<BlockState> toSet() {
        return stream().collect(Collectors.toSet());
    }

    public Map<String, BlockState> toMap() {
        return stream().collect(Collectors.toMap(BlockState::getId, Function.identity()));
    }

    public Map<BlockType, List<BlockState>> toGroups() {
        return stream().collect(Collectors.groupingBy(BlockState::getType));
    }

    public void forEach(Consumer<BlockState> consumer) {
        stream().forEach(consumer);
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean test(BlockState state) {
        return matches(state);
    }

    @Override
    public String toString() {
        return string;
    }

    public static StateMatcher parse(Object in) {
        if (in == null) {
            return StateMatcher.EMPTY;
        }
        return parse(in.toString());
    }

    public static StateMatcher parse(String in) {
        if (in == null) {
            return StateMatcher.EMPTY;
        }

        int propertiesStart = in.indexOf('[');
        int typeEnd = propertiesStart < 0 ? in.length() : propertiesStart;

        String block = in.substring(0, typeEnd);
        Optional<BlockType> typeOptional;

        if (block.equals("*")) {
            typeOptional = Optional.of(BlockAny.TYPE);
        } else {
            typeOptional = Sponge.getRegistry().getType(BlockType.class, block);
        }

        if (!typeOptional.isPresent()) {
            return StateMatcher.EMPTY;
        }

        BlockType type = typeOptional.get();
        if (propertiesStart < 0) {
            if (type == BlockAny.TYPE) {
                return StateMatcher.ANY;
            }
            return new StateMatcher(type, Collections.emptyMap(), in);
        }

        ImmutableMap.Builder<String, Object> properties = ImmutableMap.builder();

        for (int i = propertiesStart + 1; i < in.length(); i++) {
            int keyStart = i;
            int keyEnd = in.indexOf('=', keyStart);

            if (keyEnd < 0) {
                break;
            }

            int valStart = keyEnd + 1;
            int valEnd = in.indexOf(',', valStart);
            if (valEnd < 0) {
                valEnd = in.indexOf(']');
                if (valEnd < 0) {
                    break;
                }
            }

            String key = in.substring(keyStart, keyEnd);
            String val = in.substring(valStart, valEnd);
            Object value = val.equals("*") ? StateMatcher.ANY_VALUE : val;

            properties.put(key, value);

            i = valEnd;
        }

        return new StateMatcher(type, properties.build(), in);
    }
}
