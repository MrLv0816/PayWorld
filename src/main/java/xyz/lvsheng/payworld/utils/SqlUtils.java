package xyz.lvsheng.payworld.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.lvsheng.payworld.PayWorld;

import java.sql.*;
import java.util.*;

/**
 * @author MrLv
 * @date 2021/12/15
 * @apiNote
 */
public class SqlUtils {


    /**
     * 数据库初始化
     *
     * @throws SQLException sql异常
     */
    public static void initSql() throws SQLException {

        //获取数据库连接池
        Connection connection = JdbcUtils.getConnection();

        //建立数据库
        if (PayWorld.plugins.getConfig().getBoolean("Mysql.enable")) {
            JdbcUtils.createDatabase(connection);
        }

        //创建表
        createTable(connection);
    }


    /**
     * 创建表
     *
     * @param conn 数据库连接池
     * @throws SQLException sql异常
     */
    public static void createTable(Connection conn) throws SQLException {

        StringBuilder worldSQL = new StringBuilder();

        //获取世界列表 拼接 sql 语句
        for (World world : Bukkit.getWorlds()) {
            worldSQL.append(", `").append(world.getName().toLowerCase()).append("` int DEFAULT 0");
        }

        String sql = "CREATE TABLE IF NOT EXISTS `Player`  (  `uuid` varchar(36) NOT NULL" + worldSQL +
                ",  PRIMARY KEY (`uuid`)) ";

        Statement stat = conn.createStatement();

        stat.executeUpdate(sql);
    }


    /**
     * 玩家进入服务器后调用一次本方法
     * @param uuid  玩家唯一标识
     * @return  数据库受影响记录数
     * @throws SQLException sql异常
     */
    public static int addPlayer(UUID uuid) throws SQLException {

        String sql = "INSERT";
        if (!JdbcUtils.mysqlEnable) {
            sql += " OR";
        }
        sql += " IGNORE INTO Player (uuid) VALUES(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        int updateSql = -1;
        try {
            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, uuid.toString());

            updateSql = ps.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return updateSql;
    }


    /**
     * 修改增加玩家的时间
     * @param uuid  玩家唯一标识
     * @param world 世界
     * @param time  时间
     * @param set   是否为直接修改
     * @return  数据库受影响记录数
     * @throws SQLException slq异常
     */
    public static int updateTime(UUID uuid, String world, int time, boolean set) throws SQLException {


        String sql = "UPDATE Player SET " + world + " = " + world + "+? WHERE uuid = ?";

        if (set) {
            sql = "UPDATE Player SET " + world + " = ? WHERE uuid = ?";
        }

        Connection conn = null;
        PreparedStatement ps = null;
        int updateSql = -1;

        try {
            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, Math.max(0,time));
            ps.setString(2, uuid.toString());


            updateSql = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return updateSql;
    }

    public static int getTime(UUID uuid, String world) throws SQLException {

        String sql = "SELECT `" + world + "` from Player WHERE UUID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, uuid.toString());

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(world);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static Map<String, Integer> getAll(UUID uuid) throws SQLException {

        String sql = "SELECT * from Player WHERE UUID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();

            Map<String, Integer> resultMap = new HashMap<>();

            List<String> world = PayWorld.plugins.getConfig().getStringList("World");
            while (rs.next()) {
                for (String w : world) {
                    resultMap.put(w,rs.getInt(w));
                }
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static void updateSqlField(String world) throws SQLException {

        World bw = Bukkit.getWorld(world);
        if (bw == null) {
            throw new SQLException("检测到了不存在的世界,创建数据库字段失败...");
        }


        String sql = "SELECT COUNT(?) FROM Player";
        Connection conn = null;
        PreparedStatement ps = null;

        try {

            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, world.toLowerCase(Locale.ROOT));

            ps.executeQuery();

            //没有异常不做任何处理
        } catch (SQLException e) {
            sql = "ALTER TABLE Player ADD COLUMN ? int DEFAULT 0";

            conn = JdbcUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, world.toLowerCase());
            ps.executeUpdate();


        }


    }
}
