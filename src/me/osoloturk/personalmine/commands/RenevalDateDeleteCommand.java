package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class RenevalDateDeleteCommand extends AbstractCommand {
	private final APM instance;
	
	public RenevalDateDeleteCommand(APM instance) {
		super("renewaldate delete", Settings.ARGUMENTS_RENEWALDATE_DELETE.getString(), "apm.renewaldate.delete");
		this.instance = instance;
	}
	
	// apm renewaldate delete <permission>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(!instance.getConfig().isSet(Settings.RENEWAL_DATE_BASE.getPath() + "." + args[0])) {
			Settings.ERROR_RENEVALDATE_NOT_FOUND.send(sender, null);
			return false;
		}
		instance.getConfig().set(Settings.RENEWAL_DATE_BASE.getPath() + "." + args[0], null);
		Settings.MESSAGE_RENEWALDATE_CREATE.send(sender, Arrays.asList("%permission%", args[0]));
		instance.saveConfig();
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_RENEWALDATE_DELETE.getStringList();
	}

	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3) return Settings.RENEWAL_DATE_BASE.getConfigurationSection(false).stream().collect(Collectors.toList());
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
