package me.osoloturk.personalmine.gui_infrastructure;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CustomInventory {

    Size getSize();

    void setSize(Size size);

    String getTitle();

    void setTitle(String title);
    
    boolean isOperation();
    
    void setOperation(boolean operation);

    boolean canInteractInventory();

    void setInteractInventory(boolean interactInventory);

    boolean addIcon(InventoryIcon icon);
    
    boolean removeIcon(int position);

	void setIcon(int position, InventoryIcon icon, boolean override);

    InventoryIcon getIcon(int position);

    Map<Integer, InventoryIcon> getIcons();
    
    boolean addCloseAction(CloseAction action);
    
    Set<CloseAction> getCloseActions();

    default boolean hasInventory(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        return inventory != null && inventory.getHolder() instanceof CustomInventoryHolder && ((CustomInventoryHolder) inventory.getHolder()).getCustomInventory() == this;
    }

    Inventory createInventory(Player player);

    Inventory openInventory(Player player);
    
    void openInventoryUpdateMode(Player player);

    void update(Player player);

    void update();

    Collection<Player> getViewers();
    
    boolean isShouldCancel();
	
	void setShouldCancel(boolean shouldCancel);

    default void onOpen(Player player) {}

    default void onClose(Player player) {}

}