
package me.osoloturk.personalmine.gui_infrastructure;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.osoloturk.personalmine.APM;

import java.util.*;

public class SimpleCustomInventory implements CustomInventory {
	private final Map<Integer, InventoryIcon> icons = new LinkedHashMap<>();
	private final Set<CloseAction> closeActions = new HashSet<>();
	private final Set<Player> viewers = new HashSet<>();
	private Size size;
	private String title;
	private boolean operation, interactInventory = true, shouldCancel = true;
	private APM instance;
	
	public SimpleCustomInventory(APM instance, Size size, String title) {
		this(instance, size, title, null);
	}
	
	public SimpleCustomInventory(APM instance) {
		this(instance, null, null, null);
	}
	
	public SimpleCustomInventory(APM instance, Size size, String title, Map<Integer, InventoryIcon> defaults) {
		this.instance = instance;
		this.size = size;
		this.title = title;

		if (defaults != null) {
			defaults.entrySet().forEach(entry -> setIcon(entry.getKey(), entry.getValue(), false));
		}
	}
	
    public SimpleCustomInventory clone() {
    	return new SimpleCustomInventory(instance, size, title, icons);
    }
    
	@Override
	public void setSize(Size size) {
		Validate.notNull(size, "Size can't be null!");

		this.size = size;
	}
	
	@Override
	public void setTitle(String title) {
		Validate.notNull(title, "Title can't be null!");

		this.title = title;
	}
	
	@Override
	public boolean canInteractInventory() {
		return interactInventory;
	}
	
	@Override
	public boolean addIcon(InventoryIcon icon) {
		for(int i = 0; i < size.getSize(); i++) {
			if(!icons.containsKey(i)) {
				icons.put(i, icon);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setIcon(int position, InventoryIcon icon, boolean override) {
		if (icon == null) {
			icons.remove(position);
		} else {
			if(override || !icons.containsKey(position)) icons.put(position, icon);
		}
	}
	
	@Override
	public InventoryIcon getIcon(int position) {
		return icons.get(position);
	}

	@Override
	public boolean removeIcon(int position) {
		return icons.remove(position) != null;
	}
	
	@Override
	public Map<Integer, InventoryIcon> getIcons() {
		return Collections.unmodifiableMap(icons);
	}
	
	@Override
	public boolean addCloseAction(CloseAction action) {
		return closeActions.add(action);
	}
	
	@Override
	public Set<CloseAction> getCloseActions() {
		return closeActions;
	}
	
	@Override
	public void setOperation(boolean operation) {
		this.operation = operation;
	}
	
	@Override
	public boolean isOperation() {
		return operation;
	}
	
	@Override
	public Inventory createInventory(Player player) {
		Validate.notNull(player, "Player can't be null!");
		Inventory inventory = size == Size.HOPPER ? Bukkit.createInventory(new CustomInventoryHolder(this), InventoryType.HOPPER, title) : Bukkit.createInventory(new CustomInventoryHolder(this), size.getSize(), title);
		for(Map.Entry<Integer, InventoryIcon> entry : icons.entrySet()) {
			if(entry.getKey() < 0 || entry.getKey() >= size.getSize())
				continue;
			try {
				inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
			} catch(Exception e) {
				// getLogger().log(Level.SEVERE, "Error while building item in slot: " + entry.getKey(), e);
			}
		}
		return inventory;
	}
	
	@Override
	public Inventory openInventory(Player player) {
		Inventory inventory = createInventory(player);

		player.openInventory(inventory);

		return inventory;
	}
	
	@Override
	public void openInventoryUpdateMode(Player player) {
		Inventory inventory = createInventory(player);

		player.openInventory(inventory);
	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            if(hasInventory(player)) update(player);
	            else cancel();
	        }
	    }.runTaskTimerAsynchronously(instance, 1, 20);
	}

	@Override
	public void update() {
		Iterator<Player> iterator = viewers.iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player.isOnline()) {
				update(player);
			} else {
				iterator.remove();
			}
		}
	}
	
	@Override
	public Collection<Player> getViewers() {
		return viewers;
	}
	
	@Override
	public Size getSize() {
		return size;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public boolean isShouldCancel() {
		return shouldCancel;
	}
	
	public void setShouldCancel(boolean shouldCancel) {
		this.shouldCancel = shouldCancel;
	}
	
	@Override
	public void setInteractInventory(boolean interactInventory) {
		this.interactInventory = interactInventory; 
	}
	
	public void update(Player player) {
		Inventory inventory = player.getOpenInventory().getTopInventory();
		if (inventory == null) return;

		if (inventory.getHolder() instanceof CustomInventoryHolder) {
			CustomInventoryHolder customHolder = (CustomInventoryHolder) inventory.getHolder();
			if (customHolder.getCustomInventory() != this) {
				customHolder.setCustomInventory(this);
			}
			if (getSize().getSize() != inventory.getSize()) {
				openInventory(player);
			} else {
				Inventory newInventory = createInventory(player);
				ItemStack[] currentContents = inventory.getContents();
				ItemStack[] newContents = newInventory.getContents();
				for (int i = 0; currentContents.length > i; i++) {
					ItemStack oldStack = currentContents[i];
					ItemStack newStack = newContents[i];
					if (oldStack == null && newStack != null) {
						currentContents[i] = newStack;
					} else if (oldStack != null && newStack != null) {
						if (!oldStack.isSimilar(newStack)) {
							currentContents[i] = newStack;
						}
					} else {
						currentContents[i] = null;
					}
				}
				inventory.setContents(currentContents);
			}
		}
	}
	
	public void update(Player player, String inventoryTitle) {
		Inventory inventory = player.getOpenInventory().getTopInventory();
		if (inventory == null) return;

		if (inventory.getHolder() instanceof CustomInventoryHolder) {
			CustomInventoryHolder customHolder = (CustomInventoryHolder) inventory.getHolder();
			if (customHolder.getCustomInventory() != this) {
				customHolder.setCustomInventory(this);
			}
			if (getSize().getSize() != inventory.getSize() || !Objects.equals(inventoryTitle, title)) {
				openInventory(player);
			} else {
				Inventory newInventory = createInventory(player);
				ItemStack[] currentContents = inventory.getContents();
				ItemStack[] newContents = newInventory.getContents();
				for (int i = 0; currentContents.length > i; i++) {
					ItemStack oldStack = currentContents[i];
					ItemStack newStack = newContents[i];

					if (oldStack == null && newStack != null) {
						currentContents[i] = newStack;
					} else if (oldStack != null && newStack != null) {
						if (!oldStack.isSimilar(newStack)) {
							currentContents[i] = newStack;
						}
					} else {
						currentContents[i] = null;
					}
				}
				inventory.setContents(currentContents);
			}
		}
	}
	
}