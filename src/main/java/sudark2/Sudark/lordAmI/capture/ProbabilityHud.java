package sudark2.Sudark.lordAmI.capture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import sudark2.Sudark.lordAmI.pet.PetManager;
import sudark2.Sudark.lordAmI.util.Msg;

public final class ProbabilityHud extends BukkitRunnable {

    public static void start(Plugin plugin) {
        new ProbabilityHud().runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getInventory().getItemInMainHand().getType() != Material.EGG) continue;
            LivingEntity target = lookAt(p);
            if (target == null) continue;
            if (PetManager.isPet(target)) {
                p.sendActionBar(Msg.cn("§6该生物已被收复"));
                continue;
            }
            double chance = chance(p, target);
            p.sendActionBar(Msg.cn(String.format("§e捕获概率: §f%.2f%%", chance * 100.0)));
        }
    }

    public static double chance(Player p, LivingEntity target) {
        int level = p.getLevel();
        double hp = target.getHealth();
        if (level <= 0) return 0.0;
        return (double) level / (level + hp);
    }

    private static LivingEntity lookAt(Player p) {
        RayTraceResult r = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                5.0,
                0.3,
                e -> e instanceof LivingEntity && !(e instanceof Player) && e != p && !e.isDead()
        );
        if (r == null) return null;
        Entity hit = r.getHitEntity();
        return hit instanceof LivingEntity le ? le : null;
    }
}
