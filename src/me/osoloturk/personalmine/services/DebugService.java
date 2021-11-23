package me.osoloturk.personalmine.services;

import java.util.List;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;

public class DebugService {
	final APM instance;
	final boolean debugMode;
	
	public DebugService(final APM instance) {
		this.instance = instance;
		debugMode = Settings.DEBUG.getBoolean();
		if(debugMode) {
			instance.getLogger().warning("Your Debug Mode is Enabled. If you don't need this then turn off");
		}
	}
	
	public void debug(final String message) {
		if(debugMode) {
			instance.getLogger().warning("[Debug] " + message);
		}
	}
	
	public void debug(final List<String> messages) {
		messages.forEach(this::debug);
	}
}
