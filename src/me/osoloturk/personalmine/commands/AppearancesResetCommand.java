package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;

public class AppearancesResetCommand extends AbstractCommand {
	
	private final APM instance;
	
	public AppearancesResetCommand(APM instance) {
		super("appearances reset", Settings.ARGUMENTS_APPEARANCES_RESET.getString(), "apm.appearances.reset");
		this.instance = instance;
	}
	
	// apm appearances reset [player]
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player targetPlayer;
		targetPlayer = instance.getServer().getPlayer(args[0]);
		if(targetPlayer == null) {
			Settings.ERROR_PLAYER_ISNULL.send(sender, null);
			return false;
		}
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			MinePlayer minePlayer = instance.getPlayerManager().getMinePlayer(targetPlayer.getUniqueId());
			minePlayer.resetApperances();
			Settings.MESSAGE_APPEARANCES_RESET_ADMIN.send(sender, Arrays.asList("%target%", targetPlayer.getDisplayName()));
			Settings.MESSAGE_APPEARANCES_RESET_TARGET.send(targetPlayer, null);
		});
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_APPEARANCES_RESET.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3) {
			return instance.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
		}
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