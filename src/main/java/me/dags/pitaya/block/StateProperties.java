package me.dags.pitaya.block;

import com.google.common.collect.ImmutableMap;
import me.dags.pitaya.util.OptionalValue;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class StateProperties implements OptionalValue  {

    private static StateProperties EMPTY = new StateProperties(BlockTypes.AIR, Collections.emptyMap());

    private final BlockType type;
    private final Map<String, Object> properties;

    private StateProperties(BlockType type, Map<String, Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    public BlockType getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return String.format("%s=%s", type, properties);
    }

    public static StateProperties parse(String in) {
        int propertiesStart = in.indexOf('[');
        int typeEnd = propertiesStart < 0 ? in.length() : propertiesStart;

        String block = in.substring(0, typeEnd);
        Optional<BlockType> typeOptional;

        if (block.equals("*")) {
            typeOptional = Optional.of(BlockTypes.AIR);
        } else {
            typeOptional = Sponge.getRegistry().getType(BlockType.class, block);
        }

        if (!typeOptional.isPresent()) {
            return StateProperties.EMPTY;
        }

        BlockType type = typeOptional.get();

        if (propertiesStart < 0) {
            return new StateProperties(type, Collections.emptyMap());
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

        return new StateProperties(type, properties.build());
    }
}