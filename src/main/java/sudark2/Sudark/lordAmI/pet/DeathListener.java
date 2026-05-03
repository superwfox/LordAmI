package sudark2.Sudark.lordAmI.pet;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import sudark2.Sudark.lordAmI.Team.TeamManager;
import sudark2.Sudark.lordAmI.persist.PetIdStore;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;

public final class DeathListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        LivingEntity le = e.getEntity();
        if (!PetManager.isPet(le)) return;
        String petId = PetManager.readPetId(le);
        if (petId == null) return;

        PetSnapshot snap = PetIdStore.get(petId);
        if (snap != null) PetIdStore.onDead(snap);

        PetManager.unregister(le.getUniqueId());
        TeamManager.removeEntity(le);
    }
}
