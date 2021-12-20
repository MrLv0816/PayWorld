package xyz.lvsheng.payworld.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lvsheng.payworld.PayWorld;
import xyz.lvsheng.payworld.utils.SqlUtils;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;
import java.util.*;

/**
 * @author MrLv
 * @date 2021/9/4
 * @apiNote
 */
public class Pw implements CommandExecutor, TabCompleter {
    private CommandSender sender;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender = commandSender;

        //显示剩余时长
        if (args.length == 0) {
            if (sender instanceof Player) {
                showTime();
            } else {
                sender.sendMessage(Utils.getMessage("notConsole"));
            }

            return true;
        }

        //重载配置文件
        if (args.length == 1 && "reload".equals(args[0].toLowerCase())) {
            reload();
            return true;
        }

        //错误的参数显示帮助页面
        if (args.length < 4) {
            help();
            return true;
        }


        if (!Utils.matchWorld(args[2])) {
            sender.sendMessage(Utils.getMessage("configNoWorld"));
            return true;
        }

        //时间输入错误
        int time;
        try {
            time = Integer.parseInt(args[3]);
        } catch (Exception e) {
            sender.sendMessage(Utils.getMessage("inputError").replace("\\n", "\n"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "add":
                if (addTime(args[1], args[2], time, false)) {
                    sender.sendMessage(Utils.getMessage("addTime")
                            .replace("%world%", Utils.worldToAlias(args[2]))
                            .replace("%worldTime%", time + ""));
                }
                break;
            case "set":
                if (addTime(args[1], args[2], time, true)) {
                    sender.sendMessage(Utils.getMessage("setTime")
                            .replace("%world%", Utils.worldToAlias(args[2]))
                            .replace("%worldTime%", time + ""));
                }
                break;
            case "give":
                give(args[1], args[2], time);
                break;
            default:
                help();
                break;
        }


        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1:
                list.add("add");
                list.add("set");
                list.add("give");
                break;
            case 2:
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    list.add(onlinePlayer.getName());
                }
                break;
            case 3:
                list = PayWorld.plugins.getConfig().getStringList("World");
                break;
            case 4:
                list.add("60");
                list.add("120");
                list.add("240");
                break;
        }
        return list;
    }

    public void showTime() {

        Player player = (Player) sender;
        try {
            Map<String, Integer> timeMap = SqlUtils.getAll(player.getUniqueId());
            if (timeMap == null) {
                return;
            }

            sender.sendMessage(Utils.getMessage("listTitle"));
            String listText = Utils.getMessage("list");
            for (String key : timeMap.keySet()) {
                sender.sendMessage(listText
                        .replace("%world%", Utils.worldToAlias(key))
                        .replace("%worldTime%", timeMap.get(key) + ""));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addTime(String playerName, String world, int time, boolean set) {

        try {

            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

            //验证数据库字段
            SqlUtils.updateSqlField(world);
            //添加时长
            int i = SqlUtils.updateTime(player.getUniqueId(), world, time, set);
            if (i > 0) {
                //增加成功 提示消息
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;
    }

    public void give(String playerName, String world, int time) {

        try {

            //查找玩家
            Player player = Bukkit.getServer().getPlayerExact(playerName);
            if (player == null) {
                //玩家不存在
                sender.sendMessage(Utils.getMessage("offline"));
                return;
            }

            //创建时长卡
            ItemStack itemStack = Utils.createCard(world, time);
            HashMap<Integer, ItemStack> inv = player.getInventory().addItem(itemStack);
            if (inv.size() != 0) {
                //玩家背包已满
                sender.sendMessage(Utils.getMessage("backpack"));
                return;
            }
            sender.sendMessage(Utils.getMessage("giveCrad")
                    .replace("%world%", Utils.worldToAlias(world))
                    .replace("%worldTime%", time + ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void help() {
        sender.sendMessage("- §a[PayWorld] §e帮助-------------------#");
        sender.sendMessage("- §b/payworld");
        sender.sendMessage("- §e显示所有世界的剩余时长");
        sender.sendMessage("- §b/payworld <Add/Set/Give> <PlayerName> <WorldName> <Time>");
        sender.sendMessage("- §e对玩家进行 添加/修改/卡片 操作");
        sender.sendMessage("- §e#------------------------------------#");
        sender.sendMessage("- §a命令简写模式: /pw");

    }

    public void reload() {
        PayWorld.plugins.reloadConfig();
        sender.sendMessage(Utils.getMessage("reload"));
    }


}


