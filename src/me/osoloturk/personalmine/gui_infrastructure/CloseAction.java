package me.osoloturk.personalmine.gui_infrastructure;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface CloseAction {
	
	void handle(CustomInventory customInventory, Player player, InventoryCloseEvent event);
	
}
