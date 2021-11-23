package me.osoloturk.personalmine.gui_infrastructure;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickAction {
	
	void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event);

}
