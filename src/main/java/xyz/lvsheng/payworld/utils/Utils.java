package xyz.lvsheng.payworld.utils;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lvsheng.payworld.PayWorld;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author MrLv
 * @date 2021/9/5
 * @apiNote
 */
public class Utils {
    public static final String VERSION;

    static {
        VERSION = getVersion();
    }

    /**
     * 获取服务器版本号
     */
    private static String getVersion() {
        String[] split = Bukkit.getBukkitVersion().split("\\.");
        String version = "v" + split[0] + "_" + split[1].replaceAll("-R.*", "") + "_R";

        for (int i = 1; i < 10; i++) {
            try {
                Class.forName("org.bukkit.craftbukkit." + version + i + ".inventory.CraftItemStack");
                return version + i;
            } catch (Exception ignored) {
                //忽略异常
            }
        }
        return null;
    }

    /**
     * 别名转世界名
     *
     * @param alias 别名
     * @return 世界名
     */
    public static String aliasToWorld(String alias) {

        List<String> aliasWorld = PayWorld.plugins.getConfig().getStringList("WorldAlias");
        for (String aliasStr : aliasWorld) {
            String[] split = aliasStr.split(":");

            if (split[1].replaceAll("&","")
                    .equalsIgnoreCase(alias.replaceAll("§",""))) {
                return split[0];
            }
        }


        return alias;
    }

    /**
     * 世界名转别名
     *
     * @param world 世界名
     * @return 别名
     */
    public static String worldToAlias(String world) {
        List<String> aliasWorld = PayWorld.plugins.getConfig().getStringList("WorldAlias");
        for (String aliasStr : aliasWorld) {
            String[] split = aliasStr.split(":");
            if (split[0].equalsIgnoreCase(world)) {
                return split[1].replaceAll("&","§");
            }
        }


        return world;
    }

    /**
     * 给item添加NBT
     * @param im    物品
     * @param tagMap    NBT
     * @return 带 NBT 标签的物品
     */
    public static ItemStack setNbt(ItemStack im, Map<String, String> tagMap) {

        try {

            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + VERSION + ".inventory.CraftItemStack");
            Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Object NMSItem = asNMSCopy.invoke(null, im);

            // 相当于 CraftItemStack.asNMSCopy(im);

            Class<?> itemStack = Class.forName("net.minecraft.server." + VERSION + ".ItemStack");
            Class<?> nbtTagCompound = Class.forName("net.minecraft.server." + VERSION + ".NBTTagCompound");

            Method getTag = itemStack.getMethod("getTag");
            Object nbt = getTag.invoke(NMSItem);
            // 相当于 NBTTagCompound nbt = imNMS.getTag();
            //nbt中包含了 Meta 信息,所以要获取
            if (nbt == null) {
                Constructor<?> createNewTag = nbtTagCompound.getConstructor();
                nbt = createNewTag.newInstance();
                // 相当于 nbt = new NBTTagCompound();
            }


            Class<?> nbtTagString = Class.forName("net.minecraft.server." + VERSION + ".NBTTagString");
            Class<?> nbtBase = Class.forName("net.minecraft.server." + VERSION + ".NBTBase");
            Method setNBTString = nbtTagCompound.getMethod("set", String.class, nbtBase);

            for (String key : tagMap.keySet()) {
                Object stringValue = nbtTagString.getConstructor(String.class).newInstance(tagMap.get(key));
                setNBTString.invoke(nbt, key, stringValue);
            }


            // 相当于 nbt.set("someValue", NBTTagString.create("This is a string."));

            Method setTag = itemStack.getMethod("setTag", nbtTagCompound);
            setTag.invoke(NMSItem, nbt);

            // 相当于 imNMS.setTag(nbt);

            Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", itemStack);
            im = (ItemStack) asBukkitCopy.invoke(null, NMSItem);

            return im;

            // 相当于 im = CraftItemStack.asBukkitCopy(imNMS);
            // 主要处理到此结束，以下为异常捕获
        } catch (Exception e) {
            e.printStackTrace();
            // 出错我们也没办法，只能输出了
        }
        return null;
    }

    /**
     * 获取 item 的 nbt 标签
     * @param im    物品
     * @param key   nbt key
     * @return  value
     */
    public static String getNbt(ItemStack im, String key) {

        try {

            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + VERSION + ".inventory.CraftItemStack");
            Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Object NMSItem = asNMSCopy.invoke(null, im);

            // 相当于 CraftItemStack.asNMSCopy(im);

            Class<?> itemStack = Class.forName("net.minecraft.server." + VERSION + ".ItemStack");
            Class<?> nbtTagCompound = Class.forName("net.minecraft.server." + VERSION + ".NBTTagCompound");

            Method getTag = itemStack.getMethod("getTag");
            Object nbt = getTag.invoke(NMSItem);

            if (nbt == null) {
                return null;
            }

            Method getString = nbtTagCompound.getMethod("getString", String.class);
            return (String) getString.invoke(nbt, key);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 创建时长卡
     *
     * @param worldName 世界名
     * @param time      时间
     * @return 卡
     */
    public static ItemStack createCard(String worldName, int time) {

        //取物品类型
        String itemName =
                Objects.requireNonNull(PayWorld.plugins.getConfig().getString("Card.item")).toUpperCase(Locale.ROOT);

        ItemStack itemStack = new ItemStack
                (Material.getMaterial(itemName) == null ? Material.PAPER : Material.getMaterial(itemName));

        //设置物品名
        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName(Utils.colorMessage(PayWorld.plugins.getConfig().getString("Card.name")));


        //设置lore
        List<String> lore = new ArrayList<>();
        List<String> loreConfig = PayWorld.plugins.getConfig().getStringList("Card.lore");
        for (String s : loreConfig) {
            lore.add(Utils.colorMessage(s.replaceAll
                            ("%world%", worldToAlias(worldName)).
                    replaceAll("%worldTime%", time + "")));
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("world", worldName);
        tagMap.put("time", String.valueOf(time));
        return setNbt(itemStack, tagMap);
    }

    /**
     * 世界是否存在于配置文件中
     *
     * @param worldName 世界名称
     * @return 真假
     */
    public static boolean matchWorld(String worldName) {

        if (worldName == null) {
            return false;
        }

        List<String> world = PayWorld.plugins.getConfig().getStringList("World");
        for (String w : world) {
            if (w.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;


    }

    /**
     * 判断世界是否存在
     *
     * @return 如果存在则返回世界, 不存在则返回 主世界
     */
    public static World backWorld() {
        World backWorld = Bukkit.getWorld(PayWorld.plugins.getConfig().getString("BackWorld"));
        return backWorld == null ? Bukkit.getWorld("world") : backWorld;
    }

    /**
     * 彩色文字
     *
     * @param message 文本
     * @return 文本
     */
    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 获取 message 字段
     *
     * @param field 字段
     * @return 文本
     */
    public static String getMessage(String field) {
        return colorMessage(PayWorld.plugins.getConfig().getString("Messages." + field));
    }


}
