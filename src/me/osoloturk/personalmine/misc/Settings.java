package me.osoloturk.personalmine.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.utils.Utils;

public enum Settings {
	ERROR_PERMISSION("error.permission"),
	ERROR_CONSOLE("error.console"),
	ERROR_CHANCEPACK_ISNULL("error.chancepack-notfound"),
	ERROR_CHANCEPACK_NOTNULL("error.chancepack-already-created"),
	ERROR_PLAYER_ISNULL("error.player-notfound"),
	ERROR_PLAYER_CHANCEPACK_ALREADY_HAVE("error.player-chancepack-already-have"),
	ERROR_PLAYER_CHANCEPACK_NOT_HAVE("error.player-chancepack-not-have"),
	ERROR_PARSE_INTEGER("error.parse-integer"),
	ERROR_MATERIAL_NOTFOUND("error.material-notfound"),
	ERROR_MINEBLOCK_NOTFOUND("error.mineblock-notfound"),
	ERROR_MINEBLOCK_ALREADY_HAVE("error.mineblock-already-have"),
	ERROR_POS_IS_EMPTY("error.pos-is-empty"),
	ERROR_RENEWALDATE_ALREADYCREATED("error.renewaldate-already-created"),
	ERROR_RENEVALDATE_NOT_FOUND("error.renewaldate-not-found"),
	ERROR_REQUIRED_MONEY("error.money-required"),
	ERROR_REQUIRED_EXP("error.exp-required"),
	ERROR_GENERATOR_LISTENER_DISABLED("error.generator-disabled"),
	
	ARGUMENTS_RELOAD("command-arguments.reload"),
	ARGUMENTS_CHANCEPACK_GIVE("command-arguments.chancepacks-give"),
	ARGUMENTS_CHANCEPACK_REMOVE("command-arguments.chancepacks-remove"),
	ARGUMENTS_CHANCEPACK_EDIT("command-arguments.chancepacks-edit"),
	ARGUMENTS_CHANCEPACK_CREATE("command-arguments.chancepacks-create"),
	ARGUMENTS_SELECTOR("command-arguments.selector"),
	ARGUMENTS_MINEBLOCK_CREATE("command-arguments.mineblock-create"),
	ARGUMENTS_RENEWALDATE_RELOAD("command-arguments.renewaldate-reload"),
	ARGUMENTS_RENEWALDATE_SET("command-arguments.renewaldate-set"),
	ARGUMENTS_RENEWALDATE_CREATE("command-arguments.renewaldate-create"),
	ARGUMENTS_RENEWALDATE_DELETE("command-arguments.renewaldate-delete"),
	ARGUMENTS_TIMELEFT("command-arguments.timeleft"),
	ARGUMENTS_APPEARANCES_RESET("command-arguments.appearances-reset"),
	ARGUMENTS_GENERATOR_GIVE("command-arguments.generator-give"),
	ARGUMENTS_HELPS("command-arguments.helps"),
	ARGUMENTS_ADMIN("command-arguments.admin"),
	
	USAGE_RELOAD("usage.reload"),
	USAGE_CHANCEPACK_GIVE("usage.chancepacks-give"),
	USAGE_CHANCEPACK_REMOVE("usage.chancepacks-remove"),
	USAGE_CHANCEPACK_EDIT("usage.chancepacks-edit"),
	USAGE_CHANCEPACK_CREATE("usage.chancepacks-create"),
	USAGE_SELECTOR("usage.selector"),
	USAGE_MINEBLOCK_CREATE("usage.mineblock-create"),
	USAGE_RENEWALDATE_RELOAD("usage.renewaldate-reload"),
	USAGE_RENEWALDATE_CREATE("usage.renewaldate-create"),
	USAGE_RENEWALDATE_DELETE("usage.renewaldate-delete"),
	USAGE_RENEWALDATE_SET("usage.renewaldate-set"),
	USAGE_TIMELEFT("usage.timeleft"),
	USAGE_APPEARANCES_RESET("usage.appearances-reset"),
	USAGE_HELPS("usage.helps"),
	USAGE_GENERATOR_GIVE("usage.generator-give"),
	USAGE_MAIN_COMMAND("usage.main-command"),
	
	BLOCK_BLACKLIST_ENABLED("block-black-list.enabled"),
	BLOCK_BLACKLIST_WORLD_ROOT("block-black-list.worlds"),
	
	REGION_BREAK_AREA("region-break-area"),
	
	FLY_PASS_IN_MINEBLOCK("fly-pass"),
	
	DEBUG("debug-mode"),
	
