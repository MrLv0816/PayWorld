package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.lvsheng.payworld.Util.SQLite;
import xyz.lvsheng.payworld.Util.Utils;
import xyz.lvsheng.payworld.listEvent.Events;

import java.sql.SQLException;
import java.util.Objects;


/**
 * @author MrLv
 * @date 2021/9/3
 * @apiNote
 */
public class Task extends BukkitRunnable {

    @Override
    public void run() {
        for (Player players : Bukkit.getOnlinePlayers()) {

            //刚进入某个付费世界的玩家跳过一次扫描,以防被瞬间传送出去
            if (!Objects.equals(Events.playerMap.get(players.getUniqueId()), null) && Events.playerMap.get(players.getUniqueId())) {
                Events.playerMap.put(players.getUniqueId(), false);
                continue;
            }


            for (String s : Main.plugins.getConfig().getStringList("World")) {


                if (players.getWorld().getName().equalsIgnoreCase(s)) {
                    try {
                        int time = SQLite.select(Main.sql, players.getUniqueId(), players.getWorld().getName());
                        SQLite.update(Main.sql, players.getUniqueId(), players.getWorld().getName(), --time < 0 ? 0 : time);
                        if (time < 1) {
                            Bukkit.getScheduler().runTask(Main.plugins,new Thread(()->players.teleport(Utils.searchWorld(Main.plugins.getConfig().getString("BackWorld")).getSpawnLocation())));
                            players.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.timeOut")));
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }


                }
            }
        }
    }
}