package sudark2.Sudark.lordAmI.pearl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import sudark2.Sudark.lordAmI.Team.TeamManager;
import sudark2.Sudark.lordAmI.persist.PetIdStore;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;
import sudark2.Sudark.lordAmI.pet.Pet;
import sudark2.Sudark.lordAmI.pet.PetManager;
import sudark2.Sudark.lordAmI.pet.PetState;
import sudark2.Sudark.lordAmI.util.EntityIO;
import sudark2.Sudark.lordAmI.util.Identity;
import sudark2.Sudark.lordAmI.util.Keys;
import sudark2.Sudark.lordAmI.util.Msg;

public final class PearlInteractListener implements Listener {

    private final Plugin plugin;

    public PearlInteractListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = e.getItem();
        if (!PearlFactory.isAnyEye(item)) return;
        Player p = e.getPlayer();
        Action a = e.getAction();

        if (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR) {
            boolean frameClick = a == Action.RIGHT_CLICK_BLOCK
                    && e.getClickedBlock().getType() == Material.END_PORTAL_FRAME;
            if (frameClick && !PearlFactory.isStored(item)) return;

            e.setCancelled(true);
            e.setUseItemInHand(Event.Result.DENY);
            if (a == Action.RIGHT_CLICK_BLOCK && PearlFactory.isStored(item)) {
                if (frameClick) {
                    p.sendActionBar(Msg.cn("§6收复之眼不能填充末地传送门"));
                    return;
                }
                if (onCooldown(p)) return;
                handleDeploy(p, e.getClickedBlock(), e.getBlockFace(), item);
            }
            return;
        }

