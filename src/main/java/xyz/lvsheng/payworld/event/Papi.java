package xyz.lvsheng.payworld.event;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import xyz.lvsheng.payworld.PayWorld;
import xyz.lvsheng.payworld.utils.SQLite;

import java.sql.SQLException;

public class Papi extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "payworld";
    }

    @Override
    public String getAuthor() {
        return "MrLv";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        for (World world : Bukkit.getWorlds()) {
            if (params.equalsIgnoreCase(world.getName())) {

                try {
                    SQLite.carateWorldColumn(PayWorld.sql);
                    return String.valueOf(Math.max(SQLite.select(PayWorld.sql, player.getUniqueId(), world.getName()), 0));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }
        return "0";
    }
}
