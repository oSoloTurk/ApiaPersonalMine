package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {
	
	protected final String[] mainCommand;
	protected final String[] alternativeCommand;
	protected final String permission;
	
	public AbstractCommand(String mainCommand, String alternativeArgument, String permission) {
		this.mainCommand = mainCommand.split(" ");
		this.alternativeCommand = alternativeArgument != null ? alternativeArgument.split(" ") : new String[0];
		this.permission = permission;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);
	
	public abstract int getMinArg();
	
	public abstract int getMaxArg();
	
	public abstract boolean canExecuteableByConsole();
	
	public String getUsage() {
		return "";
	}
	
	public List<String> getUsages() {
		return Collections.emptyList();
	}
	
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain){
		return null;
	}
	
	public List<String> getCompleteSuggestions(int length, String[] args, boolean matchInMain) {
		List<String> returnArray = getCompleteSuggestionsPlus(length, args, matchInMain);
		if((returnArray == null || returnArray.isEmpty()) && (length <= getMaxArg())) {
			if(matchInMain) {
				if(length <= mainCommand.length && mainCommand[length - 1].toLowerCase().contains(args[length - 1].toLowerCase())) {
					returnArray = Arrays.asList(mainCommand[length - 1]);
				}
			} else {
				if(length <= alternativeCommand.length && alternativeCommand[length - 1].toLowerCase().contains(args[length - 1].toLowerCase())) {
					returnArray = Arrays.asList(alternativeCommand[length - 1]);
				}
			}
		}
		return returnArray == null ? Collections.emptyList() : returnArray;
	}
	
	public String[] getAlternativeArguments() {
		return alternativeCommand.length > 0 ? alternativeCommand : new String[0];
	}
	
	public String[] getMainArguments() {
		return mainCommand.length > 0 ? mainCommand : new String[0];
	}
	
	public String getPermission() {
		return permission;
	}
	
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(getPermission());
	}
}
