package xyz.lvsheng.payworld.Commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lvsheng.payworld.Main;
import xyz.lvsheng.payworld.Util.SQLite;
import xyz.lvsheng.payworld.Util.Utils;

import java.sql.SQLException;
import java.util.*;

/**
 * @author MrLv
 * @date 2021/9/4
 * @apiNote
 */
public class pw implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.hasPermission("payworld.admin")) {
            this.com(sender, args.length != 0 ? args : null);
        } else {
            this.com(sender, null);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (Objects.requireNonNull(((Player) sender).getPlayer()).hasPermission("payworld.admin")) {
                List<String> list = new ArrayList<>();
                if (args.length == 1) {
                    list.add("add");
                    list.add("set");
                    list.add("give");
                    list.add("show");
                    return list;
                }

                if (args.length == 2 && !args[0].equalsIgnoreCase("show")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        list.add(player.getName());
                    }
                    return list;
                }

                if (args.length == 3 && !args[0].equalsIgnoreCase("show")) {
                    return Main.plugins.getConfig().getStringList("World");
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
        }
        return null;
    }

    private void com(CommandSender sender, String... args) {
        if (args == null) {
            if (sender instanceof Player) {
                this.show(sender);
            } else {
                sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.notConsole")));
            }
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reload(sender);
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("give")) {
        }


        if (args.length > 2) {
            if (!Utils.equals(Main.plugins.getConfig().getStringList("World"), args[2])) {
                sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.configNoWorld")));
                return;
            }
            int inputTime = 0;
            try {
                inputTime = Integer.parseInt(args[3]);
            } catch (Exception e) {
                this.inputError(sender);
                return;
            }
            if (args[0].equalsIgnoreCase("add")) {
                add(sender, args[1], args[2], inputTime);
            }
            if (args[0].equalsIgnoreCase("set")) {
                set(sender, args[1], args[2], inputTime);
            }
            if (args[0].equalsIgnoreCase("give")) {
                this.give(sender, args[1], args[2], inputTime);
            }

            return;
        }
        this.inputError(sender);
    }

    private void inputError(CommandSender sender) {
        sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.inputError")));
    }

    public static void add(CommandSender sender, String playerName, String worldName, Integer time) {
        try {
            int pTime = SQLite.select(Main.sql, Bukkit.getOfflinePlayer(playerName).getUniqueId(), worldName);
            set(sender, playerName, worldName, (pTime < 0 ? 0 : pTime + time));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void set(CommandSender sender, String playerName, String worldName, Integer time) {
        try {
            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            int pTime = SQLite.select(Main.sql, uuid, worldName);
            if (pTime < 0) {
                SQLite.insert(Main.sql, uuid, worldName, time);
            } else {
                SQLite.update(Main.sql, uuid, worldName, time);
            }
            sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.getTime")).replace("%world%",worldName).replace("%worldTime%",time+""));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void give(CommandSender sender,String playerName,String worldName,Integer time){
        ItemStack card = Utils.createCard(worldName, time);
        Player player = Bukkit.getPlayer(playerName);
        if (!Objects.equals(player,null)) {
            if (player.getInventory().addItem(card).size() == 0) {
                return;
            }
        }else {
            sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.offline")));
            return;
        }
        sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.backpack")));
    }

    private void show(CommandSender sender) {
        try {
            HashMap<String, Integer> pTimeMap = SQLite.select(Main.sql, ((Player) sender).getUniqueId());
            sender.sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.worldTimeList")));
            for (String world : Main.plugins.getConfig().getStringList("World")) {
                Integer worldTime = pTimeMap.get(world);
                if (Objects.equals(worldTime, null)) {
                    worldTime = 0;
                }
                sender.sendMessage("- §6" + world + ": §b" + worldTime);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void reload(CommandSender sender) {
        Main.plugins.reloadConfig();
        sender.sendMessage("§a[PayWorld]§e reloadConfig!");

    }

}
