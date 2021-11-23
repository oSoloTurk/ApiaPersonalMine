package me.osoloturk.personalmine.gui_infrastructure;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {
    private CustomInventory customInventory;
    
	public CustomInventoryHolder(CustomInventory customInventory) {
		this.customInventory = customInventory;
	}
	
	public CustomInventory getCustomInventory() {
		return customInventory;
	}
    
	protected void setCustomInventory(CustomInventory customInventory) {
        this.customInventory = customInventory;
    }
	
    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, 9);
    }
    
}