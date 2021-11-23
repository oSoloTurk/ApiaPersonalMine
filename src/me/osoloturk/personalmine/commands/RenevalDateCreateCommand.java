package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Utils;

public class RenevalDateCreateCommand extends AbstractCommand {
	private final APM instance;
	
	public RenevalDateCreateCommand(APM instance) {
		super("renewaldate create", Settings.ARGUMENTS_RENEWALDATE_CREATE.getString(), "apm.renewaldate.create");
		this.instance = instance;
	}
	
	// apm renewaldate create <permission> <date>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(Utils.getInt(args[1], -1) == -1) {
			Settings.ERROR_PARSE_INTEGER.send(sender, null);
			return false;
		}
		if(instance.getConfig().isSet(Settings.RENEWAL_DATE_BASE.getPath() + "." + args[0])) {
			Settings.ERROR_RENEWALDATE_ALREADYCREATED.send(sender, null);
			instance.getServer().dispatchCommand(sender, "apm renevaldate set " + args[0] + " " + args[1]);
			return false;
		}
		instance.getConfig().set(Settings.RENEWAL_DATE_BASE.getPath() + "." + args[0], Utils.getInt(args[1], -1));
		Settings.MESSAGE_RENEWALDATE_CREATE.send(sender, Arrays.asList("%permission%", args[0], "%date%", args[1]));
		instance.saveConfig();
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_RENEWALDATE_CREATE.getStringList();
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3)
			Arrays.asList("Enter a name for renewaldate permission");
		if(length == 4)
			return Arrays.asList("Enter a time as minute");
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
