package sudark2.Sudark.lordAmI.pearl;

import sudark2.Sudark.lordAmI.pet.Pet;

import java.util.HashMap;
import java.util.Map;

public final class SelectionState {

    private static final long TTL_MS = 3 * 60 * 1000L;

    private static final Map<String, Entry> SEL = new HashMap<>();

    public static void select(String owner, Pet pet) {
        SEL.put(owner, new Entry(pet, System.currentTimeMillis() + TTL_MS));
    }

    public static Pet get(String owner) {
        Entry e = SEL.get(owner);
        if (e == null) return null;
        if (System.currentTimeMillis() > e.expireAt) {
            SEL.remove(owner);
            return null;
        }
        if (e.pet.entity == null || !e.pet.entity.isValid() || e.pet.entity.isDead()) {
            SEL.remove(owner);
            return null;
        }
        return e.pet;
    }

    public static void clear(String owner) {
        SEL.remove(owner);
    }

    private record Entry(Pet pet, long expireAt) {}
}
