package me.osoloturk.personalmine.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.gui_infrastructure.ClickAction;
import me.osoloturk.personalmine.gui_infrastructure.CustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.InventoryIcon;
import me.osoloturk.personalmine.gui_infrastructure.SimpleCustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.Size;
import me.osoloturk.personalmine.utils.Utils;

public class YesNoSelector extends SimpleCustomInventory {
	
	private final String titleExample;
	private final String path;
	private final APM instance;
	private final ClickAction closeAction;
	
	public YesNoSelector(APM instance, String identifier) {
		super(instance);
		this.instance = instance;
		this.path = identifier;
		closeAction = new ClickAction() {
			@Override
			public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
				instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
			}
		};
		titleExample = Utils.color(instance.getConfig().getString(identifier + ".title"));
		setSize(Size.fit(instance.getConfig().getInt(identifier + ".rows")));
		if(instance.getConfig().isSet(path + ".optional-content")) {
			for(String key : instance.getConfig().getConfigurationSection(path + ".optional-content").getKeys(false)) {
				String spesificPath = path + ".optional-content." + key;
				InventoryIcon icon = new InventoryIcon(instance.getGuiManager().createItem(spesificPath));
				instance.getGuiManager().calculateSlots(spesificPath + ".slots").forEach(slot -> setIcon(slot, icon, false));
			}
		}
		if(instance.getConfig().isSet(path + ".compulsory-content")) {
			for(String key : instance.getConfig().getConfigurationSection(path + ".compulsory-content").getKeys(false)) {
				String spesificPath = path + ".compulsory-content." + key;
				setIcon(instance.getConfig().getInt(spesificPath + ".slot"), new InventoryIcon(instance.getGuiManager().createItem(spesificPath)), true);
			}
		}
	}
	
	public void openSelector(Player player, String title, List<String> question, ClickAction noButton, ClickAction yesButton) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			getIcon(instance.getConfig().getInt(path + ".compulsory-content.no-button.slot")).setClickAction(noButton == null ? closeAction : noButton);
			getIcon(instance.getConfig().getInt(path + ".compulsory-content.yes-button.slot")).setClickAction(yesButton == null ? closeAction : yesButton);
			setTitle(title == null ? titleExample : title);
			List<String> lore = Utils.color(instance.getConfig().getStringList(path + ".compulsory-content.info.block.lore"));
			InventoryIcon infoIcon = getIcon(instance.getConfig().getInt(path + ".compulsory-content.info.slot"));
			for(int line = 0; line < lore.size(); line++) {
				if(lore.get(line).contains("%question%")) {
					lore.remove(line);
					lore.addAll(line, Utils.color(question));
					break;
				}
			}
			infoIcon.setLore(lore);
			instance.getServer().getScheduler().runTask(instance, () -> openInventory(player));
		});
	}
	
}
