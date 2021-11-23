package me.osoloturk.personalmine.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Utils;

public class FileManager {
	private final APM instance;
	private File configFile, upgradeNotes;
	private FileConfiguration config, upgradeFile;
	
	public FileManager(APM instance) {
		this.instance = instance;
	}
	
	private void checkUpgradedPath() {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			public void checkUpgradeFile() {
				if(!upgradeNotes.exists()) {
					instance.saveResource("upgrade-information.yml", true);
				} else {
					upgradeNotes.delete();
					try {
						upgradeNotes.createNewFile();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				if(upgradeFile == null) {
					upgradeFile = new YamlConfiguration();
					try {
						upgradeFile.load(upgradeNotes);
					} catch(IOException | InvalidConfigurationException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void run() {
				YamlConfiguration configResource = YamlConfiguration.loadConfiguration(new InputStreamReader(instance.getResource("config.yml")));
				for(String path : config.getConfigurationSection("").getKeys(true)) {
					if(path.startsWith(Settings.CHANCES_BASE.getPath()) || path.startsWith(Settings.RENEWAL_DATE_BASE.getPath()) || (path.startsWith("gui.") && path.contains("compulsory-content")))
						continue;
					if(!configResource.isSet(path)) {
						checkUpgradeFile();
						upgradeFile.set("removed-from-config." + path, config.get(path));
						config.set(path, null);
					}
				}
				
				for(String path : configResource.getConfigurationSection("").getKeys(true)) {
					if(path.startsWith(Settings.CHANCES_BASE.getPath()) || path.startsWith(Settings.RENEWAL_DATE_BASE.getPath()) || (path.startsWith("gui.") && path.contains("compulsory-content")))
						continue;
					if(!config.isSet(path)) {
						config.set(path, configResource.get(path));
						checkUpgradeFile();
						upgradeFile.set("added-to-config." + path, configResource.get(path));
					}
				}
				if(upgradeFile != null) {
					String oldConfigVersion = config.getString("versions.config");
					String newConfigVersion = configResource.getString("versions.config");
					upgradeFile.set("previous-config-version", oldConfigVersion);
					upgradeFile.set("current-config-version", newConfigVersion);
					config.set("versions.config", newConfigVersion);
					instance.getLogger().info("Please Check upgrade-information.yml in ApiaPersonelMine folder new configurations stay here");
					try {
						upgradeFile.save(upgradeNotes);
						config.save(configFile);
					} catch(IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		});
		
	}
	
	public void createFiles() {
		if(!instance.getDataFolder().exists()) {
			if(Utils.isLegacy()) {
				instance.saveResource("config_legacy.yml", true);
				new File(instance.getDataFolder(), "config_legacy.yml").renameTo(new File(instance.getDataFolder(), "config.yml"));
			} else
				instance.saveResource("config.yml", true);
		}
		upgradeNotes = new File(instance.getDataFolder(), "upgrade-information.yml");
		configFile = new File(instance.getDataFolder(), "config.yml");
		if(!configFile.exists()) {
			if(Utils.isLegacy()) {
				instance.saveResource("config_legacy.yml", true);
				new File(instance.getDataFolder(), "config_legacy.yml").renameTo(new File(instance.getDataFolder(), "config.yml"));
			} else
				instance.saveResource("config.yml", true);
			instance.getLogger().info("Created new Config.yml");
		}
		config = instance.getConfig();
		instance.getLogger().info("Loading all files.");
		checkUpgradedPath();
	}
	
}
