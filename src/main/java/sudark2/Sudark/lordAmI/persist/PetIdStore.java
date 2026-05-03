package sudark2.Sudark.lordAmI.persist;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PetIdStore {

    private static Plugin plugin;
    private static File file;
    private static final Map<String, PetSnapshot> BY_ID = new HashMap<>();
    private static final Map<UUID, String> BY_ENTITY = new HashMap<>();

    public static void init(Plugin pl) {
        plugin = pl;
        file = new File(plugin.getDataFolder(), "pets.yml");
        plugin.getDataFolder().mkdirs();
        load();
    }

    public static void load() {
        BY_ID.clear();
        BY_ENTITY.clear();
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        for (String id : y.getKeys(false)) {
            ConfigurationSection s = y.getConfigurationSection(id);
            if (s == null) continue;
            PetSnapshot snap = new PetSnapshot();
            snap.petId = id;
            snap.owner = s.getString("owner");
            snap.type = EntityType.valueOf(s.getString("type"));
            snap.displayName = s.getString("displayName");
            snap.maxHealth = s.getDouble("maxHealth");
            snap.age = s.getInt("age");
            snap.saddled = s.getBoolean("saddled");
            snap.head = decode(s.getString("eq.head"));
            snap.chest = decode(s.getString("eq.chest"));
            snap.legs = decode(s.getString("eq.legs"));
            snap.feet = decode(s.getString("eq.feet"));
            snap.mainHand = decode(s.getString("eq.mainHand"));
            snap.offHand = decode(s.getString("eq.offHand"));
            snap.state = PetSnapshot.State.valueOf(s.getString("state", "STORED"));
            String eu = s.getString("entityUuid");
            if (eu != null) snap.entityUuid = UUID.fromString(eu);
            snap.world = s.getString("world");
            BY_ID.put(id, snap);
            if (snap.entityUuid != null) BY_ENTITY.put(snap.entityUuid, id);
        }
    }

    public static void saveAll() {
        YamlConfiguration y = new YamlConfiguration();
        for (PetSnapshot snap : BY_ID.values()) {
            String id = snap.petId;
            y.set(id + ".owner", snap.owner);
            y.set(id + ".type", snap.type.name());
            y.set(id + ".displayName", snap.displayName);
            y.set(id + ".maxHealth", snap.maxHealth);
            y.set(id + ".age", snap.age);
            y.set(id + ".saddled", snap.saddled);
            y.set(id + ".eq.head", encode(snap.head));
            y.set(id + ".eq.chest", encode(snap.chest));
            y.set(id + ".eq.legs", encode(snap.legs));
            y.set(id + ".eq.feet", encode(snap.feet));
            y.set(id + ".eq.mainHand", encode(snap.mainHand));
            y.set(id + ".eq.offHand", encode(snap.offHand));
            y.set(id + ".state", snap.state.name());
            y.set(id + ".entityUuid", snap.entityUuid == null ? null : snap.entityUuid.toString());
            y.set(id + ".world", snap.world);
        }
        try {
            y.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("保存 pets.yml 失败: " + ex.getMessage());
        }
    }

    public static PetSnapshot get(String petId) {
        return BY_ID.get(petId);
    }

    public static PetSnapshot getByEntity(UUID entityUuid) {
        String id = BY_ENTITY.get(entityUuid);
        return id == null ? null : BY_ID.get(id);
    }

    public static void put(PetSnapshot snap) {
        BY_ID.put(snap.petId, snap);
        if (snap.entityUuid != null) BY_ENTITY.put(snap.entityUuid, snap.petId);
    }

    public static void onDeployed(PetSnapshot snap, UUID entityUuid, String world) {
        clearEntityIndex(snap.entityUuid);
        snap.state = PetSnapshot.State.DEPLOYED;
        snap.entityUuid = entityUuid;
        snap.world = world;
        BY_ENTITY.put(entityUuid, snap.petId);
    }

    public static void onStored(PetSnapshot snap) {
        clearEntityIndex(snap.entityUuid);
        snap.state = PetSnapshot.State.STORED;
        snap.entityUuid = null;
        snap.world = null;
    }

    public static void onDead(PetSnapshot snap) {
        clearEntityIndex(snap.entityUuid);
        snap.state = PetSnapshot.State.DEAD;
        snap.entityUuid = null;
        snap.world = null;
    }

    public static void remove(String petId) {
        PetSnapshot snap = BY_ID.remove(petId);
        if (snap != null) clearEntityIndex(snap.entityUuid);
    }

    private static void clearEntityIndex(UUID entityUuid) {
        if (entityUuid != null) BY_ENTITY.remove(entityUuid);
    }

    private static String encode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String s) {
        if (s == null || s.isEmpty()) return null;
        return Base64.getDecoder().decode(s);
    }
}
