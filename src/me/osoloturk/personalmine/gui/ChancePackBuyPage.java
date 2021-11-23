package me.osoloturk.personalmine.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.gui.GuiManager.UniversalContents;
import me.osoloturk.personalmine.gui_infrastructure.ClickAction;
import me.osoloturk.personalmine.gui_infrastructure.CustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.InventoryIcon;
import me.osoloturk.personalmine.gui_infrastructure.SimpleCustomInventory;
import me.osoloturk.personalmine.gui_infrastructure.Size;
import me.osoloturk.personalmine.misc.ChancePack;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class ChancePackBuyPage extends SimpleCustomInventory {
	
	private final String path = "gui.chancepack-buy";
	private boolean notElements = false;
	private int maxPlace = 0;
	private final List<Integer> slots;
	private final APM instance;
	private final ClickAction cancelBuyAction;
	
	public ChancePackBuyPage(APM instance) {
		super(instance);
		this.instance = instance;
		cancelBuyAction = new ClickAction() {
			@Override
			public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
				Settings.MESSAGE_CHANCEPACK_BUY_CANCEL.send(player, null);
			}
		};
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
	}
	
	public void openChancePackBuy(Player player, int page) {
		if(instance.getEconomy() == null) {
			instance.getLogger().severe("You can not use chancepack buy system because you have not economy system.");
			player.sendMessage("You can not use this system right now!");
			return;
		}
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			int elementLength = slots.size();
			int element = 0;
			int slot = 0;
			MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(player.getUniqueId());
			for(Entry<String, ChancePack> entry : instance.getChanceManager().getChances().entrySet()) {
				if(mPlayer.hasChancePack(entry.getKey()) || !entry.getValue().isSelling() || entry.getValue().isDefaultMode())
					continue;
				if(element >= ((page - 1) * elementLength) && element <= (page * elementLength)) {
					InventoryIcon icon = instance.getGuiManager().getUniverselContent(UniversalContents.CHANCE_PACK_WITH_COSTS).getKey().clone();
					List<String> lore = icon.getLore();
					List<String> blocks = new ArrayList<>();
					ChancePack pack = entry.getValue();
					for(Entry<Pair<Material, Byte>, Integer> blockEntry : pack.getChances().entrySet()) {
						blocks.add(Settings.GUI_UNIVERSAL_ELEMENTS_BLOCK.getString()
								.replace("%blockname%", blockEntry.getKey().getKey().name() + (blockEntry.getKey().getValue() != 0 ? (":" + blockEntry.getKey().getValue()) : ""))
								.replace("%chance%", "" + blockEntry.getValue()));
					}
					for(int line = 0; line < lore.size(); line++) {
						lore.set(line, lore.get(line).replace("%packname%", entry.getKey().toUpperCase().replaceAll("_", " ")).replace("%cost_exp%", "" + pack.getCostExp())
								.replace("%cost_money%", "" + pack.getCostMoney()).replace("%status%", Settings.GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_CAN_BUY.getString()));
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
					List<String> question = Settings.MESSAGE_CHANCEPACK_BUY_QUESTION.getStringList();
					for(int line = 0; line < question.size(); line++) {
						if(question.get(line).contains("%details%")) {
							question.remove(line);
							question.addAll(line, lore);
						}
					}
					icon.setClickAction(new ClickAction() {
						@Override
						public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
							instance.getGuiManager().getYesNoSelector().openSelector(player, null, question, cancelBuyAction, new ClickAction() {
								@Override
								public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
									instance.getChanceManager().buyPack(player, mPlayer, entry);
									instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
									openChancePackBuy(player, page);
								}
							});
						}
					});
					setIcon(slots.get(slot++), icon, true);
				}
				element++;
			}
			fillPageButtons(page, elementLength, instance.getChanceManager().getChances().size());
			InventoryIcon trace = instance.getGuiManager().getUniverselContent(UniversalContents.CHANCE_PACK_WITH_COSTS).getValue();
			for(int index = 0; index < slots.size(); index++) {
				if(index > slot - 1 && index <= maxPlace)
					setIcon(slots.get(index), trace, true);
			}
			maxPlace = slot - 1;
			if(slot == 0) {
				if(!notElements) { // Current gui is empty and before snapshot is not empty
					setIcon(instance.getConfig().getInt("gui.universal-contents.element-not-found.slot"), instance.getGuiManager().getUniverselContent(UniversalContents.ELEMENT_NOT_FOUND).getKey(),
							true);
				}
			} else {
				if(notElements) { // Current gui filled and before snapshot is empty
					for(int index = 0; index <= slot; index++) { // Last inserted elements have been overwritten with the previous not element icon
						if(slots.get(index) == slot) {
							notElements = false;
							break;
						}
					}
					if(notElements) { // Place trace of not element icon
						setIcon(instance.getConfig().getInt("gui.universal-contents.element-not-found.slot"),
								instance.getGuiManager().getUniverselContent(UniversalContents.ELEMENT_NOT_FOUND).getValue(), true);
						notElements = false;
					}
				}
			}
			instance.getServer().getScheduler().runTask(instance, () -> openInventory(player));
		});
	}
	
	private void fillPageButtons(int page, int elementLength, int requiredSize) {
		int positionNext = instance.getConfig().getInt("gui.universal-contents.next-button.slot");
		int positionPrevious = instance.getConfig().getInt("gui.universal-contents.back-button-" + (page > 1 ? "with" : "without") + "-previous-page.slot");
		if(page * elementLength < requiredSize) {
			setIcon(positionNext, instance.getGuiManager().getUniverselContent(UniversalContents.NEXT_BUTTON).getKey(), true);
			getIcon(positionNext).setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					openChancePackBuy(player, page + 1);
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
					if(!shift)
						openChancePackBuy(player, page - 1);
					else
						instance.getGuiManager().openUserDashBoard(player);
				}
			});
		} else {
			setIcon(positionPrevious, instance.getGuiManager().getUniverselContent(UniversalContents.BACK_BUTTON_WITHOUT_PREVIOUS_PAGE).getKey(), true);
			getIcon(positionPrevious).setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					instance.getGuiManager().openUserDashBoard(player);
				}
			});
		}
	}
}
