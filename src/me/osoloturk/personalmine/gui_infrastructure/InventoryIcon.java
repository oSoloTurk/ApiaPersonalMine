package me.osoloturk.personalmine.gui_infrastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryIcon {
	private final ItemStack itemStack;
	private final List<ClickAction> clickActions = new ArrayList<>();
    private boolean shouldCancel = true;
    
	public InventoryIcon(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public InventoryIcon clone() {
		return new InventoryIcon(itemStack.clone());
	}
	
	public InventoryIcon addClickAction(ClickAction... clickAction) {
		clickActions.addAll(Arrays.asList(clickAction));
		return this;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public List<ClickAction> getClickActions() {
		return this.clickActions;
	}
	
	public void setName(String name) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(name);
		itemStack.setItemMeta(itemMeta);
	}
	
	public void setLore(List<String> lore) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
	}
	
	public List<String> getLore() {
		return itemStack.getItemMeta().getLore();
	}
	
	public InventoryIcon setClickAction(ClickAction clickAction) {
		clickActions.clear();
		clickActions.add(clickAction);
		return this;
	}
	
	public InventoryIcon clearActions() {
		clickActions.clear();
		return this;
	}
	
	public boolean isShouldCancel() {
		return shouldCancel;
	}
	
	public InventoryIcon setShouldCancel(boolean shouldCancel) {
		this.shouldCancel = shouldCancel;
		return this;
	}
	
	public void glow() {
		itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemStack.setItemMeta(itemMeta);
	}
}
