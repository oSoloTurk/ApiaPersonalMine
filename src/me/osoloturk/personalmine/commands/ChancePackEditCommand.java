package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class ChancePackEditCommand extends AbstractCommand {
	private final APM instance;
	
	public ChancePackEditCommand(APM instance) {
		super("chancepack edit", Settings.ARGUMENTS_CHANCEPACK_EDIT.getString(), "apm.chancepack.edit");
		this.instance = instance;
	}
	
	// apm chancepack edit <packname> money <newvalue>
	// apm chancepack edit <packname> exp <newvalue>
	// apm chancepack edit <packname> add <material> <chance> <data>
	// apm chancepack edit <packname> remove <material> [<data>]
	// apm chancepack edit <packname> defaultmode <newvalue>
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		EditType editType = null;
		switch(args[1].toUpperCase()) {
		case "MONEY":
			editType = args.length == 3 ? EditType.MONEY : null;
			break;
		case "EXP":
			editType = args.length == 3 ? EditType.EXP : null;
			break;
		case "ADD":
			editType = args.length >= 3 ? EditType.ADD : null;
			break;
		case "REMOVE":
			editType = args.length >= 3 ? EditType.REMOVE : null;
			break;
		case "DEFAULTMODE":
			editType = args.length == 3 ? EditType.DEFAULTMODE : null;
			break;
		}
		if(editType == null) {
			Settings.USAGE_CHANCEPACK_EDIT.getStringList().forEach(sender::sendMessage);
			return false;
		}
		if(instance.getChanceManager().getChancePack(args[0]) == null) {
			Settings.ERROR_CHANCEPACK_ISNULL.send(sender, null);
			return false;
		}
		String path = Settings.CHANCES_BASE.getPath() + "." + args[0];
		final EditType editTypeFinal = editType;
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			switch(editTypeFinal) {
			case MONEY:
				long moneyValue = Utils.getInt(args[2], -1);
				if(moneyValue == -1) {
					Settings.ERROR_PARSE_INTEGER.send(sender, null);
					return;
				}
				instance.getConfig().set(path + ".cost.money", moneyValue);
				long previousValue = instance.getChanceManager().getChancePack(args[0]).setCostMoney(moneyValue);
				Settings.MESSAGE_CHANCEPACK_EDIT_MONEY.send(sender, Arrays.asList("%pack%", args[0], "%previous%", "" + previousValue));
				break;
			case EXP:
				int expValue = Utils.getInt(args[2], -1);
				if(expValue == -1) {
					Settings.ERROR_PARSE_INTEGER.send(sender, null);
					return;
				}
				instance.getConfig().set(path + ".cost.exp", expValue);
				previousValue = instance.getChanceManager().getChancePack(args[0]).setCostExp(expValue);
				Settings.MESSAGE_CHANCEPACK_EDIT_EXP.send(sender, Arrays.asList("%pack%", args[0], "%previous%", "" + previousValue));
				break;
			case REMOVE:
				Material material = Material.valueOf(args[2]);
				byte data = args.length == 4 ? Utils.getByte(args[3], (byte) 0) : 0;
				if(material == null) {
					Settings.ERROR_MATERIAL_NOTFOUND.send(sender, null);
					return;
				}
				previousValue = instance.getChanceManager().getChancePack(args[0]).removeChanceItem(Pair.of(material, data));
				instance.getConfig().set(path + ".blocks." + material.toString(), null);
				Settings.MESSAGE_CHANCEPACK_EDIT_REMOVE.send(sender, Arrays.asList("%pack%", args[0], "%previous%", "" + previousValue));
				break;
			case ADD:
				material = Material.valueOf(args[2]);
				int chance = Utils.getInt(args[3], -1);
				data = args.length == 5 ? Utils.getByte(args[4], (byte) 0) : 0;
				if(material == null) {
					Settings.ERROR_MATERIAL_NOTFOUND.send(sender, null);
					return;
				}
				if(chance == -1) {
					Settings.ERROR_PARSE_INTEGER.send(sender, null);
					return;
				}
				previousValue = instance.getChanceManager().getChancePack(args[0]).setChanceItem(Pair.of(material, data), chance);
				instance.getConfig().set(path + ".blocks." + material.toString() + ".chance", chance);
				if(data != 0)
					instance.getConfig().set(path + ".blocks." + material.toString() + ".data", data);
				Settings.MESSAGE_CHANCEPACK_EDIT_ADD.send(sender, Arrays.asList("%pack%", args[0], "%previous%", "" + previousValue));
				break;
			case DEFAULTMODE:
				boolean value = instance.getChanceManager().getChancePack(args[0]).setDefaultMode(Boolean.parseBoolean(args[2]));
				instance.getConfig().set(path + ".default", !value);
				Settings.MESSAGE_CHANCEPACK_EDIT_ADD.send(sender, Arrays.asList("%pack%", args[0], "%previous%", "" + value));
				break;
			}
			instance.saveConfig();
		});
		
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_CHANCEPACK_EDIT.getStringList();
	}
	
	public enum EditType {
		MONEY,
		EXP,
		ADD,
		REMOVE,
		DEFAULTMODE;
	}
	
	@Override
	public int getMinArg() {
		return 4;
	}
	
	@Override
	public int getMaxArg() {
		return 7;
	}
	
	@Override
	public boolean canExecuteableByConsole() {
		return true;
	}
	
	@Override
	public List<String> getCompleteSuggestionsPlus(int length, String[] args, boolean matchInMain) {
		List<String> suggestion = null;
		if(length == 3)
			suggestion = instance.getChanceManager().getChances().keySet().stream().map(perm -> perm).collect(Collectors.toList());
		else if(length == 4)
			suggestion = Arrays.asList("money", "exp", "add", "remove", "defaultmode");
		else if(length == 5) {
			if(args[3].equalsIgnoreCase("money"))
				suggestion = Arrays.asList("<newvalue>");
			else if(args[3].equalsIgnoreCase("exp"))
				suggestion = Arrays.asList("<newvalue>");
			else if(args[3].equalsIgnoreCase("add"))
				suggestion = Arrays.asList("<material>");
			else if(args[3].equalsIgnoreCase("remove"))
				suggestion = Arrays.asList("<material>");
			else if(args[3].equalsIgnoreCase("defaultmode"))
				suggestion = Arrays.asList("<newvalue>");
		} else if(length == 6) {
			if(args[3].equalsIgnoreCase("add"))
				suggestion = Arrays.asList("<chance>");
			else if(args[3].equalsIgnoreCase("remove"))
				suggestion = Arrays.asList("<data>");
		} else if(length == 7 && args[3].equalsIgnoreCase("add"))
			suggestion = Arrays.asList("<data>");
		return suggestion;
	}
}
