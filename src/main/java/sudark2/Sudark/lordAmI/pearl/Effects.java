package sudark2.Sudark.lordAmI.pearl;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class Effects {

    private static final Particle.DustTransition YELLOW =
            new Particle.DustTransition(Color.YELLOW, Color.YELLOW, 0.6f);
    private static final Particle.DustTransition RED_BIG =
            new Particle.DustTransition(Color.RED, Color.RED, 2.5f);

    public static void selectPet(LivingEntity pet, Player issuer) {
        Location at = pet.getLocation().add(0, pet.getHeight() / 2.0, 0);
        pet.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, at,
                20, 0.4, 0.4, 0.4, 0, YELLOW);
        issuer.playSound(issuer.getLocation(), Sound.BLOCK_BEEHIVE_DRIP, 0.8f, 1.4f);
    }

    public static void attackBurst(LivingEntity target, Player issuer) {
        Location head = target.getEyeLocation().add(0, 0.6, 0);
        target.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, head,
                12, 0.25, 0.25, 0.25, 0, RED_BIG);
        issuer.playSound(issuer.getLocation(), Sound.ENTITY_BEE_STING, 0.9f, 1.2f);
    }

    public static void attackPulse(LivingEntity target) {
        Location head = target.getEyeLocation().add(0, 0.6, 0);
        target.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, head,
                4, 0.15, 0.15, 0.15, 0, RED_BIG);
    }

    public static void pathingArc(Location from, Location to, Player issuer) {
        World w = from.getWorld();
        if (w == null || to.getWorld() != w) return;
        Vector start = from.toVector().add(new Vector(0, 1, 0));
        Vector end = to.toVector();
        double dist = start.distance(end);
        double peakY = Math.max(start.getY(), end.getY()) + Math.max(2.0, dist / 3.0);
        Vector mid = start.clone().add(end).multiply(0.5);
        mid.setY(peakY);

        int steps = Math.max(20, (int) (dist * 3));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double oneMinusT = 1 - t;
            Vector p = start.clone().multiply(oneMinusT * oneMinusT)
                    .add(mid.clone().multiply(2 * oneMinusT * t))
                    .add(end.clone().multiply(t * t));
            w.spawnParticle(Particle.END_ROD, p.getX(), p.getY(), p.getZ(),
                    1, 0, 0, 0, 0);
        }
        issuer.playSound(issuer.getLocation(), Sound.BLOCK_BEEHIVE_EXIT, 0.9f, 1.2f);
    }

    public static void arrivedRing(Plugin plugin, Location anchor) {
        new BukkitRunnable() {
            int tick = 0;
            final int duration = 20;

            @Override
            public void run() {
                if (tick > duration) {
                    cancel();
                    return;
                }
                double radius = 5.0 * ((double) tick / duration);
                int count = (int) Math.max(8, radius * 8);
                World w = anchor.getWorld();
                if (w == null) {
                    cancel();
                    return;
                }
                for (int i = 0; i < count; i++) {
                    double angle = 2 * Math.PI * i / count;
                    double x = anchor.getX() + radius * Math.cos(angle);
                    double z = anchor.getZ() + radius * Math.sin(angle);
                    w.spawnParticle(Particle.TOTEM_OF_UNDYING,
                            x, anchor.getY() + 0.1, z,
                            1, 0, 0.05, 0, 0);
                }
                if (tick == 0) {
                    w.playSound(anchor, Sound.BLOCK_BEEHIVE_ENTER, 0.9f, 1.2f);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
