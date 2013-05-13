package de.markus.saveinventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInfo {

	private File playerfolder;
	private String inventoryOwner, admin;
	private String[] inventoryNames;
	private int invPointer;

	private ItemStack[] lastArmor;
	private ItemStack[] lastInventory;

	public PlayerInfo(String admin, String inventoryOwner) {

		this.playerfolder = new File(SaveInventory.getInstance().getPlayerDataFolder(), inventoryOwner);
		if (!this.playerfolder.exists()) {
			Bukkit.getServer().getPlayer(admin).sendMessage("Von diesem Spieler ist kein Inventar gespeichert.");
			return;
		}
		this.admin = admin;
		this.inventoryOwner = inventoryOwner;
		this.inventoryNames = this.sortInventoryNames(this.playerfolder.list());
		this.invPointer = 0;

	}

	public File getPlayerFolder() {
		return this.playerfolder;
	}

	public String getAdmin() {
		return this.admin;
	}

	public String getInventoryOwner() {
		return this.inventoryOwner;
	}

	public ItemStack[] getLastArmor() {
		return this.lastArmor;
	}

	public ItemStack[] getLastInventory() {
		return this.lastInventory;
	}

	private int[] toIntArray(String[] x) {
		int[] y = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		return y;
	}

	private boolean isNewerInventory(String a, String b) {

		int[] spA = toIntArray(a.replace(".inv", "").split("_"));
		if (spA.length != 5) {
			return true;
		}
		int[] spB = toIntArray(b.replace(".inv", "").split("_"));
		if (spB.length != 5) {
			return false;
		}
		for (int i = 0; i < 5; i++) {
			if (spA[i] > spB[i]) {
				return true;
			} else if (spA[i] < spB[i]) {
				return false;
			}
		}
		System.out.println(a + " < " + b);
		return false;
	}

	private String[] sortInventoryNames(String[] names) {
		// Bubblesort - kann auch mit etwas optimalerem ersetzt werden

		boolean finnished = false;
		while (!finnished) {
			finnished = true;
			for (int i = 0; i < names.length - 1; i++) {
				if (!isNewerInventory(names[i], names[i + 1])) {
					String x = names[i];
					names[i] = names[i + 1];
					names[i + 1] = x;
					finnished = false;
				}
			}
		}
		return names;
	}

	public Inventory loadInventory() {
		if (this.invPointer < 0)
			return null;
		File x = new File(this.playerfolder, this.inventoryNames[this.invPointer]);
		if (!x.exists())
			return null;

		try {
			FileInputStream in = new FileInputStream(x);
			GZIPInputStream zipin = new GZIPInputStream(in);
			InputStreamReader reader = new InputStreamReader(zipin);
			BufferedReader br = new BufferedReader(reader);

			String readed;
			String inventoryString = "";
			while ((readed = br.readLine()) != null) {
				inventoryString += readed + System.getProperty("line.separator");
			}
			YamlConfiguration yml = new YamlConfiguration();
			yml.loadFromString(inventoryString);
			this.lastInventory = ItemParser.getItemStackArrayFromHashMap(yml.getConfigurationSection("inventory"), 36);
			this.lastArmor = ItemParser.getItemStackArrayFromHashMap(yml.getConfigurationSection("armor"), 4);

			Inventory inv = Bukkit.createInventory(null, 54, "SaveInventory: " + this.inventoryOwner);
			// set armor
			for (int i = 0; i < 4; i++) {
				inv.setItem(i, this.lastArmor[i]);
			}

			// set inventory
			for (int i = 0; i < 9; i++) {
				inv.setItem(i + 45, this.lastInventory[i]);
			}

			for (int i = 9; i < 36; i++) {
				inv.setItem(i + 9, this.lastInventory[i]);
			}

			// set water as left klick
			if (this.invPointer > 0) {
				ItemStack itemWater = new ItemStack(Material.WATER, 1);
				ItemMeta imWater = itemWater.getItemMeta();
				imWater.setDisplayName(this.inventoryNames[this.invPointer - 1]);
				itemWater.setItemMeta(imWater);
				inv.setItem(6, itemWater);
			}
			// set book
			ItemStack itemBook = new ItemStack(Material.BOOK, 1);
			ItemMeta imBook = itemBook.getItemMeta();
			imBook.setDisplayName(this.inventoryNames[this.invPointer]);
			itemBook.setItemMeta(imBook);
			inv.setItem(7, itemBook);

			// set lava as right klick
			if (this.invPointer < this.inventoryNames.length - 1) {
				ItemStack itemLava = new ItemStack(Material.LAVA, 1);
				ItemMeta imLava = itemLava.getItemMeta();
				imLava.setDisplayName(this.inventoryNames[this.invPointer + 1]);
				itemLava.setItemMeta(imLava);
				inv.setItem(8, itemLava);
			}

			return inv;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean hasNextInventory() {
		if (this.invPointer < this.inventoryNames.length - 1) {
			return true;
		}
		return false;
	}

	public boolean hasPreviousInventory() {
		if (this.invPointer > 0) {
			return true;
		}
		return false;
	}

	public void setNextInventory() {
		this.invPointer++;
	}

	public void setPreviousInventory() {
		this.invPointer--;
	}
}
