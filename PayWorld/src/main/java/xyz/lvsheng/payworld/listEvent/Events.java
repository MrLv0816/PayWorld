package xyz.lvsheng.payworld.listEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lvsheng.payworld.Commands.pw;
import xyz.lvsheng.payworld.Main;
import xyz.lvsheng.payworld.Util.SQLite;
import xyz.lvsheng.payworld.Util.Utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author MrLv
 * @date 2021/9/4
 * @apiNote
 */
public class Events implements Listener {
    public static HashMap<UUID, Boolean> playerMap = new HashMap<>();

    //加入游戏检查世界时长
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        try {
            for (String world : Main.plugins.getConfig().getStringList("World")) {
                int time = SQLite.select(Main.sql, event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());
                if (world.equalsIgnoreCase(event.getPlayer().getWorld().getName()) && time <= 0) {
                    event.getPlayer().teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
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
            List<String> world = Main.plugins.getConfig().getStringList("World");

            for (String s : world) {
                if (Objects.equals(s.toLowerCase(), Objects.requireNonNull(Objects.requireNonNull(event.getTo()).getWorld()).getName().toLowerCase())) {
                    if (SQLite.select(Main.sql, event.getPlayer().getUniqueId(), s) <= 0) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Utils.ColorMessage(Main.plugins.getConfig().getString("Messages.lackTime")));
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
        if (event.hasItem() && ("RIGHT_CLICK_BLOCK".equalsIgnoreCase(action) || "RIGHT_CLICK_AIR".equals(action)) && "§e§l时长兑换卡".equals(event.getItem().getItemMeta().getDisplayName())) {

            ItemMeta itemMeta = event.getItem().getItemMeta();
            if (!Objects.equals(itemMeta, null)) {
                List<String> lore = itemMeta.getLore();
                if (!Objects.equals(lore, null) && Objects.equals(lore.size(), 2)) {
                    String worldName = lore.get(0).split(": ")[1];
                    String time = lore.get(1).split(": ")[1].split(" ")[0];
                    pw.add(event.getPlayer(), event.getPlayer().getName(), worldName, Integer.parseInt(time));
                    event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }
        }
    }


}
