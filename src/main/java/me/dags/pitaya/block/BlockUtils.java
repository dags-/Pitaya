package me.dags.pitaya.block;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

import java.util.stream.Stream;

public class BlockUtils {

    public static final BlockState NONE = BlockTypes.AIR.getDefaultState();

    public static BlockState state(String input) {
        StateMatcher matcher = StateMatcher.parse(input);
        if (matcher.isAbsent()) {
            return NONE;
        }
        return Sponge.getRegistry().getAllOf(BlockState.class).stream().filter(matcher::matches).findFirst().orElse(NONE);
    }

    public static Stream<BlockState> states(String input) {
        StateMatcher matcher = StateMatcher.parse(input);
        if (matcher.isAbsent()) {
            return Stream.empty();
        }
        return Sponge.getRegistry().getAllOf(BlockState.class).stream().filter(matcher::matches);
    }
}
