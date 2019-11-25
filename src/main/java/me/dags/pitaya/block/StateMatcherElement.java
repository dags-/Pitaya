package me.dags.pitaya.block;

import me.dags.commandbus.command.CommandException;
import me.dags.commandbus.command.Context;
import me.dags.commandbus.command.Input;
import me.dags.commandbus.element.ValueElement;
import me.dags.commandbus.element.function.Filter;
import me.dags.commandbus.element.function.Options;
import me.dags.commandbus.element.function.ValueParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StateMatcherElement extends ValueElement {

    public StateMatcherElement(String key, int priority, Options options, Filter filter, ValueParser<?> parser) {
        super(key, priority, options, filter, parser);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        String text = input.next();
        if (text.contains("[") && !text.contains("]")) {
            throw new CommandException("Missing closing bracket: " + text);
        }

        StateMatcher matcher = StateMatcher.parse(text);
        if (matcher.isAbsent()) {
            throw new CommandException("Invalid state matcher: " + matcher);
        }

        context.add(getKey(), matcher);
    }

    @Override
    public void suggest(Input input, Context context, List<String> list) {
        if (!input.hasNext()) {
            return;
        }

        try {
            String text = input.last().next();
            String name = getName(text);
            Optional<BlockType> type = Sponge.getRegistry().getType(BlockType.class, name);

            if (type.isPresent()) {
                suggestProperty(type.get(), text, list);
                return;
            }

            suggestType(name, list);
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    private String getName(String input) {
        int index = input.indexOf('[');
        int split = index > 0 ? index : input.length();
        return input.substring(0, split);
    }

    private void suggestProperty(BlockType type, String input, List<String> suggestions) {
        if (input.endsWith("]")) {
            return;
        }

        int propsStart = input.lastIndexOf('[');
        if (propsStart == -1) {
            if (!type.getTraits().isEmpty()) {
                suggestions.add(input + "[");
            }
            return;
        }

        int keyStart = Math.max(propsStart, input.lastIndexOf(',')) + 1;
        if (keyStart == 0) {
            return;
        }

        int keyEnd = input.indexOf('=', keyStart);
        if (keyEnd == -1) {
            keyEnd = input.length();
        }

        String key = input.substring(keyStart, keyEnd);
        Optional<BlockTrait<?>> trait = type.getTrait(key);
        if (!trait.isPresent()) {
            suggestKey(type, input, key, keyStart, suggestions);
            return;
        }

        int valStart = keyEnd + 1;
        if (valStart < input.length()) {
            String value = input.substring(valStart);
            suggestValue(trait.get(), input, value, valStart, suggestions);
        }
    }

    private void suggestType(String input, List<String> suggestions) {
        Sponge.getRegistry().getAllOf(BlockType.class).stream()
                .filter(state -> Filter.CONTAINS.test(state.getId(), input))
                .sorted(Comparator.comparing(state -> state.getId().length()))
                .map(state -> state.getTraits().isEmpty() ? state.getId() : state.getId() + "[")
                .forEach(suggestions::add);
    }

    private void suggestKey(BlockType type, String input, String key, int start, List<String> list) {
        type.getTraits().stream()
                .map(BlockTrait::getName)
                .filter(name -> Filter.STARTS_WITH.test(name, key))
                .filter(name -> !Filter.CONTAINS.test(name + "=", input))
                .map(name -> input.substring(0, start) + name + "=")
                .forEach(list::add);
    }

    private void suggestValue(BlockTrait<?> trait, String input, String value, int start, List<String> list) {
        trait.getPossibleValues().stream()
                .map(Object::toString)
                .filter(name -> Filter.STARTS_WITH.test(name, value))
                .map(name -> input.substring(0, start) + name)
                .forEach(list::add);
    }
}
