package sudark2.Sudark.lordAmI.persist;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public class PetSnapshot {

    public String petId;
    public String owner;
    public EntityType type;
    public String displayName;
    public double maxHealth;
    public int age;
    public boolean saddled;

    public byte[] head, chest, legs, feet, mainHand, offHand;
    public byte[] horseSaddle, horseArmor;

    public State state = State.STORED;
    public UUID entityUuid;
    public String world;

    public enum State { STORED, DEPLOYED, DEAD }
}
