package sudark2.Sudark.lordAmI;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.lordAmI.Team.TeamManager;
import sudark2.Sudark.lordAmI.capture.EggCaptureListener;
import sudark2.Sudark.lordAmI.capture.ProbabilityHud;
import sudark2.Sudark.lordAmI.pearl.PearlInteractListener;
import sudark2.Sudark.lordAmI.persist.ChunkLoadListener;
import sudark2.Sudark.lordAmI.persist.PetIdStore;
import sudark2.Sudark.lordAmI.persist.PetSnapshot;
import sudark2.Sudark.lordAmI.pet.DeathListener;
import sudark2.Sudark.lordAmI.pet.OwnerCombatListener;
import sudark2.Sudark.lordAmI.pet.PetManager;
import sudark2.Sudark.lordAmI.pet.PetTargetListener;
import sudark2.Sudark.lordAmI.pet.PetTicker;
import sudark2.Sudark.lordAmI.util.EntityIO;
import sudark2.Sudark.lordAmI.util.Keys;

public final class LordAmI extends JavaPlugin {

    @Override
    public void onEnable() {
        Keys.init(this);
        PetIdStore.init(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggCaptureListener(), this);
        pm.registerEvents(new PearlInteractListener(this), this);
        pm.registerEvents(new PetTargetListener(), this);
        pm.registerEvents(new OwnerCombatListener(), this);
        pm.registerEvents(new DeathListener(), this);
        pm.registerEvents(new ChunkLoadListener(), this);

        ProbabilityHud.start(this);
        PetTicker.start(this);

        Bukkit.getScheduler().runTask(this, this::reattachLoadedPets);
    }

    @Override
    public void onDisable() {
        for (var pet : PetManager.all().values()) {
            if (pet.entity == null || !pet.entity.isValid()) continue;
            PetSnapshot snap = PetIdStore.get(pet.petId);
            if (snap == null) continue;
            EntityIO.capture(pet.entity, snap);
            snap.entityUuid = pet.entity.getUniqueId();
            snap.world = pet.entity.getWorld().getName();
            snap.state = PetSnapshot.State.DEPLOYED;
        }
        PetIdStore.saveAll();
    }

    private void reattachLoadedPets() {
        for (World w : Bukkit.getWorlds()) {
            for (Entity en : w.getEntities()) {
                if (!(en instanceof LivingEntity le)) continue;
                if (!PetManager.isPet(le)) continue;
                if (PetManager.getFromEntity(le) != null) continue;
                String owner = PetManager.readOwner(le);
                String petId = PetManager.readPetId(le);
                if (owner == null || petId == null) continue;
                PetManager.register(le, owner, petId);
                TeamManager.addEntity(owner, le);
            }
        }
    }
}
