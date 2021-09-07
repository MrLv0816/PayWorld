package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.lvsheng.payworld.event.Listener;
import xyz.lvsheng.payworld.utils.SQLite;
import xyz.lvsheng.payworld.utils.Utils;

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
            if (!Objects.equals(Listener.playerMap.get(players.getUniqueId()), null) && Listener.playerMap.get(players.getUniqueId())) {
                Listener.playerMap.put(players.getUniqueId(), false);
                continue;
            }


            for (String s : PayWorld.plugins.getConfig().getStringList("World")) {


                if (players.getWorld().getName().equalsIgnoreCase(s)) {
                    try {
                        int time = SQLite.select(PayWorld.sql, players.getUniqueId(), players.getWorld().getName());
                        SQLite.update(PayWorld.sql, players.getUniqueId(), players.getWorld().getName(), --time);
                        if (time < 1) {
                            Bukkit.getScheduler().runTask(PayWorld.plugins, new Thread(() -> players.teleport(Utils.searchWorld(PayWorld.plugins.getConfig().getString("BackWorld")).getSpawnLocation())));
                            players.sendMessage(Utils.ColorMessage(PayWorld.plugins.getConfig().getString("Messages.timeOut")));
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }


                }
            }
        }
    }
}