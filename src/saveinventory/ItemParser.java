package saveinventory;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemParser {

	public static ItemStack[] getItemStackArrayFromHashMap(ConfigurationSection cs, int size) {
		ItemStack[] items = new ItemStack[size];

		for (String key : cs.getKeys(false)) {
			int slot = Integer.parseInt(key);
			items[slot] = (ItemStack) cs.get(key);
		}
		return items;
	}

	public static HashMap<String, Object> getHashMapFromItemStackArray(ItemStack[] items) {
		HashMap<String, Object> list = new HashMap<String, Object>();

		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getType() != Material.AIR)
				list.put("" + i, items[i]);
		}
		return list;
	}
}
