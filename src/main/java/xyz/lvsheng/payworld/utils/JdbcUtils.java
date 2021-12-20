package xyz.lvsheng.payworld.utils;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import xyz.lvsheng.payworld.PayWorld;

import java.sql.*;

/**
 * Jdbc操作工具类
 *
 * @author MrLv
 * @date 2021/10/24
 * @apiNote
 */
public class JdbcUtils {
    public static boolean mysqlEnable;
    private static Connection sql;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //只读取一次
            mysqlEnable = PayWorld.plugins.getConfig().getBoolean("Mysql.enable");

            if (mysqlEnable) {
                //Mysql
                String database = PayWorld.plugins.getConfig().getString("Mysql.database");
                String url = "jdbc:mysql://" + (PayWorld.plugins.getConfig().getString("Mysql.url") + "/").replaceAll("/+", "/") + database + "?&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false";
                String username = PayWorld.plugins.getConfig().getString("Mysql.username");
                String password = PayWorld.plugins.getConfig().getString("Mysql.password");

                sql = DriverManager.getConnection(url, username, password);
            } else {
                //Sqlite
                SQLiteConfig config = new SQLiteConfig();
                config.setSharedCache(true);
                config.enableRecursiveTriggers(true);
                SQLiteDataSource ds = new SQLiteDataSource(config);
                String url = System.getProperty("user.dir"); // 获取工作目录
                ds.setUrl("jdbc:sqlite:" + url + "/plugins/PayWorld/" + "PlayerDataBase.db");
                sql = ds.getConnection();
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static Connection getConnection() throws SQLException {
        return sql;
    }


    /**
     * 创建数据库
     *
     * @throws SQLException sql异常
     */
    public static void createDatabase(Connection conn) throws SQLException {
        String database = PayWorld.plugins.getConfig().getString("Mysql.database");
        String sql = "Create Database If Not Exists " + database + " Character Set UTF8";
        Statement statement = conn.createStatement();
        statement.execute(sql);
    }


    public static void close(Connection conn, Statement statement, ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            conn.close();
                        }
                    }
                }
            }
        }


    }
}
