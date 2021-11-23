package me.osoloturk.personalmine;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.osoloturk.personalmine.databases.MySQLDatabase;
import me.osoloturk.personalmine.databases.SQLiteDatabase;
import me.osoloturk.personalmine.gui.GuiManager;
import me.osoloturk.personalmine.gui_infrastructure.CustomInventoryListener;
import me.osoloturk.personalmine.protect.Protect;
import me.osoloturk.personalmine.commands.CommandNavigator;
import me.osoloturk.personalmine.databases.IDatabase;
import me.osoloturk.personalmine.listeners.BlockBreakListener;
import me.osoloturk.personalmine.listeners.ChunkLoadListener;
import me.osoloturk.personalmine.listeners.FlyListener;
import me.osoloturk.personalmine.listeners.PacketListener;
import me.osoloturk.personalmine.listeners.PlayerJQListener;
import me.osoloturk.personalmine.listeners.SelectorListener;
import me.osoloturk.personalmine.managers.ChanceManager;
import me.osoloturk.personalmine.managers.CommandManager;
import me.osoloturk.personalmine.managers.FileManager;
import me.osoloturk.personalmine.managers.MineBlockManager;
import me.osoloturk.personalmine.managers.PlayerManager;
import me.osoloturk.personalmine.managers.SelectorManager;
import me.osoloturk.personalmine.misc.OpenWorldFilter;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.runnables.RenevalDateResetRunnable;
import me.osoloturk.personalmine.services.DebugService;
import me.osoloturk.personalmine.services.SoundService;
import me.osoloturk.personalmine.utils.Metrics;
import me.osoloturk.personalmine.hooks.skyblocks.*;
import me.osoloturk.personalmine.listeners.GeneratorListener;
import net.milkbowl.vault.economy.Economy;

public class APM extends JavaPlugin {
	
	private static APM instance;
	private ISkyBlock skyblockHook;
	private Economy econ;
	private CommandManager commandManager;
	private PacketListener packetListener;
	private OpenWorldFilter openWorldFilter;
	private ChunkLoadListener openWorldFilterListener;
	private FlyListener flyModeListener;
	private GeneratorListener generatorListener;
	private MineBlockManager mineBlockManager;
	private ChanceManager chanceManager;
	private PlayerManager playerManager;
	private GuiManager guiManager;
	private SelectorManager selectorManager;
	private SoundService soundService;
	private DebugService debugService;
	private IDatabase database;
	private FileManager fileManager;
	
