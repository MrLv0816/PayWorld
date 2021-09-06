package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lvsheng.payworld.Commands.pw;
import xyz.lvsheng.payworld.Util.SQLite;
import xyz.lvsheng.payworld.listEvent.Events;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Connection sql;
    public static Main plugins;


    @Override
    public void onEnable() {
        // Plugin startup logic
        this.init();
        Bukkit.getConsoleSender().sendMessage("§a[PayWorld] §eload!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            sql.close();
        } catch (SQLException throwables) {
            //都卸载了,异常就异常吧...
        }
    }

    /**
     * 初始化
     */
    private void init() {
        try {
            // JavaPlugin
            plugins = this;
            // create config
            saveDefaultConfig();
            //get sql
            sql = SQLite.getConnection();
            // create table
            SQLite.createTable(sql);
            // register command
            Objects.requireNonNull(getCommand("payworld")).setExecutor(new pw());
            // register event
            Bukkit.getPluginManager().registerEvents(new Events(), this);
            // timer start
            new Task().runTaskTimerAsynchronously(this, 20 * 60, 20 * 60);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
