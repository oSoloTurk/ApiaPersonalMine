package me.osoloturk.personalmine.hooks.skyblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.event.player.PlayerIslandLeaveEvent;
import com.songoda.skyblock.api.island.IslandRole;
import com.songoda.skyblock.island.IslandWorld;

import me.osoloturk.personalmine.APM;

public class FabledSkyBlockHook extends ISkyBlock implements Listener {
	private final APM instance;
	
	public FabledSkyBlockHook(APM instance) {
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		return SkyBlockAPI.getIslandManager().getIslandAtLocation(player.getLocation()).getRole(player) != IslandRole.VISITOR;
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return SkyBlockAPI.getImplementation().getWorldManager().isIslandWorld(loc.getWorld());
	}
	
	@Override
	public String getSkyBlockWorld() {
		return SkyBlockAPI.getImplementation().getWorldManager().getWorld(IslandWorld.Normal).getName();
	}
	
	@EventHandler
	public void onReset(PlayerIslandLeaveEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(event.getPlayer().getUniqueId());
	}
}
