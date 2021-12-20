package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.lvsheng.payworld.event.Listener;
import xyz.lvsheng.payworld.utils.SqlUtils;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;
import java.util.Collection;


/**
 * @author MrLv
 * @date 2021/9/3
 * @apiNote
 */
public class Task extends BukkitRunnable {

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {

            //不在付费世界
            if (!Utils.matchWorld(player.getWorld().getName())) {
                continue;
            }

            //忽略一次计时
            Boolean skip = Listener.skipList.get(player.getUniqueId());
            if (skip != null) {
                if (skip) {
                    Listener.skipList.put(player.getUniqueId(), false);
                    return;
                }
            }


            try {
                //更新时长
                int time = SqlUtils.getTime(player.getUniqueId(), player.getWorld().getName());
                SqlUtils.updateTime(player.getUniqueId(), player.getWorld().getName(), --time, true);
                if (time > 0) {
                    return;
                }

                Bukkit.getScheduler().runTask(PayWorld.plugins,
                        () -> player.teleport(Utils.backWorld().getSpawnLocation()));
                player.sendMessage(Utils.getMessage("timeOut"));


            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
    //    for (String world : PayWorld.plugins.getConfig().getStringList("World")) {
    //        try{
    //            for (Player player : Objects.requireNonNull(Bukkit.getWorld(world)).getPlayers()) {
    //                //检测玩家时间
    //                try {
    //
    //                    int pTime = SQLite.select(PayWorld.sql, player.getUniqueId(), world);
    //                    SQLite.update(PayWorld.sql, player.getUniqueId(), world, --pTime);
    //                    if (pTime < 1) {
    //                        player.sendMessage
    //                                (Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.timeOut")));
    //                        Bukkit.getScheduler().runTask(PayWorld.plugins, new Thread(() -> player.teleport(Utils.searchWorld(PayWorld.plugins.getConfig().getString("BackWorld")).getSpawnLocation())));
    //                    }
    //
    //
    //                } catch (SQLException throwables) {
    //                    throwables.printStackTrace();
    //                }
    //            }
    //        }catch (NullPointerException e){
    //            Bukkit.getConsoleSender().sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.NoWorld")));
    //        }
    //    }
}