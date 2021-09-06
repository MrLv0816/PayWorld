package xyz.lvsheng.antiaddiction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.lvsheng.antiaddiction.ListEvent.event;

import java.util.Calendar;
import java.util.Date;

/**
 * @author MrLv
 * @date 2021/9/1
 * @apiNote
 */
public class Timer extends BukkitRunnable {
    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if(calendar.get(Calendar.HOUR) >= 21){
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!Main.plugins.getConfig().getBoolean(onlinePlayer.getUniqueId()+".age")) {
                    onlinePlayer.kickPlayer("国家新闻出版署下发\n" +
                            "《关于进一步严格管理 切实防止未成年人沉迷网络游戏的通知》\n针对未成年人过度使用甚至沉迷网络游戏问题，进一步严格管理措施，坚决防止未成年人沉迷网络游戏，切实保护未成年人身心健康。\n" +
                            "\n" +
                            "通知要求，严格限制向未成年人提供网络游戏服务的时间，所有网络游戏企业仅可在周五、周六、周日和法定节假日每日20时至21时向未成年人提供1小时服务，其他时间均不得以任何形式向未成年人提供网络游戏服务；");
                }
            }
        }

    }
}
