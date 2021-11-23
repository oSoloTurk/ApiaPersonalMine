package me.osoloturk.personalmine.runnables;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;

public class RenevalDateResetRunnable extends BukkitRunnable{

	private final APM instance;
	
	public RenevalDateResetRunnable(APM instance) {
		this.instance = instance;
	}
	
	@Override
	public void run() {
		for(Entry<UUID, MinePlayer> entry : instance.getPlayerManager().getMinePlayersWithUUID().entrySet()) {
			long date = 0;
			Player player = instance.getServer().getPlayer(entry.getKey());
			for(String permission : Settings.RENEWAL_DATE_BASE.getConfigurationSection(false)) {
				long value = 0;
				if(player.hasPermission(permission))
					value = instance.getConfig().getLong(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission) * 60000;
				if(value < date)
					date = value;
			}
			if(entry.getValue().getRenevalDate() + date >= System.currentTimeMillis()) {
				entry.getValue().resetApperances();
				instance.getChanceManager().createApperances(entry.getValue(), player);
				Settings.MESSAGE_APPEARANCES_RESET_NATURALLY.send(player, null);
			}
		}
	}
}
