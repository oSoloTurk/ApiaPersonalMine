package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;

public class ChancePackRemoveCommand extends AbstractCommand {
	private final APM instance;
	
	public ChancePackRemoveCommand(APM instance) {
		super("chancepack remove", Settings.ARGUMENTS_CHANCEPACK_REMOVE.getString(), "apm.chancepack.remove");
		this.instance = instance;
	}
	
	// apm chancepack remove <packname> <player>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(instance.getChanceManager().getChancePack(args[0]) == null) {
			Settings.ERROR_CHANCEPACK_ISNULL.send(sender, null);
			return false;
		}
		Player targetPlayer = instance.getServer().getPlayer(args[1]);
		if(targetPlayer == null) {
			Settings.ERROR_PLAYER_ISNULL.send(sender, null);
			return false;
		}
		MinePlayer targetMinePlayer = instance.getPlayerManager().getMinePlayer(targetPlayer.getUniqueId());
		if(!targetMinePlayer.isHave(args[0])) {
			Settings.ERROR_PLAYER_CHANCEPACK_NOT_HAVE.send(sender, null);
			return false;
		}
		targetMinePlayer.removeChancePack(args[0]);
		List<String> replaceList = Arrays.asList("%target%", targetPlayer.getDisplayName(), "%pack%", args[0]);
		Settings.MESSAGE_CHANCEPACK_REMOVE_ADMIN.send(sender, replaceList);
		Settings.MESSAGE_CHANCEPACK_REMOVE_TARGET.send(targetPlayer, replaceList);
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_CHANCEPACK_REMOVE.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		List<String> suggestion = null;
		if(length == 3)
			suggestion = instance.getChanceManager().getChances().keySet().stream().map(perm -> perm).collect(Collectors.toList());
		else if(length == 4)
			suggestion = instance.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
		return suggestion;
	}
	
	@Override
	public int getMinArg() {
		return 2;
	}
	
	@Override
	public int getMaxArg() {
		return 4;
	}
	
	@Override
	public boolean canExecuteableByConsole() {
		return true;
	}
	
}