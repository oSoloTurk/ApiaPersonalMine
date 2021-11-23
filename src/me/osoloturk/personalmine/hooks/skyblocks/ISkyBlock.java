package me.osoloturk.personalmine.hooks.skyblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class ISkyBlock implements Listener {
	
	public abstract boolean isMemberOfThisIsland(Player player);
	
	public abstract boolean isSkyBlockWorld(Location loc);
	
	public abstract String getSkyBlockWorld();
	
}
