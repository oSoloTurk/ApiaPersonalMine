package me.osoloturk.personalmine.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class MineBlockCreateCommand extends AbstractCommand {
	private final APM instance;
	
	public MineBlockCreateCommand(APM instance) {
		super("mineblock create", Settings.ARGUMENTS_MINEBLOCK_CREATE.getString(), "apm.selector");
		this.instance = instance;
	}
	
	// apm mineblock create
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Pair<Location, Location> poses = instance.getSelectorManager().getPosOfPlayer(((Player) sender).getUniqueId());
		if(poses == null || (poses != null && (poses.getKey() == null || poses.getValue() == null))) {
			Settings.ERROR_POS_IS_EMPTY.send(sender, null);
			return false;
		}
		if((instance.getMineBlockManager().isMineBlock(poses.getKey()) || instance.getMineBlockManager().isMineBlock(poses.getValue()))) {
			Settings.ERROR_MINEBLOCK_ALREADY_HAVE.send(sender, null);
			return false;
		}
		instance.getMineBlockManager().addMineBlock(poses, AppearanceType.MULTI);
		Settings.MESSAGE_SELECTOR_CREATE_MULTI.send(sender,
				Arrays.asList("%first%", Utils.getPrettyStringFromLocation(poses.getKey()), "%second%", Utils.getPrettyStringFromLocation(poses.getValue())));
	
		return true;
	}
	
	@Override
	public List<String> getUsages() {
		return Settings.USAGE_MINEBLOCK_CREATE.getStringList();
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
