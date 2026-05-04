package sudark2.Sudark.lordAmI.pet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import sudark2.Sudark.lordAmI.pearl.Effects;
import sudark2.Sudark.lordAmI.util.Identity;

public final class OwnerCombatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();

        Player attacker = resolvePlayer(damager);
        if (attacker != null && victim instanceof LivingEntity le && !(le instanceof Player)) {
            engage(attacker, le);
            return;
        }
        if (victim instanceof Player owner) {
            LivingEntity foe = resolveLiving(damager);
            if (foe == null || foe instanceof Player) return;
            engage(owner, foe);
        }
    }

    private void engage(Player owner, LivingEntity target) {
        if (PetManager.isPet(target)) return;
        String qq = Identity.of(owner);
        if (qq == null) return;
        boolean any = false;
        for (Pet pet : PetManager.byOwner(qq)) {
            if (pet.state != PetState.FOLLOW) continue;
            if (pet.entity == null || !pet.entity.isValid()) continue;
            if (pet.entity == target) continue;
            pet.state = PetState.ATTACKING;
            pet.attackTargetUuid = target.getUniqueId();
            if (pet.entity instanceof Mob m) m.setTarget(target);
            any = true;
        }
        if (any) Effects.attackBurst(target, owner);
    }

    private static Player resolvePlayer(Entity e) {
        if (e instanceof Player p) return p;
        if (e instanceof Projectile pr && pr.getShooter() instanceof Player p) return p;
        return null;
    }

    private static LivingEntity resolveLiving(Entity e) {
        if (e instanceof LivingEntity le) return le;
        if (e instanceof Projectile pr && pr.getShooter() instanceof LivingEntity le) return le;
        return null;
    }
}
