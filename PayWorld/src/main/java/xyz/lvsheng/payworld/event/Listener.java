package xyz.lvsheng.payworld.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lvsheng.payworld.PayWorld;
import xyz.lvsheng.payworld.commands.Pw;
import xyz.lvsheng.payworld.utils.SQLite;
import xyz.lvsheng.payworld.utils.Utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author MrLv
 * @date 2021/9/4
 * @apiNote
 */
public class Listener implements org.bukkit.event.Listener {
    public static HashMap<UUID, Boolean> playerMap = new HashMap<>();

    //加入游戏检查世界时长
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        try {
            for (String world : PayWorld.plugins.getConfig().getStringList("World")) {
                int time = SQLite.select(PayWorld.sql, event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());
                if (world.equalsIgnoreCase(event.getPlayer().getWorld().getName()) && time <= 0) {
                    event.getPlayer().teleport(Utils.searchWorld(PayWorld.plugins.getConfig().getString("BackWorld")).getSpawnLocation());
                    return;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    //传送时检查时长
    @EventHandler
    public void playerTp(PlayerTeleportEvent event) {
        try {
            List<String> world = PayWorld.plugins.getConfig().getStringList("World");

            for (String s : world) {
                if (Objects.equals(s.toLowerCase(), Objects.requireNonNull(Objects.requireNonNull(event.getTo()).getWorld()).getName().toLowerCase())) {
                    if (SQLite.select(PayWorld.sql, event.getPlayer().getUniqueId(), s) <= 0) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Utils.ColorMessage(PayWorld.plugins.getConfig().getString("Messages.lackTime")));
                        break;
                    } else {
                        playerMap.put(event.getPlayer().getUniqueId(), true);
                    }
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    //使用时长卡
    @EventHandler
    public void playerUse(PlayerInteractEvent event) {
        String action = event.getAction().name();
        if (event.hasItem() && ("RIGHT_CLICK_BLOCK".equalsIgnoreCase(action) || "RIGHT_CLICK_AIR".equals(action))
                && Objects.equals(event.getItem().getItemMeta().getDisplayName(),
                PayWorld.plugins.getConfig().getString("Card.name").replaceAll("&", "§"))) {


            //卡片正则
            Pattern world = null;
            Pattern time = null;
            List<String> lorelist = PayWorld.plugins.getConfig().getStringList("Card.lore");
            for (String s : lorelist) {
                if (s.contains("%world%")) {
                    world = Pattern.compile(s.replaceAll("&", "§").replace("%world%", "(.*)"));
                }
                if (s.contains("%worldTime%")) {
                    time = Pattern.compile(s.replaceAll("&", "§").replace("%worldTime%", "(.*)"));
                }
            }
            if (world == null || time == null) {
                Bukkit.getConsoleSender().sendMessage("§a[PayWorld] §c卡片配置错误,无法正常识别 %world% 或 %worldTime%");
                return;
            }


            //匹配卡片文字,取出世界名与增加时间
            String addName = "";
            String addTime = "";
            ItemMeta itemMeta = event.getItem().getItemMeta();
            if (!Objects.equals(itemMeta, null) && !Objects.equals(itemMeta.getLore(), null)) {
                for (String lore : itemMeta.getLore()) {
                    Matcher w = world.matcher(lore);
                    if (w.find()) {
                        addName = w.group(1);
                    }
                    Matcher t = time.matcher(lore);
                    if (t.find()) {
                        addTime = t.group(1);
                    }
                }
            }


            //给予玩家世界 清空手持物品
            if (!"".equals(addName) && !"".equals(addTime)) {
                Pw.add(event.getPlayer(), event.getPlayer().getName(), addName, Integer.parseInt(addTime));
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    event.getPlayer().getInventory().setItemInMainHand(item);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }
        }
    }


}
