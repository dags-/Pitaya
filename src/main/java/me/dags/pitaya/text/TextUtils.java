package me.dags.pitaya.text;

import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

public class TextUtils {

    public static Text minimize(Text text) {
        return minimize(text, null).build();
    }

    public static Text.Builder minimize(Text text, @Nullable Text.Builder parent) {
        Text.Builder builder = parent;
        if (!text.toPlainSingle().isEmpty()) {
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