	GUI_UNIVERSAL_ELEMENTS_BLOCK("gui.universal-contents.block"),
	GUI_UNIVERSAL_ELEMENTS_COORD_FILTER("gui.universal-contents.coord-filter"),	
	GUI_UNIVERSAL_ELEMENTS_MODE_CHANCEPACK("gui.universal-contents.mode.chance-pack"),
	GUI_UNIVERSAL_ELEMENTS_MODE_RENEWALDATE("gui.universal-contents.mode.renewal-date"),
	GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_ACTIVE("gui.universal-contents.active-chancepack"),
	GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_CAN_BUY("gui.universal-contents.can-buy-chancepack"),
	GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_DEACTIVE("gui.universal-contents.deactive-chancepack"),
	GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_ADMIN("gui.universal-contents.admin-chancepack"),
	GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_ACTIVE("gui.universal-contents.active-renewal-date"),
	GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_DEACTIVE("gui.universal-contents.deactive-renewal-date"),
	GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_ADMIN("gui.universal-contents.admin-renewal-date"),
	GUI_UNIVERSAL_ELEMENTS_OPENWORLDFILTER_ACTIVE("gui.universal-contents.active-open-world-filter"),
	GUI_UNIVERSAL_ELEMENTS_OPENWORLDFILTER_DEACTIVE("gui.universal-contents.deactive-open-world-filter"),
	
	MESSAGE_CHANCEPACK_CREATE("messages.chancepack-create"),
	MESSAGE_CHANCEPACK_GIVE_ADMIN("messages.chancepack-give.executor"),
	MESSAGE_CHANCEPACK_GIVE_TARGET("messages.chancepack-give.target"),
	MESSAGE_CHANCEPACK_REMOVE_ADMIN("messages.chancepack-remove.executor"),
	MESSAGE_CHANCEPACK_REMOVE_TARGET("messages.chancepack-remove.target"),
	MESSAGE_CHANCEPACK_EDIT_ADD("messages.chancepack-edit-add.executor"),
	MESSAGE_CHANCEPACK_EDIT_REMOVE("messages.chancepack-edit-remove.executor"),
	MESSAGE_CHANCEPACK_EDIT_MONEY("messages.chancepack-edit-money.executor"),
	MESSAGE_CHANCEPACK_EDIT_EXP("messages.chancepack-edit-exp.executor"),
	MESSAGE_APPEARANCES_RESET_ADMIN("messages.appearances-reset.executor"),
	MESSAGE_APPEARANCES_RESET_TARGET("messages.appearances-reset.target"),
	MESSAGE_RENEWAL_DATE_RESET_ADMIN("messages.renewal-date-reset.executor"),
	MESSAGE_RENEWAL_DATE_RESET_TARGET("messages.renewal-date-reset.target"),
	MESSAGE_GENERATOR_GIVE_ADMIN("messages.generator-give.executor"),
	MESSAGE_GENERATOR_GIVE_TARGET("messages.generator-give.target"),
	MESSAGE_SELECTOR_GIVE_SINGLE("messages.selector-give.single"),
	MESSAGE_SELECTOR_GIVE_MULTI("messages.selector-give.multi"),
	MESSAGE_APPEARANCES_RESET_NATURALLY("messages.appearances-reset-naturally"),
	MESSAGE_SELECTOR_SELECTED_MULTI("messages.selector-selected-multi"),
	MESSAGE_SELECTOR_REMOVE_SINGLE("messages.selector-remove-single"),
	MESSAGE_SELECTOR_CREATE_SINGLE("messages.selector-create-single"),
	MESSAGE_SELECTOR_CREATE_MULTI("messages.selector-create-multi"),
	MESSAGE_TIMELEFT("messages.timeleft"),
	MESSAGE_RELOAD("messages.reload"),
	MESSAGE_SELECTOR_REMOVE_MULTI_QUESTION("messages.selector-remove-multi.question"),
	MESSAGE_SELECTOR_REMOVE_MULTI_SUCCES("messages.selector-remove-multi.succes-message"),
	MESSAGE_SELECTOR_REMOVE_MULTI_CANCEL("messages.selector-remove-multi.cancel-message"),
	MESSAGE_CHANCEPACK_MODE_CONVERT("messages.chancepack-mode-convert"),
	MESSAGE_RENEWALDATE_CREATE("messages.renewaldate-succes-create"),
	MESSAGE_RENEWALDATE_SET("messages.renewaldate-succes-set"),
	MESSAGE_CHANCEPACK_BUY_QUESTION("messages.chancepack-buy.question"),
	MESSAGE_CHANCEPACK_BUY_SUCCES("messages.chancepack-buy.succes"),
	MESSAGE_CHANCEPACK_BUY_CANCEL("messages.chancepack-buy.cancel"),
	
	ACTIVE_WORLDS("active-worlds"),
	
	GENERATOR_ENABLED("generators.enabled"),
	GENERATOR_SKYBLOCK_SUPPORT("generators.skyblock-support"),
	GENERATOR_CONSOLE_LOG("generators.console-log.enabled"),
	GENERATOR_BLOCK("generators"),
	GENERATOR_RECIPE_ENABLED("generators.recipe.enabled"),
	GENERATOR_RECIPE_ITEMS("generators.recipe.items"),
	GENERATOR_DELAY("generators.delay"),
	
