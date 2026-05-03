package sudark2.Sudark.lordAmI.Team;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class TeamManager {

    public static Team getOrCreate(String owner) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String name = "lami_" + owner;
        Team t = sb.getTeam(name);
        if (t == null) {
            t = sb.registerNewTeam(name);
            t.setAllowFriendlyFire(true);
            t.setCanSeeFriendlyInvisibles(false);
        }
        return t;
    }

    public static void addEntity(String owner, LivingEntity entity) {
        getOrCreate(owner).addEntity(entity);
    }

    public static void removeEntity(LivingEntity entity) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team t = sb.getEntityTeam(entity);
        if (t != null && t.getName().startsWith("lami_")) t.removeEntity(entity);
    }
}
