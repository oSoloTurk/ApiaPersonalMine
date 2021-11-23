package me.osoloturk.personalmine.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.gui_infrastructure.InventoryIcon;

import static me.osoloturk.personalmine.utils.Utils.color;
import static me.osoloturk.personalmine.utils.Utils.getInt;
import static me.osoloturk.personalmine.utils.Utils.getNmsVersion;

public class GuiManager {
	private final APM instance;
	private int bukkitUnbreakableSupport = -1;
	private Method spigotMethod, setUnbreakableMethod;
	private YesNoSelector yesNoSelector;
	private Map<UniversalContents, Pair<InventoryIcon, InventoryIcon>> universalIcons;
	private Field profileField;
	private AdminPage adminPage;
	private UserPage userPage;
	private ChancePackBuyPage chancePackBuyPage;
	
	public GuiManager(APM instance) {
		this.instance = instance;
		init();
	}
	
	public void init() {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			yesNoSelector = new YesNoSelector(instance, "gui.yes-no-gui");
			universalIcons = new HashMap<>();
			for(UniversalContents content : UniversalContents.values())
				universalIcons.put(content,
						Pair.of(new InventoryIcon(createItem("gui.universal-contents." + content.getPath())), new InventoryIcon(createItem("gui.universal-contents." + content.getPath() + ".trace"))));
			adminPage = new AdminPage(instance);
			userPage = new UserPage(instance);
			chancePackBuyPage = new ChancePackBuyPage(instance);
		});
	}
	
	public void openAdminDashBoard(Player player) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			adminPage.openAdminPage(player, 1);
		});
	}
	
	public void openUserDashBoard(Player player) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			userPage.openUserPage(player, 1);
		});
	}
	
	public void openChancePackBuyPage(Player player) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			chancePackBuyPage.openChancePackBuy(player, 1);
		});
	}
	
	public List<Integer> calculateSlots(String path) {
		List<Integer> returnList = new ArrayList<>();
		String configValue = instance.getConfig().getString(path).replaceAll("\\s+", "");
		int slot = 0;
		String[] slots = configValue.split(",");
		for(String slotValue : slots) {
			if(!slotValue.contains("-"))
				returnList.add(getInt(slotValue, 0));
			else {
				int numberOne = getInt(slotValue.split("-")[0], 0), numberTwo = getInt(slotValue.split("-")[1], 0);
				int direction = numberOne - numberTwo < 0 ? 1 : -1;
				slot = numberOne;
				do {
					returnList.add(slot);
					slot += direction;
				} while(slot != (numberOne - numberTwo > 0 ? numberOne : numberTwo));
			}
		}
		return returnList;
	}
	
	public ItemStack createItem(String path) {
		return createItem(Material.valueOf(instance.getConfig().getString(path + ".block.material")), instance.getConfig().getInt(path + ".block.amount", 1),
				(short) instance.getConfig().getInt(path + ".block.data_value"), color(instance.getConfig().getString(path + ".block.name")),
				color(instance.getConfig().getStringList(path + ".block.lore")), instance.getConfig().getStringList(path + ".block.enchants"),
				instance.getConfig().getStringList(path + ".block.flags"), instance.getConfig().getBoolean(path + ".block.unbreakable"), instance.getConfig().getString(path + ".block.skull_owner"));
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack createItem(Material material, int amount, short data, String name, List<String> lore, List<String> enchants, List<String> flags, boolean unbreakable, String owner) {
		boolean skull = false;
		ItemStack item = new ItemStack(material, amount);
		if(material.name().equals("SKULL_ITEM") || material.name().equals("PLAYER_HEAD"))
			skull = true;
		if(data != 0)
			item.setDurability(data);
		ItemMeta itemMeta = item.getItemMeta();
		if(unbreakable) {
			try {
				if(bukkitUnbreakableSupport == -1) {
					try {
						ItemMeta.class.getDeclaredMethod("setUnbreakable", boolean.class);
						bukkitUnbreakableSupport = 1;
					} catch(NoSuchMethodException | SecurityException ex) {
						bukkitUnbreakableSupport = 0;
					}
				}
				if(bukkitUnbreakableSupport == 1) {
					itemMeta.setUnbreakable(unbreakable);
				} else {
					if(spigotMethod == null) {
						spigotMethod = itemMeta.getClass().getDeclaredMethod("spigot");
						spigotMethod.setAccessible(true);
					}
					Object itemStackSpigot = spigotMethod.invoke(itemMeta);
					if(setUnbreakableMethod == null) {
						setUnbreakableMethod = itemStackSpigot.getClass().getDeclaredMethod("setUnbreakable", Boolean.TYPE);
						setUnbreakableMethod.setAccessible(true);
					}
					setUnbreakableMethod.invoke(itemStackSpigot, unbreakable);
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		if(name != null)
			itemMeta.setDisplayName(name);
		if(lore != null)
			itemMeta.setLore(lore);
		if(flags != null && !flags.isEmpty()) {
			for(String flag : flags) {
				ItemFlag itemFlag = ItemFlag.valueOf(flag);
				if(itemFlag != null)
					itemMeta.addItemFlags(itemFlag);
			}
		}
		item.setItemMeta(itemMeta);
		if(owner != null && !owner.isEmpty() && skull) {
			SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
			if(owner.contains("custom-")) {
				try {
					owner = owner.replace("custom-", "");
					GameProfile profile = new GameProfile(UUID.randomUUID(), null);
					profile.getProperties().put("textures", new Property("textures", owner));
					if(profileField == null) {
						profileField = skullMeta.getClass().getDeclaredField("profile");
						profileField.setAccessible(true);
					}
					profileField.set(skullMeta, profile);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				if(getNmsVersion().startsWith("v1_8_"))
					skullMeta.setOwner(owner);
				else {
					OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
					skullMeta.setOwningPlayer(player);
				}
			}
			item.setItemMeta(skullMeta);
		}
		if(enchants != null && !enchants.isEmpty()) {
			for(String string : enchants) {
				String[] split = string.split(";");
				Enchantment enchant = Enchantment.getByName(split[0]);
				if(enchant != null)
					item.addUnsafeEnchantment(enchant, Integer.valueOf(split[1]));
			}
		}
		return item;
	}
	
	public YesNoSelector getYesNoSelector() {
		return yesNoSelector;
	}
	
	public Pair<InventoryIcon, InventoryIcon> getUniverselContent(UniversalContents content) {
		return universalIcons.get(content);
	}
	
	public enum UniversalContents {
		BACK_BUTTON_WITH_PREVIOUS_PAGE("back-button-with-previous-page"),
		BACK_BUTTON_WITHOUT_PREVIOUS_PAGE("back-button-without-previous-page"),
		BACK_BUTTON_ONLY_PREVIOUS_PAGE("back-button-only-previous-page"),
		NEXT_BUTTON("next-button"),
		CHANCE_PACK_WITH_COSTS("chance-pack-with-costs"),
		CHANCE_PACK_WITHOUT_COSTS("chance-pack-without-costs"),
		RENEVAL_DATE("renewal-date"),
		ELEMENT_NOT_FOUND("element-not-found");
		
		final String path;
		
		private UniversalContents(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
	}
}
