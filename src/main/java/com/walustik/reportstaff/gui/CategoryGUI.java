package com.walustik.reportstaff.gui;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class CategoryGUI {

    public static void open(Player viewer, Player target) {
        String title = ColorUtils.colorize("&8Report &7" + target.getName());
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        addCategoryItem(inventory, 10, Material.DIAMOND_SWORD, "Combat", "combat", viewer, target);
        addCategoryItem(inventory, 12, Material.FEATHER, "Movement", "movement", viewer, target);
        addCategoryItem(inventory, 14, Material.PAPER, "Chat", "chat", viewer, target);
        addCategoryItem(inventory, 16, Material.BEDROCK, "Bug", "bug", viewer, target);
        addCategoryItem(inventory, 22, Material.WRITABLE_BOOK, "Custom Reason", "custom", viewer, target);

        viewer.openInventory(inventory);
    }

    private static void addCategoryItem(Inventory inventory, int slot, Material material, String displayName, String action, Player viewer, Player target) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setDisplayName(ColorUtils.colorize("&a" + displayName));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize("&7Select this reason for reporting"));
        lore.add(ColorUtils.colorize("&f" + target.getName()));
        meta.setLore(lore);

        NamespacedKey actionKey = ReportStaffPlugin.getInstance().getActionKey();
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
}
