package me.osoloturk.personalmine.hooks.skyblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;

import me.osoloturk.personalmine.APM;

public class SuperiorSkyblockHook extends ISkyBlock implements Listener {
	private final APM instance;
	
	public SuperiorSkyblockHook(APM instance) {
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		return SuperiorSkyblockAPI.getIslandAt(player.getLocation()).getIslandMembers(true).stream().filter(member -> member.getUniqueId().equals(player.getUniqueId())).findAny().isPresent();
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return SuperiorSkyblockAPI.getIslandsWorld().equals(loc.getWorld());
	}
	
	@Override
	public String getSkyBlockWorld() {
		return SuperiorSkyblockAPI.getIslandsWorld().getName();
	}
	
	@EventHandler
	public void onReset(IslandLeaveEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(event.getPlayer().getUniqueId());
	}
}
