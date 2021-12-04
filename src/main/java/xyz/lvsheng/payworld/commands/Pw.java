package xyz.lvsheng.payworld.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.lvsheng.payworld.PayWorld;
import xyz.lvsheng.payworld.utils.SQLite;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;
import java.util.*;

/**
 * @author MrLv
 * @date 2021/9/4
 * @apiNote
 */
public class Pw implements CommandExecutor, TabCompleter {

    /**
     * 添加玩家的时长
     * @param sender 命令执行人
     * @param playerName 玩家名称
     * @param worldName 世界名称
     * @param time  时间
     */
    public static void add(CommandSender sender, String playerName, String worldName, Integer time) {
        try {
            int pTime = SQLite.select(PayWorld.sql, Bukkit.getOfflinePlayer(playerName).getUniqueId(), worldName);
            set(sender, playerName, worldName, (pTime < 0 ? time : pTime + time));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * 修改玩家的时长
     * @param sender   命令执行人
     * @param playerName    玩家名称
     * @param worldName 世界名称
     * @param time  时间
     */
    public static void set(CommandSender sender, String playerName, String worldName, Integer time) {

        try {
            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            int pTime = SQLite.select(PayWorld.sql, uuid, worldName);
            if (pTime < 0) {
                SQLite.insert(PayWorld.sql, uuid, worldName, time);
            } else {
                SQLite.update(PayWorld.sql, uuid, worldName, time);
            }
            sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.giveTime")).replace("%world%", worldName).replace("%worldTime%", time + ""));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.hasPermission("payworld.admin")) {
            this.com(sender, args.length != 0 ? args : null);
        } else {
            if (sender.hasPermission("payworld.show")) {
                this.com(sender, null);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            try{
                if (Objects.requireNonNull(((Player) sender).getPlayer()).hasPermission("payworld.admin")) {
                    List<String> list = new ArrayList<>();
                    if (args.length == 1) {
                        list.add("add");
                        list.add("set");
                        list.add("give");
                        list.add("show");
                        list.add("updataSQL");
                        return list;
                    }

                    if (args.length == 2 && !args[0].equalsIgnoreCase("show")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            list.add(player.getName());
                        }
                        return list;
                    }

                    if (args.length == 3 && !args[0].equalsIgnoreCase("show")) {
                        return PayWorld.plugins.getConfig().getStringList("World");
                    }

                    if (args.length == 4 && !args[0].equalsIgnoreCase("show")) {
                        list.add("0");
                        list.add("60");
                        list.add("120");
                        list.add("240");
                        list.add("480");
                        list.add("960");
                        return list;
                    }

                }
            }catch (Exception exception){
                //
            }
        }
        return null;
    }

    private void com(CommandSender sender, String... args) {


        if (args == null) {
            if (sender instanceof Player) {
                this.show(sender);
            } else {
                sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.notConsole")));
            }
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reload(sender);
            return;
        }

        //检测世界变动,增加列
        try {
            SQLite.carateWorldColumn(PayWorld.sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (args.length > 2) {
            //判断输入的世界是否存在于配置文件中
            if (!Utils.equals(PayWorld.plugins.getConfig().getStringList("World"), args[2])) {
                sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.configNoWorld")));
                return;
            }

            //判断输入是否为数字
            int inputTime;
            try {
                inputTime = Integer.parseInt(args[3]);
            } catch (Exception e) {
                this.inputError(sender);
                return;
            }


            if (args[0].equalsIgnoreCase("add")) {
                add(sender, args[1], args[2], inputTime);
                return;
            }
            if (args[0].equalsIgnoreCase("set")) {
                set(sender, args[1], args[2], inputTime);
                return;
            }
            if (args[0].equalsIgnoreCase("give")) {
                this.give(sender, args[1], args[2], inputTime);
                return;
            }
        }
        this.inputError(sender);
    }

    private void inputError(CommandSender sender) {
        sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.inputError")
                .replaceAll("\\\\n", "\n")));
    }

    private void give(CommandSender sender, String playerName, String worldName, Integer time) {
        ItemStack card = Utils.createCard(worldName, time);
        Player player = Bukkit.getPlayer(playerName);
        if (!Objects.equals(player, null)) {
            if (!(player.getInventory().addItem(card).size() == 0)) {
                sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.backpack")));
            }
        } else {
            sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.offline")));
        }
    }

    private void show(CommandSender sender) {
        try {
            HashMap<String, Integer> pTimeMap = SQLite.select(PayWorld.sql, ((Player) sender).getUniqueId());
            sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.worldTimeList")));
            for (String world : PayWorld.plugins.getConfig().getStringList("World")) {
                    try{
                        String worldName = Objects.requireNonNull(Bukkit.getWorld(world)).getName();
                        Integer worldTime = pTimeMap.get(worldName);
                        if (Objects.equals(worldTime, null)) {
                            worldTime = 0;
                        }
                        sender.sendMessage("- §6" + world + ": §b" + worldTime);
                    }catch (NullPointerException e){
                        Bukkit.getConsoleSender().sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.NoWorld")));
                        sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.NoWorld")));
                    }


            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void reload(CommandSender sender) {
        PayWorld.plugins.reloadConfig();
        sender.sendMessage(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.reload")));
    }

}
