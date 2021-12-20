package xyz.lvsheng.payworld;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lvsheng.payworld.commands.Pw;
import xyz.lvsheng.payworld.event.Listener;
import xyz.lvsheng.payworld.event.Papi;
import xyz.lvsheng.payworld.utils.JdbcUtils;
import xyz.lvsheng.payworld.utils.SqlUtils;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;
import java.util.Objects;

public final class PayWorld extends JavaPlugin {
    public static PayWorld plugins;


    @Override
    public void onEnable() {
        // Plugin startup logic
        this.init();
        Bukkit.getConsoleSender().sendMessage("§a[PayWorld] §ePlugin load successful!");
    }

    @Override
    public void onDisable() {
        try {
            JdbcUtils.getConnection().close();
        } catch (SQLException e) {
            //
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
            SqlUtils.initSql();
            // register command
            Objects.requireNonNull(getCommand("payworld")).setExecutor(new Pw());
            // register event
            this.placeholder();
            Bukkit.getPluginManager().registerEvents(new Listener(), this);
            // timer start
            new Task().runTaskTimerAsynchronously(this, 20 * 60, 20 * 60);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void placeholder() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Papi().register();
        } else {
            Bukkit.getConsoleSender().sendMessage(Utils.getMessage("NoAPI"));
        }
    }

}
