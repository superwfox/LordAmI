package sudark2.Sudark.lordAmI.pet;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import sudark2.Sudark.lordAmI.util.Identity;

public final class PetTargetListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        if (!PetManager.isPet(le)) return;
        Pet pet = PetManager.getFromEntity(le);
        if (pet == null) return;

        if (e.getTarget() instanceof Player p) {
            String qq = Identity.of(p);
            if (qq != null && qq.equals(pet.owner)) {
                e.setCancelled(true);
                return;
            }
        }

        if (pet.state == PetState.ATTACKING) {
            if (pet.attackTargetUuid == null || e.getTarget() == null
                    || !pet.attackTargetUuid.equals(e.getTarget().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }
}
