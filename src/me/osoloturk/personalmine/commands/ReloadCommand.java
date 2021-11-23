package me.osoloturk.personalmine.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class ReloadCommand extends AbstractCommand{
	private final APM instance;
	
	public ReloadCommand(APM instance) {
		super("reload", Settings.ARGUMENTS_RELOAD.getString(), "apm.reload");
		this.instance = instance;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		instance.loadSystem(true);
		Settings.MESSAGE_RELOAD.send(sender, null);
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_RELOAD.getStringList();
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
