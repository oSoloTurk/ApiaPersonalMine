package me.osoloturk.personalmine.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class MainCommand extends AbstractCommand {
	private final APM instance;
	
	public MainCommand(APM instance) {
		super("", null, "apm.gui");
		this.instance = instance;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		instance.getGuiManager().openUserDashBoard((Player) sender);
		return true;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_MAIN_COMMAND.getStringList();
	}

	@Override
	public int getMinArg() {
		return 0;
	}

	@Override
	public int getMaxArg() {
		return 0;
	}

	@Override
	public boolean canExecuteableByConsole() {
		return false;
	}

}
