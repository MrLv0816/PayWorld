package xyz.lvsheng.payworld.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author MrLv
 * @date 2021/9/5
 * @apiNote
 */
public class Utils {

    public static World searchWorld(String worldName) {
        for (World world : Bukkit.getWorlds()) {
            if (worldName.equals(world.getName())) {
                return world;
            }
        }
        return Bukkit.getWorld("world");
    }

    public static String ColorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean equals(List<String> list, String str) {
        for (String s : list) {
            if (Objects.equals(s.toLowerCase(), str.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack createCard(String worldName, int time) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§e§l时长兑换卡");
        List<String> meatList = new ArrayList<>();
        meatList.add("§r- §a§l世界: " + worldName);
        meatList.add("§r- §b§l时长: " + time + " 分钟");
        itemMeta.setLore(meatList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