	public void onEnable() {
		instance = this;
		getCommand("apm").setExecutor(new CommandNavigator(instance));
		loadSystem(false);
		getServer().getPluginManager().registerEvents(new BlockBreakListener(instance), instance);
		getServer().getPluginManager().registerEvents(new PlayerJQListener(instance), instance);
		getServer().getPluginManager().registerEvents(new SelectorListener(instance), instance);
		getServer().getPluginManager().registerEvents(new CustomInventoryListener(instance), instance);
		getServer().getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				getDatabase().saveMinePlayers(getPlayerManager().getMinePlayersWithUUID());
				getDatabase().saveOpenWorldScannedChunks(getOpenWorldFilter().getScannedChunks());
				getLogger().info("Database	 Saved!");
			}
		}, 600000, 600000);
	}
	
	public void onDisable() {
		final Thread closeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				getDatabase().saveMinePlayers(getPlayerManager().getMinePlayersWithUUID());
				getDatabase().saveOpenWorldScannedChunks(getOpenWorldFilter().getScannedChunks());
			}
		}, "APM_Close_Thread");
		closeThread.setPriority(Thread.MIN_PRIORITY);
		closeThread.setDaemon(true);
		closeThread.start();
	}
	
	public void loadSystem(boolean reLoad) {
		getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			fileManager = new FileManager(instance);
			fileManager.createFiles();
			if(!setupEconomy()) {
				getLogger().warning("Economy plugin not found! - You can not use chancepack buy system");
			}
			reloadConfig();
			Settings.setConfig(getConfig());
			switch(Settings.DATABASE_MODE.getString().toUpperCase()) {
			case "SQL":
				database = new SQLiteDatabase(instance);
				break;
			case "MYSQL":
				database = new MySQLDatabase();
				break;
			default:
				getLogger().severe("Database ERROR. Make sure your database type is sqlite or mysql. Disabling.");
				setEnabled(false);
				return;
			}
			if(reLoad) {
				guiManager.init();
				chanceManager.loadChances();
				selectorManager.loadSelectors();
				openWorldFilter.loadFilters();
				soundService = new SoundService(instance);
				debugService = new DebugService(instance);
			} else {
				packetListener = new PacketListener(instance);
				openWorldFilter = new OpenWorldFilter(instance);
				guiManager = new GuiManager(instance);
				commandManager = new CommandManager(instance);
				playerManager = new PlayerManager(instance);
				getServer().getScheduler().runTask(instance, () -> mineBlockManager = new MineBlockManager(instance));
				chanceManager = new ChanceManager(instance);
				selectorManager = new SelectorManager(instance);
				soundService = new SoundService(instance);
				debugService = new DebugService(instance);
				try {
					if(!Protect.auth()) {
						instance = null;
						setEnabled(false);
						return;
					}
					Metrics metrics = new Metrics(instance, 10755);
					metrics.addCustomChart(new Metrics.SimplePie("using_open_world_filter", () -> Settings.OPEN_WORLD_ENABLED.getBoolean() + ""));
					metrics.addCustomChart(new Metrics.SimplePie("using_block_black_list", () -> Settings.BLOCK_BLACKLIST_ENABLED.getBoolean() + ""));
					metrics.addCustomChart(new Metrics.SingleLineChart("chance_pack_count", () -> Settings.CHANCES_BASE.getConfigurationSection(false).size()));
					metrics.addCustomChart(new Metrics.SimplePie("using_mysql", () -> Settings.DATABASE_MODE.getString().equalsIgnoreCase("MySQL") + ""));
					metrics.addCustomChart(new Metrics.SimplePie("using_compress", () -> Settings.DATABASE_COMPRESS.getBoolean() + ""));
				} catch(Exception e) {
					getLogger().info("You have not internet connection");
					getLogger().info("APM working offline mode");
				}
			}
			if(Settings.GENERATOR_ENABLED.getBoolean()) {
				if(Settings.GENERATOR_SKYBLOCK_SUPPORT.getBoolean())
					hookSkyBlockPlugin();
				if(generatorListener == null)
					getServer().getPluginManager().registerEvents((generatorListener = new GeneratorListener(instance)), instance);
			} else if(generatorListener != null) {
				HandlerList.unregisterAll(generatorListener);
				generatorListener = null;
			}
			if(Settings.FLY_PASS_IN_MINEBLOCK.getBoolean()) {
				getLogger().info("Fly Pass System Enabled!");
				if(flyModeListener == null)
					getServer().getPluginManager().registerEvents((flyModeListener = new FlyListener(instance)), instance);
			} else if(flyModeListener != null) {
				HandlerList.unregisterAll(flyModeListener);
				flyModeListener = null;
			}
			if(Settings.OPEN_WORLD_ENABLED.getBoolean() && !Settings.OPEN_WORLD_CHUNKS.getConfigurationSection(false).isEmpty()) {
				getLogger().info("Open World Filter System Enabled!");
				if(openWorldFilterListener == null)
					getServer().getPluginManager().registerEvents((openWorldFilterListener = new ChunkLoadListener(instance)), instance);
			} else if(openWorldFilter != null) {
				HandlerList.unregisterAll(openWorldFilterListener);
				openWorldFilterListener = null;
			}
			if(!reLoad)
				new RenevalDateResetRunnable(instance).runTaskTimerAsynchronously(instance, 0, Settings.RENEWAL_DATES_CHECK_TIMER.getInt() * 20 * 60);
		});
	}
	
	private boolean setupEconomy() {
		if(Bukkit.getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
		
	}
	
	public void disablePlugin() {
		getLogger().severe("Please contact me.");
		getLogger().severe("Discord: http://bit.ly/ApiaTeamSupport");
		getServer().getScheduler().cancelTasks(instance);
		getServer().getPluginManager().disablePlugin(instance);
	}
	
	private void hookSkyBlockPlugin() {
		PluginManager pluginManager = getServer().getPluginManager();
		if(pluginManager.isPluginEnabled("AcidIsland")) {
			skyblockHook = new AcidIslandHook(instance);
		} else if(pluginManager.isPluginEnabled("AdvancedRealm")) {
			skyblockHook = new AdvancedRealmHook(instance);
		} else if(pluginManager.isPluginEnabled("ASkyBlock")) {
			skyblockHook = new ASkyBlockHook(instance);
		} else if(pluginManager.isPluginEnabled("BentoBox")) {
			skyblockHook = new BentoBoxHook(instance);
		} else if(pluginManager.isPluginEnabled("FabledSkyBlock")) {
			skyblockHook = new FabledSkyBlockHook(instance);
		} else if(pluginManager.isPluginEnabled("IridiumSkyblock")) {
			skyblockHook = new IridiumSkyblockHook(instance);
		} else if(pluginManager.isPluginEnabled("SuperiorSkyblock2")) {
			skyblockHook = new SuperiorSkyblockHook(instance);
		} else if(pluginManager.isPluginEnabled("PlotSquared")) {
			skyblockHook = new PlotSquaredHook();
		}
		if(skyblockHook == null) {
			getLogger().warning("No supported skyblock plugins found");
			return;
		}
		if(!Settings.ACTIVE_WORLDS.getStringList().contains(skyblockHook.getSkyBlockWorld())) {
			getLogger().warning("Your SkyBlock world is not in the active worlds of ApiaPersonalMine!");
		}
		getLogger().info("Hooked to " + skyblockHook.getClass().getSimpleName().replace("Hook", ""));
		return;
	}
	
	public File getPluginFile() {
		return getFile();
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public PacketListener getPacketListener() {
		return packetListener;
	}
	
	public IDatabase getDatabase() {
		return database;
	}
	
	public PlayerManager getPlayerManager() {
		return playerManager;
	}
	
	public OpenWorldFilter getOpenWorldFilter() {
		return openWorldFilter;
	}
	
	public MineBlockManager getMineBlockManager() {
		return mineBlockManager;
	}
	
	public ChanceManager getChanceManager() {
		return chanceManager;
	}
	
	public GuiManager getGuiManager() {
		return guiManager;
	}
	
	public SelectorManager getSelectorManager() {
		return selectorManager;
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
	public boolean isActiveOpenWorldFilter() {
		return openWorldFilterListener != null;
	}
	
	public ISkyBlock getSkyBlokHook() {
		return skyblockHook;
	}
	
	public GeneratorListener getGeneratorListener() {
		return generatorListener;
	}
	
	public static APM getInstance() {
		return instance;
	}
	
	public SoundService getSoundService() {
		return soundService;
	}

	public DebugService getDebugService() {
		return debugService;
	}
}
