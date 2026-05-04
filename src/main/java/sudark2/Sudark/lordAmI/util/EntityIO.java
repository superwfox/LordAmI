package sudark2.Sudark.lordAmI.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Steerable;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;

public final class EntityIO {

    private static final LegacyComponentSerializer L = LegacyComponentSerializer.legacySection();

    public static void capture(LivingEntity e, PetSnapshot snap) {
        snap.type = e.getType();
        Component name = e.customName();
        snap.displayName = name == null ? null : L.serialize(name);
        snap.maxHealth = readMaxHealth(e);
        if (e instanceof Ageable a) snap.age = a.getAge();
        if (e instanceof Steerable s) snap.saddled = s.hasSaddle();
        if (e instanceof AbstractHorse h) {
            snap.horseSaddle = ser(h.getInventory().getSaddle());
            if (h.getInventory() instanceof HorseInventory hi) snap.horseArmor = ser(hi.getArmor());
        }

        EntityEquipment eq = e.getEquipment();
        if (eq != null) {
            snap.head = ser(eq.getItem(EquipmentSlot.HEAD));
            snap.chest = ser(eq.getItem(EquipmentSlot.CHEST));
            snap.legs = ser(eq.getItem(EquipmentSlot.LEGS));
            snap.feet = ser(eq.getItem(EquipmentSlot.FEET));
            snap.mainHand = ser(eq.getItem(EquipmentSlot.HAND));
            snap.offHand = ser(eq.getItem(EquipmentSlot.OFF_HAND));
        }
    }

    public static void apply(LivingEntity e, PetSnapshot snap) {
        if (snap.displayName != null && !snap.displayName.isBlank()) {
            e.customName(L.deserialize(snap.displayName));
            e.setCustomNameVisible(true);
        }

        AttributeInstance maxAttr = getMaxHealthAttribute(e);
        double max = snap.maxHealth > 0 ? snap.maxHealth : (maxAttr != null ? maxAttr.getValue() : 20.0);
        if (maxAttr != null) maxAttr.setBaseValue(max);
        try {
            e.setHealth(max);
        } catch (IllegalArgumentException ignored) {
        }

        if (e instanceof Ageable a && snap.age != 0) a.setAge(snap.age);
        if (e instanceof Steerable s && snap.saddled) s.setSaddle(true);
        if (e instanceof Tameable t) t.setTamed(true);
        if (e instanceof AbstractHorse h) {
            h.getInventory().setSaddle(des(snap.horseSaddle));
            if (h.getInventory() instanceof HorseInventory hi) hi.setArmor(des(snap.horseArmor));
        }
        if (e instanceof Mob mob) mob.setCanPickupItems(true);

        EntityEquipment eq = e.getEquipment();
        if (eq != null) {
            eq.setItem(EquipmentSlot.HEAD, des(snap.head), true);
            eq.setItem(EquipmentSlot.CHEST, des(snap.chest), true);
            eq.setItem(EquipmentSlot.LEGS, des(snap.legs), true);
            eq.setItem(EquipmentSlot.FEET, des(snap.feet), true);
            eq.setItem(EquipmentSlot.HAND, des(snap.mainHand), true);
            eq.setItem(EquipmentSlot.OFF_HAND, des(snap.offHand), true);
            eq.setHelmetDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setLeggingsDropChance(0f);
            eq.setBootsDropChance(0f);
            eq.setItemInMainHandDropChance(0f);
            eq.setItemInOffHandDropChance(0f);
        }
    }

    public static double readMaxHealth(LivingEntity e) {
        AttributeInstance ai = getMaxHealthAttribute(e);
        return ai != null ? ai.getValue() : 20.0;
    }

    private static AttributeInstance getMaxHealthAttribute(LivingEntity e) {
        try {
            return e.getAttribute(Attribute.MAX_HEALTH);
        } catch (NoSuchFieldError | NoSuchMethodError ignored) {
            return null;
        }
    }

    private static byte[] ser(ItemStack it) {
        if (it == null || it.getType().isAir()) return null;
        return it.serializeAsBytes();
    }

    private static ItemStack des(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return ItemStack.deserializeBytes(bytes);
    }
}
