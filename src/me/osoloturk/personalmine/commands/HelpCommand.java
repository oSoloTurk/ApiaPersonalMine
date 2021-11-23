package me.osoloturk.personalmine.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class HelpCommand extends AbstractCommand{

	private final APM instance;
	
	public HelpCommand(APM instance) {
		super("help", Settings.ARGUMENTS_HELPS.getString(), "apm.helps");
		this.instance = instance;
	}
	
	// apm help
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		for(AbstractCommand command : instance.getCommandManager().getCommands()) {
			if(sender.hasPermission(command.getPermission())) {
				command.getUsages().forEach(sender::sendMessage);
			}
		}
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_HELPS.getStringList();
	}

	@Override
	public int getMinArg() {
		return 0;
	}

	@Override
	public int getMaxArg() {
		return 1;
	}

	@Override
	public boolean canExecuteableByConsole() {
		return true;
	}
	
	
}
