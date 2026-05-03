package sudark2.Sudark.lordAmI.pet;

import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import sudark2.Sudark.lordAmI.persist.PetIdStore;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;
import sudark2.Sudark.lordAmI.util.EntityIO;
import sudark2.Sudark.lordAmI.util.Keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PetManager {

    private static final Map<UUID, Pet> BY_ENTITY = new HashMap<>();

    public static Pet register(LivingEntity entity, String owner, String petId) {
        Pet pet = new Pet(petId, owner, entity);
        BY_ENTITY.put(entity.getUniqueId(), pet);
        return pet;
    }

    public static Pet get(UUID entityUuid) {
        return BY_ENTITY.get(entityUuid);
    }

    public static Pet getFromEntity(LivingEntity e) {
        return BY_ENTITY.get(e.getUniqueId());
    }

    public static void unregister(UUID entityUuid) {
        BY_ENTITY.remove(entityUuid);
    }

    public static List<Pet> byOwner(String owner) {
        List<Pet> out = new ArrayList<>();
        for (Pet p : BY_ENTITY.values()) if (p.owner.equals(owner)) out.add(p);
        return out;
    }

    public static boolean isPet(LivingEntity e) {
        return e.getPersistentDataContainer().has(Keys.OWNER, PersistentDataType.STRING);
    }

    public static String readOwner(LivingEntity e) {
        return e.getPersistentDataContainer().get(Keys.OWNER, PersistentDataType.STRING);
    }

    public static String readPetId(LivingEntity e) {
        return e.getPersistentDataContainer().get(Keys.PET_ID, PersistentDataType.STRING);
    }

    public static String capture(String owner, LivingEntity target) {
        String petId = UUID.randomUUID().toString();
        PetSnapshot snap = new PetSnapshot();
        snap.petId = petId;
        snap.owner = owner;
        EntityIO.capture(target, snap);
        snap.state = PetSnapshot.State.STORED;
        PetIdStore.put(snap);
        return petId;
    }

    public static Map<UUID, Pet> all() {
        return BY_ENTITY;
    }
}
