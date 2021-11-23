package me.osoloturk.personalmine.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.gui.GuiManager.UniversalContents;
import me.osoloturk.personalmine.gui_infrastructure.ClickAction;
import me.osoloturk.personalmine.gui_infrastructure.CloseAction;
import me.osoloturk.personalmine.gui_infrastructure.CustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.InventoryIcon;
import me.osoloturk.personalmine.gui_infrastructure.SimpleCustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.Size;
import me.osoloturk.personalmine.misc.ChancePack;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class UserPage extends SimpleCustomInventory {
	
	private final APM instance;
	private final String path = "gui.user-gui";
	private final List<Integer> slots;
	private boolean showChancePacks = true;
	private int maxPlace = 0;
	private boolean notElements = false;
	
	public UserPage(APM instance) {
		super(instance);
		this.instance = instance;
		addCloseAction(new CloseAction() {
			@Override
			public void handle(CustomInventory customInventory, Player player, InventoryCloseEvent event) {
				instance.getPlayerManager().getMinePlayer(player.getUniqueId()).calculateMaxChance();
			}
		});
		slots = instance.getGuiManager().calculateSlots(path + ".item-slots");
		setSize(Size.fit(instance.getConfig().getInt(path + ".rows")));
		setTitle(Utils.color(instance.getConfig().getString(path + ".title")));
		if(instance.getConfig().isSet(path + ".optional-content")) {
			for(String key : instance.getConfig().getConfigurationSection(path + ".optional-content").getKeys(false)) {
				String spesificPath = path + ".optional-content." + key;
				InventoryIcon icon = new InventoryIcon(instance.getGuiManager().createItem(spesificPath));
				instance.getGuiManager().calculateSlots(spesificPath + ".slots").forEach(slot -> setIcon(slot, icon, false));
			}
		}
		if(instance.getConfig().isSet(path + ".compulsory-content")) {
			for(String key : instance.getConfig().getConfigurationSection(path + ".compulsory-content").getKeys(false)) {
				String spesificPath = path + ".compulsory-content." + key;
				setIcon(instance.getConfig().getInt(spesificPath + ".slot"), new InventoryIcon(instance.getGuiManager().createItem(spesificPath)), true);
			}
		}
		getIcon(instance.getConfig().getInt(path + ".compulsory-content.help.slot")).setClickAction(new ClickAction() {
			@Override
			public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
				instance.getServer().getScheduler().runTask(instance, () -> instance.getServer().dispatchCommand(player, "apm helps"));
			}
		});
		getIcon(instance.getConfig().getInt(path + ".compulsory-content.chancepack-buy.slot")).setClickAction(new ClickAction() {
			@Override
			public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
				instance.getGuiManager().openChancePackBuyPage(player);
			}
		});
	}
	
	public void openUserPage(Player player, int page) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			InventoryIcon modeIcon = getIcon(instance.getConfig().getInt(path + ".compulsory-content.convert-list-mode.slot"));
			modeIcon.setLore(instance.getConfig().getStringList(path + ".compulsory-content.convert-list-mode.block.lore").stream()
					.map(line -> Utils.color(line.replace("%current_mode%",
							(showChancePacks ? Settings.GUI_UNIVERSAL_ELEMENTS_MODE_CHANCEPACK.getString() : Settings.GUI_UNIVERSAL_ELEMENTS_MODE_RENEWALDATE.getString()))))
					.collect(Collectors.toList()));
			modeIcon.setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					showChancePacks = !showChancePacks;
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					openUserPage(player, page);
				}
			});
			int elementLength = slots.size();
			int element = 0;
			int slot = 0;
			MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(player.getUniqueId());
			if(showChancePacks) {
				fillPageButtons(page, elementLength, mPlayer.getChancePacks().size());
				for(Entry<String, Boolean> entry : new HashMap<>(mPlayer.getChancePacks()).entrySet()) {
					if(element >= ((page - 1) * elementLength) && element < (page * elementLength)) {
						ChancePack pack = instance.getChanceManager().getChancePack(entry.getKey());
						if(pack == null) {
							mPlayer.removeChancePack(entry.getKey());
							continue;
						}
						InventoryIcon icon = instance.getGuiManager().getUniverselContent(UniversalContents.CHANCE_PACK_WITHOUT_COSTS).getKey().clone();
						List<String> lore = icon.getLore();
						List<String> blocks = new ArrayList<>();
						for(Entry<Pair<Material, Byte>, Integer> blockEntry : pack.getChances().entrySet()) {
							blocks.add(Settings.GUI_UNIVERSAL_ELEMENTS_BLOCK.getString()
									.replace("%blockname%", blockEntry.getKey().getKey().name() + (blockEntry.getKey().getValue() != 0 ? (":" + blockEntry.getKey().getValue()) : ""))
									.replace("%chance%", "" + blockEntry.getValue()));
						}
						for(int line = 0; line < lore.size(); line++) {
							lore.set(line, lore.get(line).replace("%packname%", entry.getKey().toUpperCase().replaceAll("_", " ")).replace("%cost_exp%", "" + pack.getCostExp())
									.replace("%cost_money%", "" + pack.getCostMoney()).replace("%status%",
											(entry.getValue() ? Settings.GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_ACTIVE.getString() : Settings.GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_DEACTIVE.getString())));
							if(lore.get(line).contains("%blocks%")) {
								lore.remove(line);
								lore.addAll(line, blocks);
							}
							if(lore.get(line).contains("%description%")) {
								lore.remove(line);
								lore.addAll(line, pack.getDescription());
							}
						}
						icon.setLore(lore);
						if(entry.getValue())
							icon.glow();
						icon.setClickAction(new ClickAction() {
							@Override
							public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
								mPlayer.setChancePack(entry.getKey(), !entry.getValue());
								Settings.MESSAGE_CHANCEPACK_MODE_CONVERT.send(player, null);
								instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
								openUserPage(player, page);
							}
						});
						setIcon(slots.get(slot++), icon, true);
					}
					element++;
				}
			} else {
				Set<String> permissions = Settings.RENEWAL_DATE_BASE.getConfigurationSection(false);
				fillPageButtons(page, elementLength, permissions.size());
				long minValue = Integer.MAX_VALUE;
				int minValueSlot = -1;
				for(String permission : permissions) {
					if(!player.hasPermission(permission))
						continue;
					long renevalDate = instance.getConfig().getLong(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission);
					if(minValue > renevalDate)
						minValue = renevalDate;
					if(element >= ((page - 1) * elementLength) && element < (page * elementLength)) {
						InventoryIcon icon = instance.getGuiManager().getUniverselContent(UniversalContents.RENEVAL_DATE).getKey().clone();
						icon.setLore(
								instance.getGuiManager().getUniverselContent(UniversalContents.RENEVAL_DATE).getKey().getLore().stream()
										.map(line -> line.replaceAll("%permission%", permission).replaceAll("%status%", Settings.GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_DEACTIVE.getString())
												.replaceAll("%renewaldate%", "" + (instance.getConfig().getInt(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission))))
										.collect(Collectors.toList()));
						if(minValue == renevalDate)
							minValueSlot = slots.get(slot);
						setIcon(slots.get(slot++), icon, true);
					}
					element++;
				}
				if(minValueSlot != -1) {
					InventoryIcon icon = getIcon(minValueSlot);
					icon.glow();
					icon.setLore(icon.getLore().stream()
							.map(line -> line.replaceAll(Settings.GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_DEACTIVE.getString(), Settings.GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_ACTIVE.getString()))
							.collect(Collectors.toList()));
					setIcon(minValueSlot, icon, true);
				}
			}
			InventoryIcon trace = instance.getGuiManager().getUniverselContent(showChancePacks ? UniversalContents.CHANCE_PACK_WITHOUT_COSTS : UniversalContents.RENEVAL_DATE).getValue();
			for(int index = 0; index < slots.size(); index++) {
				if(index > slot - 1 && index <= maxPlace)
					setIcon(slots.get(index), trace, true);
			}
			maxPlace = slot - 1;
			getIcon(instance.getConfig().getInt(path + ".compulsory-content.appearance-information.slot"))
					.setLore(Utils.color(instance.getConfig().getStringList(path + ".compulsory-content.appearance-information.block.lore")).stream()
							.map(line -> line.replace("%chunks%", "" + mPlayer.getTravledChunkSize()).replace("%appearances%", "" + mPlayer.getApperances().size())).collect(Collectors.toList()));
			if(slot == 0) {
				if(!notElements) { // Current gui is empty and before snapshot is not empty
					setIcon(instance.getConfig().getInt("gui.universal-contents.element-not-found.slot"), instance.getGuiManager().getUniverselContent(UniversalContents.ELEMENT_NOT_FOUND).getKey(),
							true);
				}
			} else {
				if(notElements) { // Current gui filled and before snapshot is empty
					int notElementSlot = instance.getConfig().getInt("gui.universal-contents.element-not-found.slot");
					for(int index = 0; index <= slot; index++) { // Last inserted elements have been overwritten with the previous not element icon
						if(slots.get(index) == notElementSlot) {
							notElements = false;
							break;
						}
					}
					if(notElements) { // Place trace of not element icon
						setIcon(notElementSlot, instance.getGuiManager().getUniverselContent(UniversalContents.ELEMENT_NOT_FOUND).getValue(), true);
						notElements = false;
					}
				}
			}
			instance.getServer().getScheduler().runTask(instance, () -> openInventory(player));
		});
	}
	
	public void fillPageButtons(int page, int elementLength, int requiredSize) {
		int positionNext = instance.getConfig().getInt("gui.universal-contents.next-button.slot");
		int positionPrevious = instance.getConfig().getInt("gui.universal-contents.back-button-only-previous-page.slot");
		if(page * elementLength < requiredSize) {
			setIcon(positionNext, instance.getGuiManager().getUniverselContent(UniversalContents.NEXT_BUTTON).getKey(), true);
			getIcon(positionNext).setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					openUserPage(player, page + 1);
				}
			});
		} else
			setIcon(positionNext, instance.getGuiManager().getUniverselContent(UniversalContents.NEXT_BUTTON).getValue(), true);
		if(page > 1) {
			setIcon(positionPrevious, instance.getGuiManager().getUniverselContent(UniversalContents.BACK_BUTTON_WITH_PREVIOUS_PAGE).getKey(), true);
			getIcon(positionPrevious).setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					openUserPage(player, page - 1);
				}
			});
		} else
			setIcon(positionPrevious, instance.getGuiManager().getUniverselContent(UniversalContents.BACK_BUTTON_ONLY_PREVIOUS_PAGE).getValue(), true);
	}
}
