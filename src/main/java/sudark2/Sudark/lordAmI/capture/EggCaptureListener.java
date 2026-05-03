package sudark2.Sudark.lordAmI.capture;

import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.lordAmI.pearl.PearlFactory;
import sudark2.Sudark.lordAmI.persist.PetIdStore;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;
import sudark2.Sudark.lordAmI.pet.PetManager;
import sudark2.Sudark.lordAmI.util.Identity;
import sudark2.Sudark.lordAmI.util.Msg;

import java.util.Random;

public final class EggCaptureListener implements Listener {

    private static final Random RNG = new Random();
    private static final int CAPTURE_COOLDOWN_TICKS = 20 * 30;

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Egg egg)) return;
        if (!(egg.getShooter() instanceof Player p)) return;
        Entity hit = e.getHitEntity();
        if (!(hit instanceof LivingEntity target)) return;
        if (target instanceof Player) return;
        if (target == p) return;
        if (PetManager.isPet(target)) {
            p.sendActionBar(Msg.cn("§6该生物已被收复"));
            return;
        }

        String qq = Identity.of(p);
        if (qq == null) {
            p.sendActionBar(Msg.cn("§6未绑定 QQ,无法收复"));
            return;
        }
        if (!PearlFactory.hasPlain(p)) {
            p.sendActionBar(Msg.cn("§6需要末影之眼才能收复"));
            return;
        }

        double chance = ProbabilityHud.chance(p, target);
        if (RNG.nextDouble() < chance) {
            PearlFactory.consumePlain(p);
            String petId = PetManager.capture(qq, target);
            target.remove();
            PetSnapshot snap = PetIdStore.get(petId);
            ItemStack pearl = PearlFactory.create(snap);
            if (p.getInventory().firstEmpty() == -1) {
                p.getWorld().dropItem(p.getLocation(), pearl);
            } else {
                p.getInventory().addItem(pearl);
            }
            p.setCooldown(Material.ENDER_EYE, CAPTURE_COOLDOWN_TICKS);
            p.sendActionBar(Msg.cn("§e收复成功!"));
        } else {
            double dmg = target.getHealth();
            p.damage(dmg, target);
            p.sendActionBar(Msg.cn(String.format("§6收复失败! 受到 §f%.1f§6 点伤害", dmg)));
        }
    }
}
