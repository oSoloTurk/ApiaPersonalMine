package me.osoloturk.personalmine.hooks.skyblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.events.IslandLeaveEvent;

import com.wasteofplastic.askyblock.ASkyBlockAPI;

import me.osoloturk.personalmine.APM;

public class ASkyBlockHook extends ISkyBlock implements Listener {
	
	private final APM instance;
	
	public ASkyBlockHook(APM instance) {
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		return ASkyBlockAPI.getInstance().getIslandAt(player.getLocation()).getMembers().contains(player.getUniqueId());
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return ASkyBlockAPI.getInstance().getIslandWorld().equals(loc.getWorld());
	}
	
	@Override
	public String getSkyBlockWorld() {
		return ASkyBlockAPI.getInstance().getIslandWorld().getName();
	}
	
	@EventHandler
	public void onIslandReset(IslandLeaveEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(event.getPlayer());
	}
}
