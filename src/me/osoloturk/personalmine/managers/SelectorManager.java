package me.osoloturk.personalmine.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;

public class SelectorManager {
	private final APM instance;
	private ItemStack singleSelector, multiSelector;
	private Map<UUID, Pair<Location, Location>> multiPosOfPlayer;
	
	public SelectorManager(APM instance) {
		this.instance = instance;
		multiPosOfPlayer = new HashMap<>();
		loadSelectors();
	}
	
	public void loadSelectors() {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			multiSelector = instance.getGuiManager().createItem(Settings.MINE_BLOCK_SELECTOR_MULTI.getPath());
			singleSelector = instance.getGuiManager().createItem(Settings.MINE_BLOCK_SELECTOR_SINGLE.getPath());
		});
	}
	
	public Location setMultiPos(UUID uuid, Action type, Location newPos) {
		Pair<Location, Location> pos = multiPosOfPlayer.getOrDefault(uuid, Pair.of(null, null));
		if(type == Action.LEFT_CLICK_BLOCK) {
			pos.setLeft(newPos);
			multiPosOfPlayer.put(uuid, pos);
			return pos.getValue();
		}
		if(type == Action.RIGHT_CLICK_BLOCK) {
			pos.setRight(newPos);
			multiPosOfPlayer.put(uuid, pos);
			return pos.getKey();
		}
		return newPos;
	}
	
	public Pair<Location, Location> getPosOfPlayer(UUID uuid) {
		return multiPosOfPlayer.get(uuid);
	}
	
	public void clearPosOfPlayer(UUID uuid) {
		if(multiPosOfPlayer.containsKey(uuid))
			multiPosOfPlayer.remove(uuid);
	}
	
	public ItemStack getSingleSelector() {
		return singleSelector;
	}
	
	public ItemStack getMultiSelector() {
		return multiSelector;
	}
}
