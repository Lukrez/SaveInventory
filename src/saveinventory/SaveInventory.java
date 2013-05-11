package saveinventory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class SaveInventory extends JavaPlugin implements Listener {

	private File filePlayerData;
	private File filePlayerfolder;
	private String inventoryOwner, admin;
	private String[] inventoryNames;
	private int invPointer;

	@Override
	public void onDisable() {
		this.getLogger().info("disabled.");
	}

	@Override
	public void onEnable() {
		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		this.filePlayerData = new File(this.getDataFolder(), "playerdata");
		if (!this.filePlayerData.exists()) {
			this.filePlayerData.mkdir();
		}
		
		this.getServer().getPluginManager().registerEvents(this,this);

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				saveInventories();
				Bukkit.getServer().broadcastMessage("Inventories saved.");
			}
		}, 60, 1200);
		
		this.invPointer = -1;

		this.getLogger().info("enabled.");
	}

	public void saveItemStackArray(Player player, String dateTime) throws FileNotFoundException, IOException {
		File filePlayerFolder = new File(this.filePlayerData, player.getName());
		if (!filePlayerFolder.exists()) {
			filePlayerFolder.mkdir();
		}

		File fileInv = new File(filePlayerFolder, dateTime + ".inv");
		BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileInv)));

		YamlConfiguration yamlInventory = new YamlConfiguration();
		yamlInventory.set("inventory", ItemParser.getHashMapFromItemStackArray(player.getInventory().getContents()));
		yamlInventory.set("armor", ItemParser.getHashMapFromItemStackArray(player.getInventory().getArmorContents()));

		out.write(yamlInventory.saveToString().getBytes());
		out.flush();
		out.close();
	}

	/*public ItemStack[] getItemStackArray(String playerName) throws IOException {
		// FileInputStream in = new FileInputStream(f);
		// GZIPInputStream zipin = new GZIPInputStream(in);

		// byte[] buffer = new byte[sChunk];
	}*/

	public void saveInventories() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		Date date = new Date();

		for (Player player : this.getServer().getOnlinePlayers()) {
			try {
				this.saveItemStackArray(player, dateFormat.format(date));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only a player can use that command.");
			return true;
		}

		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("show")) {
			if (args.length == 0) {
				player.sendMessage("/show inv <player name>");
				return true;
			}

			if (args[0].equalsIgnoreCase("inv")) {
				this.admin = player.getName();
				this.inventoryOwner = args[1];

				this.filePlayerfolder = new File(this.filePlayerData, args[1]);
				if (!this.filePlayerfolder.exists())
					return true;

				this.inventoryNames = this.sortInventoryNames(filePlayerfolder.list());
				this.invPointer = 0;
				Inventory inv = this.loadInventory();
				if (inv == null)
					return true;
				player.openInventory(inv);
				return true;
			}
		}

		return false;
	}
	
	public Inventory loadInventory(){
		if (this.invPointer < 0)
			return null;
		File x = new File(this.filePlayerfolder, this.inventoryNames[this.invPointer]);
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
			ItemStack[] inventory = ItemParser.getItemStackArrayFromHashMap(yml.getConfigurationSection("inventory"), 36);
			ItemStack[] armor = ItemParser.getItemStackArrayFromHashMap(yml.getConfigurationSection("armor"), 4);
			
			Inventory inv = Bukkit.createInventory(null, 54, this.inventoryOwner);
			// set armor
			for (int i = 0;i<4;i++){
				inv.setItem(i, armor[i]);
			}
			
			// set inventory
			for (int i = 0;i<9;i++){
				inv.setItem(i+45, inventory[i]);
			}
			
			for (int i = 9;i<36;i++){
				inv.setItem(i+9, inventory[i]);
			}
			
			// set water as left klick
			if (this.invPointer > 0){
				ItemStack itemWater= new ItemStack(Material.WATER,1);
				ItemMeta imWater = itemWater.getItemMeta();
				imWater.setDisplayName(this.inventoryNames[this.invPointer-1]);
				itemWater.setItemMeta(imWater);
				inv.setItem(6, itemWater);
			}
			// set book
			ItemStack itemBook = new ItemStack(Material.BOOK,1);
			ItemMeta imBook = itemBook.getItemMeta();
			imBook.setDisplayName(this.inventoryNames[this.invPointer]);
			itemBook.setItemMeta(imBook);
			inv.setItem(7, itemBook);
			
			// set lava as right klick
			if (this.invPointer < this.inventoryNames.length-1){
				ItemStack itemLava= new ItemStack(Material.LAVA,1);
				ItemMeta imLava = itemLava.getItemMeta();
				imLava.setDisplayName(this.inventoryNames[this.invPointer+1]);
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
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getPlayer();
		if (!player.getName().equalsIgnoreCase(this.admin)) {
			return;
		}
		this.admin = "";
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (!player.getName().equalsIgnoreCase(this.admin)) {
			return;
		}
		
		event.setCancelled(true);
		
		// check if klicked on slotnr 7 or 8
		if (event.getSlot() == 6 && this.invPointer > 0){
			this.invPointer--;
		} else if (event.getSlot() == 8 && this.invPointer < this.inventoryNames.length-1){
			this.invPointer++;
		} else {
			return;
		}
		
		Inventory inv = this.loadInventory();
		if (inv == null){
			return;
		}
		event.getInventory().setContents(inv.getContents());	
	}

	private int[] toIntArray(String[] x) {
		int[] y = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		return y;
	}

	private boolean isNewerInventory(String a, String b) {
		
		int[] spA = toIntArray(a.replace(".inv","").split("_"));
		if (spA.length != 5) {
			return true;
		}
		int[] spB = toIntArray(b.replace(".inv","").split("_"));
		if (spB.length != 5) {
			return false;
		}
		for (int i = 0; i < 5; i++) {
			if (spA[i] > spB[i]) {
				return true;
			} else if (spA[i] < spB[i]){
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
}
