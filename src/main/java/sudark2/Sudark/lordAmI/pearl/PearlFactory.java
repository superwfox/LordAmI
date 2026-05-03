package sudark2.Sudark.lordAmI.pearl;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;
import sudark2.Sudark.lordAmI.util.EnchantNames;
import sudark2.Sudark.lordAmI.util.Keys;
import sudark2.Sudark.lordAmI.util.Msg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class PearlFactory {

    public static ItemStack create(PetSnapshot snap) {
        ItemStack pearl = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = pearl.getItemMeta();

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.displayName(Msg.cn("§e§l收复之眼"));

        List<Component> lore = new ArrayList<>();
        String name = snap.displayName != null && !snap.displayName.isBlank()
                ? snap.displayName
                : typeNameOf(snap);
        lore.add(Msg.cn("§6生物: §f" + name));
        lore.add(Msg.cn(slotLine("头盔", snap.head)));
        lore.add(Msg.cn(slotLine("胸甲", snap.chest)));
        lore.add(Msg.cn(slotLine("护腿", snap.legs)));
        lore.add(Msg.cn(slotLine("靴子", snap.feet)));
        lore.add(Msg.cn("§7ID: " + snap.petId.substring(0, 8)));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(Keys.PET_ID, PersistentDataType.STRING, snap.petId);
        meta.getPersistentDataContainer().set(Keys.OWNER, PersistentDataType.STRING, snap.owner);

        pearl.setItemMeta(meta);
        return pearl;
    }

    private static String typeNameOf(PetSnapshot snap) {
        return snap.type == null ? "未知" : snap.type.name();
    }

    private static String slotLine(String label, byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "§7" + label + ": §6无";
        ItemStack it = ItemStack.deserializeBytes(bytes);
        Map<Enchantment, Integer> ench = it.getEnchantments();
        if (ench.isEmpty()) return "§7" + label + ": §e有";

        List<Map.Entry<Enchantment, Integer>> sorted = new ArrayList<>(ench.entrySet());
        sorted.sort(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(Map.Entry::getValue).reversed());

        StringBuilder body = new StringBuilder();
        int show = Math.min(3, sorted.size());
        for (int i = 0; i < show; i++) {
            if (i > 0) body.append(",");
            Map.Entry<Enchantment, Integer> en = sorted.get(i);
            body.append(EnchantNames.of(en.getKey())).append(en.getValue());
        }
        if (sorted.size() > 3) body.append("...");
        return "§7" + label + ": §e有§7(" + body + "§7)";
    }

    public static ItemStack createPlain() {
        return new ItemStack(Material.ENDER_EYE);
    }

    public static boolean hasPlain(Player p) {
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null) continue;
            if (it.getType() != Material.ENDER_EYE) continue;
            if (isStored(it)) continue;
            return true;
        }
        return false;
    }

    public static boolean consumePlain(Player p) {
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null) continue;
            if (it.getType() != Material.ENDER_EYE) continue;
            if (isStored(it)) continue;
            it.setAmount(it.getAmount() - 1);
            return true;
        }
        return false;
    }

    public static boolean isStored(ItemStack item) {
        if (item == null || item.getType() != Material.ENDER_EYE) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.PET_ID, PersistentDataType.STRING);
    }

    public static boolean isAnyEye(ItemStack item) {
        return item != null && item.getType() == Material.ENDER_EYE;
    }

    public static String readPetId(ItemStack item) {
        if (!isStored(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(Keys.PET_ID, PersistentDataType.STRING);
    }
}
