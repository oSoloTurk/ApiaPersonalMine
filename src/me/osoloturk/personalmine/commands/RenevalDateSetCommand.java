package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Utils;

public class RenevalDateSetCommand extends AbstractCommand {
	private final APM instance;
	
	public RenevalDateSetCommand(APM instance) {
		super("renewaldate set", Settings.ARGUMENTS_RENEWALDATE_SET.getString(), "apm.renewaldate.set");
		this.instance = instance;
	}
	
	// apm renewaldate set <permission> <date>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(Utils.getInt(args[1], -1) == -1) {
			Settings.ERROR_PARSE_INTEGER.send(sender, null);
			return false;
		}
		instance.getConfig().set(Settings.RENEWAL_DATE_BASE.getPath() + "." + args[0], Utils.getInt(args[1], -1));
		Settings.MESSAGE_RENEWALDATE_SET.send(sender, Arrays.asList("%permission%", args[0], "%date%", args[1]));
		instance.saveConfig();
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_RENEWALDATE_SET.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		List<String> suggestion = null;
		if(length == 3)
			suggestion = Settings.RENEWAL_DATE_BASE.getConfigurationSection(false).stream().collect(Collectors.toList());
		else if(length == 4)
			suggestion = Arrays.asList("Enter a minute");
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