        if (a == Action.LEFT_CLICK_BLOCK) {
            e.setCancelled(true);
            handleLeftBlock(p, e.getClickedBlock());
        }
    }

    private boolean onCooldown(Player p) {
        if (p.hasCooldown(Material.ENDER_EYE)) {
            p.sendActionBar(Msg.cn("§7末影之眼冷却中"));
            return true;
        }
        return false;
    }

    private String requireQq(Player p) {
        String qq = Identity.of(p);
        if (qq == null) {
            p.sendActionBar(Msg.cn("§6未绑定 QQ"));
        }
        return qq;
    }

    private void handleDeploy(Player p, Block clicked, BlockFace face, ItemStack item) {
        String qq = requireQq(p);
        if (qq == null) return;
        String petId = PearlFactory.readPetId(item);
        if (petId == null) return;
        PetSnapshot snap = PetIdStore.get(petId);
        if (snap == null) {
            p.sendActionBar(Msg.cn("§6数据丢失,末影之眼已失效"));
            item.setAmount(item.getAmount() - 1);
            return;
        }
        if (!qq.equals(snap.owner)) {
            p.sendActionBar(Msg.cn("§6这不是你的末影之眼"));
            return;
        }
        if (snap.state == PetSnapshot.State.DEAD) {
            p.sendActionBar(Msg.cn("§6生物已死亡,末影之眼失效"));
            item.setAmount(item.getAmount() - 1);
            return;
        }
        if (snap.state == PetSnapshot.State.DEPLOYED) {
            p.sendActionBar(Msg.cn("§6该生物已在世界中"));
            return;
        }

        Block target = clicked.getRelative(face == null ? BlockFace.UP : face);
        Location spawn = target.getLocation().add(0.5, 0.0, 0.5);
        World w = spawn.getWorld();
        if (w == null) return;
        Entity spawned = w.spawnEntity(spawn, snap.type);
        if (!(spawned instanceof LivingEntity le)) {
            spawned.remove();
            p.sendActionBar(Msg.cn("§6不是有效生物"));
            return;
        }

        EntityIO.apply(le, snap);
        le.getPersistentDataContainer().set(Keys.OWNER, PersistentDataType.STRING, snap.owner);
        le.getPersistentDataContainer().set(Keys.PET_ID, PersistentDataType.STRING, snap.petId);
        TeamManager.addEntity(snap.owner, le);
        PetManager.register(le, snap.owner, snap.petId);
        PetIdStore.onDeployed(snap, le.getUniqueId(), w.getName());

        p.getInventory().setItemInMainHand(PearlFactory.createPlain());
        w.playSound(spawn, Sound.BLOCK_BEEHIVE_EXIT, 0.9f, 1.0f);
        p.sendActionBar(Msg.cn("§e已放置"));
    }

    private void handleLeftBlock(Player p, Block clicked) {
        if (clicked == null) return;
        String qq = requireQq(p);
        if (qq == null) return;

        Pet selected = SelectionState.get(qq);
        if (selected != null) {
            if (!selected.owner.equals(qq)) {
                SelectionState.clear(qq);
                return;
            }
            Location anchor = clicked.getLocation().add(0.5, 1.0, 0.5);
            Location from = selected.entity != null ? selected.entity.getLocation() : p.getLocation();
            selected.state = PetState.PATHING;
            selected.anchor = anchor;
            selected.pathingArrived = false;
            selected.attackTargetUuid = null;
            if (selected.entity instanceof Mob m) m.getPathfinder().moveTo(anchor, 2.5f);
            Effects.pathingArc(from, anchor, p);
            SelectionState.clear(qq);
            p.sendActionBar(Msg.cn("§e已派遣 §f" + nameOf(selected)));
            return;
        }

        boolean sneaking = p.isSneaking();
        int n = 0;
        for (Pet pet : PetManager.byOwner(qq)) {
            if (pet.entity == null || !pet.entity.isValid()) continue;
            if (!sneaking && pet.state == PetState.PATHING) continue;
            pet.state = PetState.FOLLOW;
            pet.anchor = null;
            pet.pathingArrived = false;
            pet.attackTargetUuid = null;
            if (pet.entity instanceof Mob m) m.setTarget(null);
            n++;
        }
        p.playSound(p.getLocation(), Sound.BLOCK_BEEHIVE_WORK, 0.8f, 1.4f);
        p.sendActionBar(Msg.cn("§e召集了 §f" + n + "§e 只生物" + (sneaking ? " §7(含寻路)" : "")));
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!PearlFactory.isAnyEye(item)) return;
        if (!(e.getRightClicked() instanceof LivingEntity le)) return;
        if (!PetManager.isPet(le)) return;
        String qq = requireQq(p);
        if (qq == null) {
            e.setCancelled(true);
            return;
        }
        String owner = PetManager.readOwner(le);
        if (owner == null || !owner.equals(qq)) return;
        if (onCooldown(p)) {
            e.setCancelled(true);
            return;
        }
        if (!PearlFactory.hasPlain(p)) {
            e.setCancelled(true);
            p.sendActionBar(Msg.cn("§6需要末影之眼才能收回"));
            return;
        }

        e.setCancelled(true);
        recall(p, le);
    }

    private void recall(Player p, LivingEntity le) {
        String petId = PetManager.readPetId(le);
        if (petId == null) return;
        PetSnapshot snap = PetIdStore.get(petId);
        if (snap == null) {
            le.remove();
            return;
        }
        PearlFactory.consumePlain(p);
        EntityIO.capture(le, snap);
        TeamManager.removeEntity(le);
        PetManager.unregister(le.getUniqueId());
        Location was = le.getLocation();
        le.remove();
        PetIdStore.onStored(snap);

        ItemStack pearl = PearlFactory.create(snap);
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), pearl);
        } else {
            p.getInventory().addItem(pearl);
        }
        p.getWorld().playSound(was, Sound.BLOCK_BEEHIVE_ENTER, 0.9f, 1.0f);
        p.sendActionBar(Msg.cn("§e已收回"));
    }

    @EventHandler
    public void onLeftClickEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!PearlFactory.isAnyEye(item)) return;
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        e.setCancelled(true);

        String qq = requireQq(p);
        if (qq == null) return;

        if (PetManager.isPet(le)) {
            String owner = PetManager.readOwner(le);
            if (owner != null && owner.equals(qq)) {
                Pet pet = PetManager.getFromEntity(le);
                if (pet != null) {
                    SelectionState.select(qq, pet);
                    Effects.selectPet(le, p);
                    p.sendActionBar(Msg.cn("§b已选中 §f" + nameOf(pet)));
                }
                return;
            }
        }

        Pet selected = SelectionState.get(qq);
        if (selected == null) return;
        if (!selected.owner.equals(qq)) {
            SelectionState.clear(qq);
            return;
        }
        if (selected.entity == le) return;

        selected.state = PetState.ATTACKING;
        selected.attackTargetUuid = le.getUniqueId();
        if (selected.entity instanceof Mob m) m.setTarget(le);
        Effects.attackBurst(le, p);
        SelectionState.clear(qq);
        p.sendActionBar(Msg.cn("§6已下达攻击指令"));
    }

    private static String nameOf(Pet pet) {
        if (pet.entity == null) return pet.petId.substring(0, 8);
        String n = pet.entity.getCustomName();
        return n != null ? n : pet.entity.getType().name();
    }
}
