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

public class RenevalDatePResetCommand extends AbstractCommand {
	
	private final APM instance;
	private final SimpleDateFormat dateFormat;
	
	public RenevalDatePResetCommand(APM instance) {
		super("renewaldate reload", Settings.ARGUMENTS_RENEWALDATE_RELOAD.getString(), "apm.renewaldate.reset");
		this.instance = instance;
		dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT.getString());
	}
	
	// apm renewaldate preset [player]
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player targetPlayer = instance.getServer().getPlayer(args[0]);
		if(targetPlayer == null) {
			Settings.ERROR_PLAYER_ISNULL.send(sender, null);
			return false;
		}
		Player player = (Player) sender;
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			MinePlayer minePlayer = instance.getPlayerManager().getMinePlayer(player.getUniqueId());
			long date = 0;
			for(String permission : Settings.RENEWAL_DATE_BASE.getConfigurationSection(false)) {
				long value = 0;
				if(targetPlayer.hasPermission(permission))
					value = instance.getConfig().getLong(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission) * 60000;
				if(value < date)
					date = value;
			}
			minePlayer.setRenevalDate(System.currentTimeMillis() - date);
			String dateTime = dateFormat.format(new Date(date));
			Settings.MESSAGE_RENEWAL_DATE_RESET_ADMIN.send(sender, Arrays.asList("%target%", targetPlayer.getDisplayName(), "%date%", dateTime));
			Settings.MESSAGE_RENEWAL_DATE_RESET_TARGET.send(targetPlayer, Arrays.asList("%date%", dateTime));
		});
		return true;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_RENEWALDATE_RELOAD.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3) return instance.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
		return null;
	}
	
	@Override
	public int getMinArg() {
		return 2;
	}
	
	@Override
	public int getMaxArg() {
		return 3;
	}
	
	@Override
	public boolean canExecuteableByConsole() {
		return true;
	}
	
}