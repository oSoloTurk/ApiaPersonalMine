package me.osoloturk.personalmine.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.osoloturk.personalmine.APM;

public class FlyListener implements Listener {
	private final APM instance;
	private List<UUID> flyedPlayers;
	
	public FlyListener(APM instance) {
		this.instance = instance;
		flyedPlayers = new ArrayList<>();
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		if((event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
				|| event.getPlayer().isOp())
			return;
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			UUID uuid = event.getPlayer().getUniqueId();
			if(instance.getMineBlockManager().isInBreakArea(event.getTo())) {
				if(!event.getPlayer().getAllowFlight() && !flyedPlayers.contains(uuid)) {
					flyedPlayers.add(event.getPlayer().getUniqueId());
					instance.getServer().getScheduler().runTask(instance, () -> event.getPlayer().setAllowFlight(true));
				}
			} else if(event.getPlayer().getAllowFlight() && flyedPlayers.contains(uuid)) {
				instance.getServer().getScheduler().runTask(instance, () -> event.getPlayer().setAllowFlight(false));
				flyedPlayers.remove(uuid);
			}
		});
	}
}
