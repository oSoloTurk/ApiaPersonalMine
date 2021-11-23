package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.ChancePack;
import me.osoloturk.personalmine.misc.Settings;

public class ChancePackCreateCommand extends AbstractCommand {
	private final APM instance;
	
	public ChancePackCreateCommand(APM instance) {
		super("chancepack create", Settings.ARGUMENTS_CHANCEPACK_CREATE.getString(), "apm.chancepack.create");
		this.instance = instance;
	}
	
	// apm chancepack create <packname>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(instance.getChanceManager().getChancePack(args[0]) != null) {
			Settings.ERROR_CHANCEPACK_NOTNULL.send(sender, null);
			return false;
		}
		String path = Settings.CHANCES_BASE.getPath() + "." + args[0];
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			List<String> desc = Arrays.asList("Created", "by", "ApiaTeam");
			instance.getConfig().set(path + ".default", false);
			instance.getConfig().set(path + ".sell", false);
			instance.getConfig().set(path + ".cost.money", 0);
			instance.getConfig().set(path + ".cost.exp", 0);
			instance.getConfig().set(path + ".blocks.STONE.chance", 0);
			instance.getConfig().set(path + ".descprition", desc);
			instance.saveConfig();
			instance.getChanceManager().addChancePack(args[0], new ChancePack(false, false, 0, 0, new HashMap<>(), desc));
		});
		Settings.MESSAGE_CHANCEPACK_CREATE.send(sender, Arrays.asList("%pack%", args[0]));
		return true;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_CHANCEPACK_CREATE.getStringList();
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
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 3) {
			return Arrays.asList("Enter a chancepack name");
		}
		return null;
	}
}
