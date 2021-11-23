package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Utils;

public class GeneratorGiveCommand extends AbstractCommand {
	
	private final APM instance;
	private int transitAmount;
	
	public GeneratorGiveCommand(APM instance) {
		super("generator give", Settings.ARGUMENTS_GENERATOR_GIVE.getString(), "apm.generator.give");
		this.instance = instance;
	}
	
	// apm generator give <player-name> [amount]
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player targetPlayer = instance.getServer().getPlayer(args[0]);
		if(targetPlayer == null) {
			Settings.USAGE_SELECTOR.getStringList().forEach(sender::sendMessage);
			return false;
		}
		if(instance.getGeneratorListener() == null) {
			Settings.ERROR_GENERATOR_LISTENER_DISABLED.send(sender, null);
			return false;
		}
		transitAmount = args.length == 2 ? Utils.getInt(args[1], 1) : 1;
		Settings.MESSAGE_GENERATOR_GIVE_ADMIN.send(sender, Arrays.asList("%target%", targetPlayer.getDisplayName(), "%amount%", "" + transitAmount));
		Settings.MESSAGE_GENERATOR_GIVE_TARGET.send(targetPlayer, Arrays.asList("%amount%", "" + transitAmount));
		while(transitAmount > 0) {
			targetPlayer.getInventory().addItem(instance.getGeneratorListener().getGeneratorItem());
			transitAmount--;
		}
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_SELECTOR.getStringList();
	}
	
	public enum SelectorType {
		SINGLE,
		MULTI;
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3)
			return instance.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
		if(length == 4)
			return Arrays.asList("Enter a amount");
		return null;
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