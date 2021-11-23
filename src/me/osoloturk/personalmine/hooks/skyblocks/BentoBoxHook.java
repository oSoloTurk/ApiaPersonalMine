package me.osoloturk.personalmine.hooks.skyblocks;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.osoloturk.personalmine.APM;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;

public class BentoBoxHook extends ISkyBlock implements Listener {
	private final APM instance;
	
	public BentoBoxHook(APM instance) {
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		return BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation()).get().getMembers().keySet().contains(player.getUniqueId());
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return !BentoBox.getInstance().getIslands().getIslands(loc.getWorld()).isEmpty();
	}
	
	@Override
	public String getSkyBlockWorld() {
		return getIslandWorld(BentoBox.getInstance().getIWM().getWorlds());
	}
	
	private String getIslandWorld(Set<World> worlds) {
		for(World world : worlds)
			if(world.getEnvironment() == Environment.NORMAL)
				return world.getName();
		return "world";
	}
	
	@EventHandler
	public void onReset(TeamLeaveEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(event.getPlayerUUID());
	}
	
}
