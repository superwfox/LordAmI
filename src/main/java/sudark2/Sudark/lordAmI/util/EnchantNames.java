package sudark2.Sudark.lordAmI.util;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public final class EnchantNames {

    private static final Map<String, String> CN = new HashMap<>();

    static {
        CN.put("protection", "保护");
        CN.put("fire_protection", "火焰保护");
        CN.put("feather_falling", "摔落保护");
        CN.put("blast_protection", "爆炸保护");
        CN.put("projectile_protection", "弹射物保护");
        CN.put("respiration", "水下呼吸");
        CN.put("aqua_affinity", "水下速掘");
        CN.put("thorns", "荆棘");
        CN.put("depth_strider", "深海漫步");
        CN.put("frost_walker", "冰霜行者");
        CN.put("binding_curse", "束缚诅咒");
        CN.put("soul_speed", "灵魂疾行");
        CN.put("swift_sneak", "迅捷潜行");
        CN.put("sharpness", "锋利");
        CN.put("smite", "亡灵杀手");
        CN.put("bane_of_arthropods", "节肢杀手");
        CN.put("knockback", "击退");
        CN.put("fire_aspect", "火焰附加");
        CN.put("looting", "抢夺");
        CN.put("sweeping_edge", "横扫之刃");
        CN.put("efficiency", "效率");
        CN.put("silk_touch", "精准采集");
        CN.put("unbreaking", "耐久");
        CN.put("fortune", "时运");
        CN.put("power", "力量");
        CN.put("punch", "冲击");
        CN.put("flame", "火矢");
        CN.put("infinity", "无限");
        CN.put("luck_of_the_sea", "海之眷顾");
        CN.put("lure", "饵钓");
        CN.put("loyalty", "忠诚");
        CN.put("impaling", "穿刺");
        CN.put("riptide", "激流");
        CN.put("channeling", "引雷");
        CN.put("multishot", "多重射击");
        CN.put("quick_charge", "快速装填");
        CN.put("piercing", "穿透");
        CN.put("mending", "经验修补");
        CN.put("vanishing_curse", "消失诅咒");
        CN.put("density", "致密");
        CN.put("breach", "破甲");
        CN.put("wind_burst", "风爆");
    }

    public static String of(Enchantment e) {
        String key = e.getKey().getKey();
        return CN.getOrDefault(key, key);
    }
}
