package me.osoloturk.personalmine.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MinePlayer;

public class PlayerManager {
	private final APM instance;
	private Map<UUID, MinePlayer> minePlayers;
	
	public PlayerManager(APM instance) {
		this.instance = instance;
		minePlayers = new HashMap<>();
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			for(Player player : instance.getServer().getOnlinePlayers()) {
				addMinePlayer(player.getUniqueId(), instance.getDatabase().getMinePlayer(player.getUniqueId()));
			}
		});
	}
	
	public void addMinePlayer(UUID uuid, MinePlayer profile) {
		minePlayers.put(uuid, profile);
	}
	
	public void removeMinePlayer(UUID uuid) {
		if(minePlayers.containsKey(uuid))
			instance.getDatabase().savePlayer(uuid, minePlayers.remove(uuid));
		instance.getSelectorManager().clearPosOfPlayer(uuid);
	}
	
	public MinePlayer getMinePlayer(UUID uuid) {
		return minePlayers.get(uuid);
	}
	
	public Collection<MinePlayer> getMinePlayers() {
		return minePlayers.values();
	}
	
	public Map<UUID, MinePlayer> getMinePlayersWithUUID() {
		return minePlayers;
	}
	
}
