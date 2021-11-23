package me.osoloturk.personalmine.commands;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;

public class TimeLeftCommand extends AbstractCommand {
	
	private final APM instance;
	private final SimpleDateFormat dateFormat;
	
	public TimeLeftCommand(APM instance) {
		super("timeleft", Settings.ARGUMENTS_TIMELEFT.getString(), "apm.renevaldate.reset");
		this.instance = instance;
		dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT.getString());
	}
	
	// apm timeleft [player=sender]
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player;
		if(args.length == 1) {
			player = instance.getServer().getPlayer(args[0]);
			if(player == null) {
				Settings.ERROR_PLAYER_ISNULL.send(sender, null);
				return false;
			}
		} else {
			if(!(sender instanceof Player)) {
				Settings.ERROR_CONSOLE.send(sender, null);
				return false;
			} else
				player = (Player) sender;
		}
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			MinePlayer minePlayer = instance.getPlayerManager().getMinePlayer(player.getUniqueId());
			long date = 0;
			for(String permission : Settings.RENEWAL_DATE_BASE.getConfigurationSection(false)) {
				long value = 0;
				if(player.hasPermission(permission))
					value = instance.getConfig().getLong(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission) * 60000;
				if(value < date)
					date = value;
			}
			Settings.MESSAGE_TIMELEFT.send(sender, Arrays.asList("%player%", player.getDisplayName(), "%date%", dateFormat.format(new Date(date - (System
					.currentTimeMillis() - minePlayer.getRenevalDate())))));
		});
		return true;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_TIMELEFT.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 2) return instance.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
		return null;
	}
	
	@Override
	public int getMinArg() {
		return 1;
	}
	
	@Override
	public int getMaxArg() {
		return 2;
	}
	
	@Override
	public boolean canExecuteableByConsole() {
		return true;
	}
	
}