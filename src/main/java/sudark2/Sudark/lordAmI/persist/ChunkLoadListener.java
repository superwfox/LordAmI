package sudark2.Sudark.lordAmI.persist;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import sudark2.Sudark.lordAmI.Team.TeamManager;
import sudark2.Sudark.lordAmI.pet.PetManager;

public final class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity en : e.getChunk().getEntities()) {
            if (!(en instanceof LivingEntity le)) continue;
            if (!PetManager.isPet(le)) continue;
            if (PetManager.getFromEntity(le) != null) continue;

            String owner = PetManager.readOwner(le);
            String petId = PetManager.readPetId(le);
            if (owner == null || petId == null) continue;

            PetManager.register(le, owner, petId);
            TeamManager.addEntity(owner, le);

            PetSnapshot snap = PetIdStore.get(petId);
            if (snap != null) PetIdStore.onDeployed(snap, le.getUniqueId(), le.getWorld().getName());
        }
    }
}
