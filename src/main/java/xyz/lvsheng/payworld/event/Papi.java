package xyz.lvsheng.payworld.event;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import xyz.lvsheng.payworld.utils.SqlUtils;


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
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        try {

            //验证数据库字段中是否存在这个世界
            SqlUtils.updateSqlField(params);
            //时间
            return String.valueOf(Math.max(0,SqlUtils.getTime(player.getUniqueId(),params)));


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }



}