	DATE_FORMAT("date-format"),
	
	RENEWAL_DATES_CHECK_TIMER("renewal-dates-check-timer"),
	
	AUTO_REFILL_APPEARANCES_AFTER_CHANCEPACK_BOUGHT("auto-refill-appearances-after-chancepack-bought"),
	
	OPEN_WORLD_ENABLED("open-world-filter.enabled"),
	OPEN_WORLD_FILTER_BLOCKS("open-world-filter.filter-blocks"),
	OPEN_WORLD_CHUNKS("open-world-filter.chunk-limits"),

	MINE_BLOCK_SELECTOR_PERMISSION("mine-block-selector.permission"),
	MINE_BLOCK_SELECTOR_SINGLE("mine-block-selector.single"),
	MINE_BLOCK_SELECTOR_MULTI("mine-block-selector.multi"),
	
	CHANCES_BASE("chances"),
	RENEWAL_DATE_BASE("renewal-dates"),
	
	DATABASE_MODE("database.mode"),
	DATABASE_COMPRESS("database.compress"),
	MYSQL_USER("database.for_mysql.user"),
	MYSQL_PASSWORD("database.for_mysql.password"),
	MYSQL_HOST("database.for_mysql.host"),
	MYSQL_DATABASE("database.for_mysql.database"),
	MYSQL_PORT("database.for_mysql.port");
	
	private static FileConfiguration config;
	private final String path;
	
	private Settings(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public static void setConfig(FileConfiguration config) {
		Settings.config = config;
	}
	
	public boolean getBoolean() {
		return config == null ? null : config.getBoolean(path);
	}
	
	public double getDouble() {
		return config == null ? null : config.getDouble(path);
	}
	
	public int getInt() {
		return config == null ? null : config.getInt(path);
	}
	
	public long getLong() {
		return config == null ? null : config.getLong(path);
	}
	
	public String getString() {
		return config == null ? null : Utils.color(config.getString(path));
	}
	
	public List<String> getStringList() {
		return config == null ? null : Utils.color(config.getStringList(path));
	}
	
	public Material getAsMaterial() {
		return config == null ? null : Material.valueOf(config.getString(path));
	}
	
	public Set<String> getConfigurationSection(boolean keys) {
		return config == null ? null : config.getConfigurationSection(path).getKeys(keys);
	}
	
	public String replaceString(String... replaces) {
		String original = getString();
		if(original == null)
			return original;
		for(int i = -1; ++i < replaces.length;) {
			original = original.replace(replaces[i], Utils.color(replaces[++i]));
		}
		return original;
	}
	
	public List<String> replaceListString(String... replaces) {
		List<String> original = getStringList();
		List<String> list = new ArrayList<>();
		for(String line : original) {
			for(int i = -1; ++i < replaces.length;) {
				line = line.replace(replaces[i], Utils.color(replaces[++i]));
			}
			list.add(line);
		}
		return list;
	}
	
	public void send(CommandSender sender, List<String> replaces) {
		String[] messages = (replaces != null ? applyReplaces(getString(), replaces) : getString()).split("\\|\\|");
		if(!(sender instanceof Player)) {
			String message = "";
			switch(messages[0].toUpperCase(Locale.ENGLISH)) {
			case "TITLE":
				message = messages[1] + "-" + messages[2];
				break;
			case "CHAT":
			case "ACTIONBAR":
			default:
				message = messages[1];
				break;
			}
			sender.sendMessage(message);
			return;
		}
		switch(messages[0].toUpperCase(Locale.ENGLISH)) {
		case "TITLE":
			Utils.sendTitle((Player) sender, messages[1], messages[2], Utils.getInt(messages[3], 10), Utils.getInt(messages[4], 70), Utils.getInt(messages[5], 20));
			break;
		case "CHAT":
			sender.sendMessage(messages[1]);
			break;
		case "ACTIONBAR":
			Utils.sendActionbar((Player) sender, messages[1]);
			break;
		default:
			sender.sendMessage(messages[1]);
			break;
		}
	}
	
	private String applyReplaces(String text, List<String> replaces) {
		for(int i = 0; i < replaces.size(); i++)
			text = text.replace(replaces.get(i), replaces.get(++i));
		return text;
	}
	
	public void sendList(CommandSender sender) {
		getStringList().forEach(sender::sendMessage);
	}
	
	@Override
	public String toString() {
		return config == null ? null : config.getString(path);
	}
	
	public void sendPlayer(CommandSender sender) {
		sender.sendMessage(getString());
	}
	
	public void sendPlayerASList(CommandSender sender) {
		getStringList().forEach(line -> sender.sendMessage(line));
	}
	
}
