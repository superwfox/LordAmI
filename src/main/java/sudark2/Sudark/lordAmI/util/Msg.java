package sudark2.Sudark.lordAmI.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Msg {

    private static final LegacyComponentSerializer L = LegacyComponentSerializer.legacySection();

    public static Component cn(String legacy) {
        return L.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }
}
