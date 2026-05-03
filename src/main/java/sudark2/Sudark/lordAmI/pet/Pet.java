package sudark2.Sudark.lordAmI.pet;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class Pet {

    public final String petId;
    public final String owner;
    public LivingEntity entity;

    public PetState state = PetState.FOLLOW;
    public Location anchor;
    public boolean pathingArrived;
    public UUID attackTargetUuid;

    public Pet(String petId, String owner, LivingEntity entity) {
        this.petId = petId;
        this.owner = owner;
        this.entity = entity;
    }
}
