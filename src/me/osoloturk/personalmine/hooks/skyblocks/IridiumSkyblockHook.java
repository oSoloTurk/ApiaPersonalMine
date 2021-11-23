package me.osoloturk.personalmine.hooks.skyblocks;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.UserLeaveEvent;
import com.iridium.iridiumskyblock.database.Island;

import me.osoloturk.personalmine.APM;

public class IridiumSkyblockHook extends ISkyBlock implements Listener {
	private final APM instance;
	private final IridiumSkyblock API;

	public IridiumSkyblockHook(APM instance) {
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
		API = IridiumSkyblock.getInstance();
	}

	@Override
	public boolean isMemberOfThisIsland(Player player) {
		final Optional<Island> island = API.getIslandManager().getIslandViaLocation(player.getLocation());
		if(!island.isPresent()) return false;
		return island.get().getMembers().stream().anyMatch(member -> member.equals(player.getUniqueId().toString()));
	}

	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return API.getIslandManager().getWorld().equals(loc.getWorld());
	}
	
	@Override
	public String getSkyBlockWorld() {
		return API.getIslandManager().getWorld().getName();
	}
	
	@EventHandler
	public void onReset(UserLeaveEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(event.getUser().getUuid());
	}
}
