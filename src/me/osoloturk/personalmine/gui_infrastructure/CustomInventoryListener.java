package me.osoloturk.personalmine.gui_infrastructure;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.services.SoundService.Sounds;
import me.osoloturk.personalmine.utils.Pair;

import java.util.*;

public class CustomInventoryListener implements Listener {
	private APM instance;
	
	public CustomInventoryListener(APM instance) {
		this.instance = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onClick(InventoryClickEvent event) {
		if(event.getAction() == InventoryAction.NOTHING)
			return;
		if(event.getView().getTopInventory().getHolder() instanceof CustomInventoryHolder) {
			CustomInventory customInventory = ((CustomInventoryHolder) event.getView().getTopInventory().getHolder()).getCustomInventory();
			if(event.getClickedInventory() == null) {
				event.setCancelled(true);
			} else {
				if(event.getClickedInventory().getHolder() instanceof CustomInventoryHolder) {
					InventoryIcon icon = customInventory.getIcon(event.getRawSlot());
					if(!isValid(event.getCurrentItem()) && icon == null)
						return;
					Player player = (Player) event.getWhoClicked();
					ClickType clickType = event.getClick();
					event.setCancelled(icon.isShouldCancel() || customInventory.isShouldCancel());
					for(ClickAction actionHandler : icon.getClickActions()) {
						if(actionHandler == null)
							return;
						instance.getSoundService().playSound(player, Sounds.GUI_CLICK);
						instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> actionHandler.handle(customInventory, player, clickType, event.isShiftClick(), event));
					}
				} else if(customInventory.canInteractInventory()) {
					if(event.isShiftClick() && isValid(event.getCurrentItem())) {
						event.setCancelled(true);
					} else if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR && isValid(event.getCursor())) {
						if(isValid(event.getCurrentItem()))
							return;
						stack(event.getCursor(), event.getClickedInventory());
						event.setCancelled(true);
					} else if(event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
						if(event.getRawSlot() == event.getSlot())
							event.setCancelled(true);
					} else if(event.getAction() == InventoryAction.UNKNOWN) {
						event.setCancelled(true);
					}
				} else {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onOpen(InventoryOpenEvent event) {
		if(event.isCancelled())
			return;
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(event.getInventory().getHolder() instanceof CustomInventoryHolder) {
				CustomInventory customInventory = ((CustomInventoryHolder) event.getInventory().getHolder()).getCustomInventory();
				customInventory.onOpen(player);
				customInventory.getViewers().add(player);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(event.getInventory().getHolder() instanceof CustomInventoryHolder) {
				CustomInventory customInventory = ((CustomInventoryHolder) event.getInventory().getHolder()).getCustomInventory();
				customInventory.getViewers().remove(player);
				customInventory.onClose(player);
				for(CloseAction actionHandler : customInventory.getCloseActions()) {
					if(actionHandler == null)
						return;
					instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> actionHandler.handle(customInventory, player, event));
				}
			}
		}
	}
	
	private final Comparator<Pair<ItemStack, Integer>> comparator = (pairA, pairB) -> {
		ItemStack a = pairA.getKey();
		ItemStack b = pairB.getKey();
		return (isValid(a, b) ? (a.getAmount() == b.getAmount() ? (pairA.getValue() < pairB.getValue() ? 1 : -1) : a.getAmount() > b.getAmount() ? 1 : -1)
				: (!isValid(a) && !isValid(b) ? 0 : (isValid(a) && !isValid(b) ? 1 : -1)));
	};
	
	private boolean isValid(ItemStack... itemStacks) {
		for(ItemStack itemStack : itemStacks)
			if(itemStack == null || itemStack.getType() == Material.AIR)
				return false;
		return true;
	}
	
	private void stack(ItemStack itemStack, Inventory inventory) {
		if(itemStack.getAmount() == itemStack.getMaxStackSize())
			return;
		ItemStack[] contents = inventory.getContents();
		List<Pair<ItemStack, Integer>> filtered = filter(itemStack, contents);
		filtered.sort(this.comparator);
		for(Pair<ItemStack, Integer> entry : filtered) {
			ItemStack content = entry.getKey();
			int needed = itemStack.getMaxStackSize() - itemStack.getAmount();
			if(content.getAmount() > needed) {
				itemStack.setAmount(itemStack.getAmount() + needed);
				content.setAmount(content.getAmount() - needed);
				return;
			} else if(content.getAmount() == needed) {
				itemStack.setAmount(itemStack.getAmount() + needed);
				inventory.setItem(entry.getValue(), null);
				return;
			} else if(content.getAmount() < needed) {
				itemStack.setAmount(itemStack.getAmount() + content.getAmount());
				inventory.setItem(entry.getValue(), null);
			}
		}
	}
	
	private List<Pair<ItemStack, Integer>> filter(ItemStack itemStack, ItemStack[] contents) {
		List<Pair<ItemStack, Integer>> list = new ArrayList<>();
		for(int i = 0; contents.length > i; i++) {
			ItemStack content = contents[i];
			if(itemStack == content)
				continue;
			if(itemStack.isSimilar(content))
				list.add(Pair.of(content, i));
		}
		return list;
	}
}