package me.osoloturk.personalmine.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class AdminGui extends AbstractCommand {
	private final APM instance;
	
	public AdminGui(APM instance) {
		super("admin", Settings.ARGUMENTS_ADMIN.getString(), "apm.admingui");
		this.instance = instance;
	}
	
	// apm admin	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		instance.getGuiManager().openAdminDashBoard((Player) sender);
		return true;
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
		return false;
	}
	
}