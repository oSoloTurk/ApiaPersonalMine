package me.osoloturk.personalmine.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.services.SoundService.Sounds;
import me.osoloturk.personalmine.utils.Pair;

public class CommandNavigator implements CommandExecutor, TabCompleter {
	
	private final APM instance;
	
	public CommandNavigator(APM instance) {
		this.instance = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command commandmain, String label, String[] args) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < args.length; i++) {
			sb.append(args[i] + " ");
		}
		String fullCommand = sb.toString();
		Pair<AbstractCommand, Boolean> command = instance.getCommandManager().getCommand(fullCommand);
		if(sender instanceof Player && !(sender.hasPermission(command.getKey().getPermission()) || sender.hasPermission("apm.*"))) {
			Settings.ERROR_PERMISSION.send(sender, null);
			return false;
		}
		int sourceLength = (command.getValue() ? command.getKey().getMainArguments().length : command.getKey().getAlternativeArguments().length);
		String[] realArgs = new String[((args.length - sourceLength) < 0 ? 0 : (args.length - sourceLength))];
		for(int i = 0; i < realArgs.length; i++)
			realArgs[i] = args[sourceLength + i];
		if(args.length >= command.getKey().getMinArg() && args.length <= command.getKey().getMaxArg())
			instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
				final boolean result = command.getKey().execute(sender, realArgs);
				if(sender instanceof Player) {
					if(result)
						instance.getSoundService().playSound((Player)sender, Sounds.COMMAND_SUCCESS);
					else
						instance.getSoundService().playSound((Player)sender, Sounds.COMMAND_FAILED);
				}
			});
		else
			command.getKey().getUsages().forEach(sender::sendMessage);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0) {
			ArrayList<String> result = new ArrayList<>();
			int length = args.length;
			for(AbstractCommand command : instance.getCommandManager().getCommands()) {
				if(length > command.getMaxArg() && length < command.getMinArg())
					continue;
				boolean complete = true;
				String[] mainArgs = command.getMainArguments();
				for(int i = 0; i < length; i++) {
					if(i < mainArgs.length && !mainArgs[i].toLowerCase().contains(args[i].toLowerCase())) {
						complete = false;
						break;
					}
				}
				if(complete && sender.hasPermission(command.getPermission()))
					result.addAll(command.getCompleteSuggestions(length, args, true));
				String[] altArgs = command.getAlternativeArguments();
				for(int i = 0; i < length; i++) {
					if(i < altArgs.length && !altArgs[i].toLowerCase().contains(args[i].toLowerCase())) {
						complete = false;
						break;
					}
				}
				if(complete && sender.hasPermission(command.getPermission()))
					result.addAll(command.getCompleteSuggestions(length, args, false));
				
			}
			return result;
		}
		return Collections.emptyList();
	}

}