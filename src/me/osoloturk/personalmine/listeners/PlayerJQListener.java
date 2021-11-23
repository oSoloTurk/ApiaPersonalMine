package me.osoloturk.personalmine.listeners;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.MinePlayer;

public class PlayerJQListener implements Listener {
	private final APM instance;
	
	public PlayerJQListener(APM instance) {
		this.instance = instance;
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			MinePlayer mPlayer = instance.getDatabase().getMinePlayer(event.getPlayer().getUniqueId());
			instance.getPlayerManager().addMinePlayer(event.getPlayer().getUniqueId(), mPlayer);
			for(List<MineBlock> mineBlocks : instance.getMineBlockManager().getMineBlocks().values()) {
				if(!event.getPlayer().isOnline())
					break;
				for(MineBlock mineBlock : mineBlocks) {
					if(mineBlock.getCreateDate() > mPlayer.getLastExitDate()) {
						instance.getServer().getScheduler().runTask(instance, () -> instance.getChanceManager().createApperances(event.getPlayer(), mPlayer, mineBlock));
					}
				}
			}
		});
	}
	
	@EventHandler
	public void playerQuitEvent(PlayerQuitEvent event) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> instance.getPlayerManager().removeMinePlayer(event.getPlayer().getUniqueId()));
	}
}
