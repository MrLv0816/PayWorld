package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.lvsheng.payworld.utils.SQLite;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;


/**
 * @author MrLv
 * @date 2021/9/3
 * @apiNote
 */
public class Task extends BukkitRunnable {

    @Override
    public void run() {
        for (String world : PayWorld.plugins.getConfig().getStringList("World")) {
            for (Player player : Bukkit.getWorld(world).getPlayers()) {
                //检测玩家时间
                try {
                    int pTime = SQLite.select(PayWorld.sql, player.getUniqueId(), world);
                    SQLite.update(PayWorld.sql, player.getUniqueId(), world, --pTime);
                    if (pTime < 1){
                        player.sendMessage
                                (Utils.ColorMessage(PayWorld.plugins.getConfig().getString("Messages.timeOut")));
                        player.teleport(Utils.searchWorld(PayWorld.plugins.getConfig().getString("BackWorld")).getSpawnLocation());
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }
    }
}