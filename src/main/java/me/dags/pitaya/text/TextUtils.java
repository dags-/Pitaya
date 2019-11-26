package me.dags.pitaya.text;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.ScoreText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.translation.Translation;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class TextUtils {

    /**
     * Recursively attempts to merge sibling & child Text components that share same formatting and/or TextActions
     * into a single Text object
     *
     * @param text the Text to merge
     *
     * @return the merged Text
     */
    public static Text merge(Text text) {
        if (text.isEmpty()) {
            return text;
        }
        return merge(text, false);
    }

    /**
     * Recursively attempts to reduce the number of nested Text components
     *
     * @param text the Text to minimize
     *
     * @return the minimized Text
     */
    public static Text minimize(Text text) {
        if (text.isEmpty()) {
            return text;
        }
        return minimize(text, null).build();
    }

    /**
     * Attempts to optimize the provided Text by:
     * - Recursively merging sibling & child Text components that share the same formatting and/or actions
     * - Recursively minimizing the number of nested Text components
     *
     * @param text the Text to process
     *
     * @return The optimized Text
     */
    public static Text optimize(Text text) {
        if (text.isEmpty()) {
            return text;
        }
        return merge(text, true);
    }

    private static void collectParts(Text text, Text parent, List<Text> parts, boolean minimize) {
        if (text.isEmpty()) {
            return;
        }

        Text.Builder single = text.toBuilder().removeAll();
        inherit(single, parent);

        // the hoverText action is currently the only one that uses Texts
        text.getHoverAction().filter(HoverAction.ShowText.class::isInstance).ifPresent(hover -> {
            Text hoverText = (Text) hover.getResult();
            Text merged = merge(hoverText, minimize);
            single.onHover(TextActions.showText(merged));
        });

        // TranslatableTexts may use Texts in their 'arguments'
        if (single instanceof TranslatableText.Builder) {
            TranslatableText.Builder builder = (TranslatableText.Builder) single;
            Translation translation = builder.getTranslation();
            Object[] arguments = builder.getArguments().stream()
                    .map(o -> o instanceof Text ? merge((Text) o, minimize) : o)
                    .toArray();
            builder.translation(translation, arguments);
        }

        Text flat = single.build();
        parts.add(flat);
        for (Text child : text.getChildren()) {
            collectParts(child, flat, parts, minimize);
        }
    }

    private static void copyProperties(Text src, Text.Builder dest) {
        dest.format(src.getFormat());
        src.getHoverAction().ifPresent(dest::onHover);
        src.getClickAction().ifPresent(dest::onClick);
        src.getShiftClickAction().ifPresent(dest::onShiftClick);
    }

    private static List<Text> flatten(Text text, boolean minimize) {
        List<Text> parts = new LinkedList<>();
        collectParts(text, text, parts, minimize);
        return parts;
    }

    private static void inherit(Text.Builder child, Text parent) {
        if (child.getFormat().isEmpty()) {
            child.format(parent.getFormat());
        }
        if (!child.getHoverAction().isPresent()) {
            parent.getHoverAction().ifPresent(child::onHover);
        }
        if (!child.getClickAction().isPresent()) {
            parent.getClickAction().ifPresent(child::onClick);
        }
        if (!child.getShiftClickAction().isPresent()) {
            parent.getShiftClickAction().ifPresent(child::onShiftClick);
        }
    }

    private static boolean isEmpty(Text text) {
        if (text.isEmpty()) {
            return true;
        }
        if (text instanceof LiteralText) {
            return ((LiteralText) text).getContent().isEmpty();
        }
        if (text instanceof ScoreText) {
            return ((ScoreText) text).getOverride().map(String::isEmpty).orElse(false);
        }
        if (text instanceof TranslatableText) {
            return ((TranslatableText) text).getTranslation().get().isEmpty();
        }
        return false;
    }

    private static Text join(Iterable<Text> texts) {
        Text.Builder builder = null;
        for (Text text : texts) {
            if (builder == null) {
                builder = text.toBuilder();
            } else {
                builder.append(text);
            }
        }
        if (builder == null) {
            return Text.EMPTY;
        }
        return builder.build();
    }

    private static Text merge(Text text, boolean minimize) {
        List<Text> parts = flatten(text, minimize);
        List<Text> list = new LinkedList<>();
        Text compressed = Text.EMPTY;
        for (Text part : parts) {
            if (compressed.isEmpty()) {
                compressed = part;
                continue;
            }

            Text merged = merge(compressed, part);
            if (merged.isEmpty()) {
                list.add(compressed);
                compressed = part;
            } else {
                compressed = merged;
            }
        }
        list.add(compressed);
        if (minimize) {
            return minimize(join(list));
        }
        return TextUtils.join(list);
    }

    private static Text merge(Text a, Text b) {
        if (!a.getFormat().equals(b.getFormat())) {
            return Text.EMPTY;
        }
        if (!a.getHoverAction().equals(b.getHoverAction())) {
            return Text.EMPTY;
        }
        if (!a.getClickAction().equals(b.getClickAction())) {
            return Text.EMPTY;
        }
        if (!a.getShiftClickAction().equals(b.getShiftClickAction())) {
            return Text.EMPTY;
        }
        if (a instanceof LiteralText && b instanceof LiteralText) {
            LiteralText literalA = (LiteralText) a;
            LiteralText literalB = (LiteralText) b;
            Text.Builder builder = Text.builder(literalA.getContent() + literalB.getContent());
            copyProperties(a, builder);
            return builder.build();
        }
        return Text.EMPTY;
    }

    private static Text.Builder minimize(Text text, @Nullable Text.Builder parent) {
        Text.Builder builder = parent;
        if (!isEmpty(text)) {
            builder = text.toBuilder().removeAll();
        }
        for (Text child : text.getChildren()) {
            builder = minimize(child, builder);
        }
        if (builder != null) {
            builder.format(text.getFormat());
            text.getHoverAction().ifPresent(builder::onHover);
            text.getClickAction().ifPresent(builder::onClick);
            text.getShiftClickAction().ifPresent(builder::onShiftClick);
        }
        if (builder != null && parent != null && builder != parent) {
            builder.applyTo(parent);
        }
        return parent == null ? builder : parent;
    }
}
