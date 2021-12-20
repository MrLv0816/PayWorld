package xyz.lvsheng.payworld.event;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
public class Listener implements org.bukkit.event.Listener {
    public static Map<UUID, Boolean> skipList = new HashMap<>();

    //加入游戏检查世界时长
    @EventHandler
    public void jionServer(PlayerJoinEvent event) {

        //将玩家添加进入数据库 以便后续进行修改操作
        try {
            SqlUtils.addPlayer(event.getPlayer().getUniqueId());
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //判断玩家是否处于 (付费) 世界
        if (Utils.matchWorld(event.getPlayer().getWorld().getName())) {
            return;
        }

        try {

            //查询玩家剩余时长
            int time = SqlUtils.getTime(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());

            //玩家有足够时长
            if (time > 0) {
                skipList.put(event.getPlayer().getUniqueId(), true);
                return;
            }

            //时长不足,将玩家送回默认世界
            event.getPlayer().teleport(Utils.backWorld().getSpawnLocation());
            //发送消息
            event.getPlayer().sendMessage
                    (Objects.requireNonNull(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Messages.timeOut"))));

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    //传送时检查时长
    @EventHandler
    public void tp(PlayerTeleportEvent event) {
        //判断玩家是否要传送至 (付费) 世界
        String toWolrd = event.getTo().getWorld().getName();
        if (!Utils.matchWorld(toWolrd)) {
            //不是 (付费) 世界忽略事件
            return;
        }

        //验证时长
        try {

            int time = SqlUtils.getTime(event.getPlayer().getUniqueId(), toWolrd);

            if (time > 0) {
                skipList.put(event.getPlayer().getUniqueId(), true);
                return;
            }

            //时长不足,撤销传送
            event.setCancelled(true);
            //发送消息
            event.getPlayer().sendMessage(Utils.getMessage("lackTime"));


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    //使用时长卡
    @EventHandler
    public void userCard(PlayerInteractEvent event) throws Exception {

        //未拿物品,忽略操作
        if (event.getItem() == null) {
            return;
        }

        //不是右键,忽略操作
        Action action = event.getAction();
        if (action.compareTo(Action.RIGHT_CLICK_AIR) != 0 && action.compareTo(Action.RIGHT_CLICK_BLOCK) != 0) {
            return;
        }

        //不是兑换卡的 Material,忽略操作
        String item = PayWorld.plugins.getConfig().getString("Card.item");
        if (event.getMaterial() != Material.getMaterial(item)) {
            return;
        }

        //名称不一致,忽略操作
        String itemName = PayWorld.plugins.getConfig().getString("Card.name").replaceAll("&", "");
        String itemDisplayName = event.getItem().getItemMeta().getDisplayName().replace("§", "");

        if (!itemName.equals(itemDisplayName)) {
            return;
        }

        String world = null;
        int time = 0;
        try {
            ItemStack im = event.getItem();
            world = Utils.getNbt(im, "world");
            String value = Utils.getNbt(im, "time");
            //卡片读取异常
            if (value == null || world == null) {
                throw new NullPointerException("读取卡片信息异常,卡片nbt为空");
            }
            time = Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
            event.getPlayer().sendMessage(Utils.getMessage("cardValueErr"));
            return;
        }

        //添加时间
        SqlUtils.updateSqlField(Utils.aliasToWorld(world));
        SqlUtils.updateTime(event.getPlayer().getUniqueId(), Utils.aliasToWorld(world), time, false);

        event.getPlayer().sendMessage(Utils.getMessage("addTime")
                .replace("%world%", Utils.worldToAlias(world))
                .replace("%worldTime%", time + ""));

        //减少物品
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }


}
