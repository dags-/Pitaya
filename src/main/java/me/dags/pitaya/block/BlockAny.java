package me.dags.pitaya.block;

import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class BlockAny implements BlockType {

    static final BlockAny TYPE = new BlockAny();

    static BlockState getBaseState(BlockType type, BlockState other) {
        return type == TYPE ? other : type.getDefaultState();
    }

    private BlockAny() {

    }

    @Override
    public String getId() {
        return "copy:any";
    }

    @Override
    public String getName() {
        return "any";
    }

    @Override
    public BlockState getDefaultState() {
        throw new UnsupportedOperationException("THIS SHOULD NOT HAPPEN!");
    }

    @Override
    public Collection<BlockState> getAllBlockStates() {
        return Collections.emptyList();
    }

    @Override
    public Optional<ItemType> getItem() {
        return Optional.empty();
    }

    @Override
    public boolean getTickRandomly() {
        return false;
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {

    }

    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return Collections.emptyList();
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        return Optional.empty();
    }

    @Override
    public BlockSoundGroup getSoundGroup() {
        return BlockTypes.AIR.getSoundGroup();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    public Translation getTranslation() {
        return BlockTypes.AIR.getTranslation();
    }
}