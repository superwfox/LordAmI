package sudark2.Sudark.lordAmI.pet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import sudark2.Sudark.lordAmI.pearl.Effects;
import sudark2.Sudark.lordAmI.util.Identity;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class PetTicker extends BukkitRunnable {

    private final Plugin plugin;
    private int pulseCounter = 0;

    private PetTicker(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void start(Plugin plugin) {
        new PetTicker(plugin).runTaskTimer(plugin, 0L, 5L);
    }

    @Override
    public void run() {
        pulseCounter++;
        boolean emitAttackPulse = pulseCounter % 4 == 0;

        Iterator<Map.Entry<UUID, Pet>> it = PetManager.all().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Pet> en = it.next();
            Pet pet = en.getValue();
            LivingEntity le = pet.entity;
            if (le == null || !le.isValid() || le.isDead()) {
                it.remove();
                continue;
            }
            if (!(le instanceof Mob mob)) continue;

            switch (pet.state) {
                case FOLLOW -> tickFollow(mob, pet);
                case PATHING -> tickPathing(mob, pet);
                case ATTACKING -> tickAttacking(mob, pet, emitAttackPulse);
            }
        }
    }

    private void tickFollow(Mob mob, Pet pet) {
        Player owner = findOwner(pet);
        if (owner == null || owner.getWorld() != mob.getWorld()) {
            mob.getPathfinder().stopPathfinding();
            mob.setTarget(null);
            return;
        }
        double dx = owner.getLocation().getX() - mob.getLocation().getX();
        double dz = owner.getLocation().getZ() - mob.getLocation().getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 64) {
            mob.getPathfinder().stopPathfinding();
            mob.setTarget(null);
            return;
        }
        if (dist >= 8) {
            Location dir = owner.getLocation().getDirection().clone().setY(0).normalize().toLocation(owner.getWorld());
            Location dest = owner.getLocation().clone().subtract(dir.getX(), 0, dir.getZ());
            mob.getPathfinder().moveTo(dest, 1.2);
        }
    }

    private void tickPathing(Mob mob, Pet pet) {
        if (pet.anchor == null) {
            pet.state = PetState.FOLLOW;
            return;
        }
        if (pet.anchor.getWorld() != mob.getWorld()) return;
        double dx = pet.anchor.getX() - mob.getLocation().getX();
        double dz = pet.anchor.getZ() - mob.getLocation().getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d > 5) {
            mob.getPathfinder().moveTo(pet.anchor, 1.2);
            pet.pathingArrived = false;
        } else if (!pet.pathingArrived && d < 1.2) {
            pet.pathingArrived = true;
            Effects.arrivedRing(plugin, pet.anchor.clone());
        }
    }

    private void tickAttacking(Mob mob, Pet pet, boolean emitPulse) {
        if (pet.attackTargetUuid == null) {
            pet.state = PetState.FOLLOW;
            return;
        }
        org.bukkit.entity.Entity tgt = Bukkit.getEntity(pet.attackTargetUuid);
        if (!(tgt instanceof LivingEntity le) || le.isDead() || !le.isValid()) {
            pet.state = PetState.FOLLOW;
            pet.attackTargetUuid = null;
            mob.setTarget(null);
            return;
        }
        if (mob.getTarget() != le) mob.setTarget(le);
        if (emitPulse) Effects.attackPulse(le);
    }

    private Player findOwner(Pet pet) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String qq = Identity.of(p);
            if (qq != null && qq.equals(pet.owner)) return p;
        }
        return null;
    }
}
