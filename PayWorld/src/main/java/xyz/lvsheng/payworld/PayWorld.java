package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lvsheng.payworld.commands.Pw;
import xyz.lvsheng.payworld.utils.SQLite;
import xyz.lvsheng.payworld.event.Listener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class PayWorld extends JavaPlugin {
    public static Connection sql;
    public static PayWorld plugins;


    @Override
    public void onEnable() {
        // Plugin startup logic
        this.init();
        Bukkit.getConsoleSender().sendMessage("§a[PayWorld] §ePlugin load successful!");
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
            Objects.requireNonNull(getCommand("payworld")).setExecutor(new Pw());
            // register event
            Bukkit.getPluginManager().registerEvents(new Listener(), this);
            // timer start
            new Task().runTaskTimerAsynchronously(this, 20 * 60, 20 * 60);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
