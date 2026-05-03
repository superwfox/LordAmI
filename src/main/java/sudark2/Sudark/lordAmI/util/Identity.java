package sudark2.Sudark.lordAmI.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class Identity {

    public static final NamespacedKey QQ = new NamespacedKey("sudark", "qq");

    public static String of(Player p) {
        return p.getPersistentDataContainer().get(QQ, PersistentDataType.STRING);
    }
}
