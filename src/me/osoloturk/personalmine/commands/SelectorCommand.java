package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class SelectorCommand extends AbstractCommand {
	
	private final APM instance;
	private SelectorType transitSelectorType;
	
	public SelectorCommand(APM instance) {
		super("selector", Settings.ARGUMENTS_SELECTOR.getString(), "apm.selector");
		this.instance = instance;
	}
	
	// apm selector <single/multi>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		transitSelectorType = null;
		switch(args[0].toUpperCase(Locale.ENGLISH)) {
		case "MULTI":
			transitSelectorType = SelectorType.MULTI;
			break;
		case "SINGLE":
			transitSelectorType = SelectorType.SINGLE;
			break;
		}
		if(transitSelectorType == null) {
			Settings.USAGE_SELECTOR.getStringList().forEach(sender::sendMessage);
			return false;
		}
		switch(transitSelectorType) {
		case MULTI:
			((Player) sender).getInventory().addItem(instance.getSelectorManager().getMultiSelector());
			Settings.MESSAGE_SELECTOR_GIVE_MULTI.send(sender, null);
			break;
		case SINGLE:
			((Player) sender).getInventory().addItem(instance.getSelectorManager().getSingleSelector());
			Settings.MESSAGE_SELECTOR_GIVE_SINGLE.send(sender, null);
			break;
		}
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_SELECTOR.getStringList();
	}
	
	public enum SelectorType {
		SINGLE,
		MULTI;
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		if(length == 2) return Arrays.asList("single", "multi");
		return null;
	}
	
	@Override
	public int getMinArg() {
		return 1;
	}
	
	@Override
	public int getMaxArg() {
		return 2;
	}
	
	@Override
	public boolean canExecuteableByConsole() {
		return false;
	}
	
}