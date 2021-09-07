package xyz.lvsheng.payworld.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lvsheng.payworld.PayWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        //取物品类型
        String itemName =
                Objects.requireNonNull(PayWorld.plugins.getConfig().getString("Card.item")).toUpperCase(Locale.ROOT);

        ItemStack itemStack = new ItemStack
                (Material.getMaterial(itemName) == null ? Material.PAPER : Material.getMaterial(itemName));

        //设置物品名
        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName(Utils.ColorMessage(PayWorld.plugins.getConfig().getString("Card.name")));

        //设置lore
        List<String> lore = new ArrayList<>();
        for (String s : PayWorld.plugins.getConfig().getStringList("Card.lore")) {
            lore.add(Utils.ColorMessage(s.replace("%world%", worldName).replace("%worldTime%", time + "")));
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
