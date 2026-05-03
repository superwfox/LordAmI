package sudark2.Sudark.lordAmI.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    public static NamespacedKey OWNER;
    public static NamespacedKey PET_ID;

    public static void init(Plugin plugin) {
        OWNER = new NamespacedKey(plugin, "owner");
        PET_ID = new NamespacedKey(plugin, "pet_id");
    }
}
