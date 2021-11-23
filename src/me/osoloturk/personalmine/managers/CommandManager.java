package me.osoloturk.personalmine.managers;

import java.util.Arrays;
import java.util.List;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.commands.*;
import me.osoloturk.personalmine.utils.Pair;

public class CommandManager {
	private final AbstractCommand reload, mainCommand, cpCreateCommand, cpEditCommand, cpGiveCommand, cpRemoveCommand, selectorCommand, mineBlockCreateCommand, apperancesResetCommand, renevalDateResetCommand,
			renevalDateCreateCommand, renevalDateDeleteCommand, renevalDateSetCommand, timeLeftCommand, adminGui, helpCommand, generatorCommand;
	public final List<AbstractCommand> commands;
	
	public CommandManager(APM instance) {
		reload = new ReloadCommand(instance);
		mainCommand = new MainCommand(instance);
		cpCreateCommand = new ChancePackCreateCommand(instance);
		cpEditCommand = new ChancePackEditCommand(instance);
		cpGiveCommand = new ChancePackGiveCommand(instance);
		cpRemoveCommand = new ChancePackRemoveCommand(instance);
		selectorCommand = new SelectorCommand(instance);
		mineBlockCreateCommand = new MineBlockCreateCommand(instance);
		apperancesResetCommand = new AppearancesResetCommand(instance);
		renevalDateResetCommand = new RenevalDatePResetCommand(instance);
		renevalDateCreateCommand = new RenevalDateCreateCommand(instance);
		renevalDateDeleteCommand = new RenevalDateDeleteCommand(instance);
		renevalDateSetCommand = new RenevalDateSetCommand(instance);
		timeLeftCommand = new TimeLeftCommand(instance);
		helpCommand = new HelpCommand(instance);
		adminGui = new AdminGui(instance);
		generatorCommand = new GeneratorGiveCommand(instance);
		commands = Arrays.asList(helpCommand, reload, cpCreateCommand, cpEditCommand, cpGiveCommand, cpRemoveCommand, selectorCommand, mineBlockCreateCommand, apperancesResetCommand,
				renevalDateResetCommand, timeLeftCommand, adminGui, renevalDateCreateCommand, renevalDateDeleteCommand, renevalDateSetCommand, generatorCommand);
	}

	public Pair<AbstractCommand, Boolean> getCommand(String mainCommand) {
		int maxMatch = 0;
		boolean matchInMain = true;
		AbstractCommand matchedCommand = getMainCommand();
		String[] mainArgs = mainCommand.split(" ");
		for(AbstractCommand command : commands) {
			int mainMatch = 0, alternativeMatch = 0;
			String[] commandArgs = command.getMainArguments();
			String[] alternativeArgs = command.getAlternativeArguments();
			for(int i = 0; i < mainArgs.length; i++) {
				if(i < commandArgs.length) {
					if(mainArgs[i].equalsIgnoreCase(commandArgs[i]))
						mainMatch++;
				}
			}
			if(mainMatch > maxMatch) {
				maxMatch = mainMatch;
				matchedCommand = command;
				matchInMain = true;
			}
			if(alternativeArgs != null) {
				for(int i = 0; i < mainArgs.length; i++) {
					if(i < alternativeArgs.length) {
						if(mainArgs[i].equalsIgnoreCase(alternativeArgs[i]))
							alternativeMatch++;
					}
				}
				if(alternativeMatch > maxMatch) {
					maxMatch = alternativeMatch;
					matchedCommand = command;
					matchInMain = false;
				}
			}
		}
		return Pair.of(matchedCommand, matchInMain);
	}
	
	public List<AbstractCommand> getCommands() {
		return commands;
	}
	
	
	private AbstractCommand getMainCommand() {
		return mainCommand;
	}
}
