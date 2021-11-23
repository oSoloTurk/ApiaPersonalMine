
package me.osoloturk.personalmine.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.misc.OpenWorldFilter.ChunkFilter;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class AdminPage extends SimpleCustomInventory {

	private final APM instance;
	private final List<Integer> slots;
	private final String path = "gui.admin-gui";
	private boolean showChancePacks = true;
	private int maxPlace = 0;
	private boolean notElements = false;
	
	public AdminPage(APM instance) {
		super(instance);
		this.instance = instance;
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
				instance.getServer().dispatchCommand(player, "apm helps");
			}
		});
		
		InventoryIcon icon = getIcon(instance.getConfig().getInt(path + ".compulsory-content.open-world-filter-information.slot"));
		List<String> lore = icon.getLore(), blocks = new ArrayList<>(), coordFilter = new ArrayList<>();
		for(Entry<Pair<Material, Byte>, Integer> entry : instance.getOpenWorldFilter().getFilterBlocks().entrySet()) {
			blocks.add(Settings.GUI_UNIVERSAL_ELEMENTS_BLOCK.getString()
					.replace("%blockname%", entry.getKey().getKey().name() + (entry.getKey().getValue() != 0 ? (":" + entry.getKey().getValue()) : "")).replace("%chance%", "" + entry.getValue()));
		}
		for(Entry<String, ChunkFilter> entry : instance.getOpenWorldFilter().getChunkFilters().entrySet()) {
			coordFilter.addAll(Settings.GUI_UNIVERSAL_ELEMENTS_COORD_FILTER.getStringList().stream()
					.map(line -> line.replace("%world%", entry.getKey()).replace("%borders%", entry.getValue().getBorders())).collect(Collectors.toList()));
		}
		for(int line = 0; line < lore.size(); line++) {
			lore.set(line, lore.get(line).replaceAll("%status%",
					(instance.isActiveOpenWorldFilter() ? Settings.GUI_UNIVERSAL_ELEMENTS_OPENWORLDFILTER_ACTIVE.getString() : Settings.GUI_UNIVERSAL_ELEMENTS_OPENWORLDFILTER_DEACTIVE.getString())));
			if(lore.get(line).contains("%blocks%")) {
				lore.remove(line);
				lore.addAll(line, blocks);
			}
			if(lore.get(line).contains("%coord-filters%")) {
				lore.remove(line);
				lore.addAll(line, coordFilter);
			}
		}
		icon.setLore(lore);
	}
	
	public void openAdminPage(Player player, int page) {
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
					openAdminPage(player, page);
				}
			});
			int elementLength = slots.size();
			int element = 0;
			int slot = 0;
			if(showChancePacks) {
				for(Entry<String, ChancePack> entry : instance.getChanceManager().getChances().entrySet()) {
					if(element >= ((page - 1) * elementLength) && element < (page * elementLength)) {
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
									.replace("%cost_money%", "" + pack.getCostMoney()).replace("%status%", Settings.GUI_UNIVERSAL_ELEMENTS_CHANCEPACK_ADMIN.getString()));
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
						setIcon(slots.get(slot++), icon, true);
					}
					element++;
				}
				fillPageButtons(page, elementLength, instance.getChanceManager().getChances().size());
			} else {
				Set<String> permissions = Settings.RENEWAL_DATE_BASE.getConfigurationSection(false);
				for(String permission : permissions) {
					if(element >= ((page - 1) * elementLength) && element < (page * elementLength)) {
						InventoryIcon icon = instance.getGuiManager().getUniverselContent(UniversalContents.RENEVAL_DATE).getKey().clone();
						icon.setLore(
								icon.getLore().stream()
										.map(line -> line.replaceAll("%status%", Settings.GUI_UNIVERSAL_ELEMENTS_RENEWALDATE_ADMIN.getString()).replaceAll("%permission%", permission)
												.replaceAll("%renewaldate%", "" + (instance.getConfig().getInt(Settings.RENEWAL_DATE_BASE.getPath() + "." + permission))))
										.collect(Collectors.toList()));
						setIcon(slots.get(slot++), icon, true);
					}
					element++;
				}
				fillPageButtons(page, elementLength, permissions.size());
			}
			InventoryIcon trace = instance.getGuiManager().getUniverselContent(showChancePacks ? UniversalContents.CHANCE_PACK_WITH_COSTS : UniversalContents.RENEVAL_DATE).getValue();
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
		int positionPrevious = instance.getConfig().getInt("gui.universal-contents.back-button-" + (page > 1 ? "with" : "without") + "-previous-page.slot");
		if(page * elementLength < requiredSize) {
			setIcon(positionNext, instance.getGuiManager().getUniverselContent(UniversalContents.NEXT_BUTTON).getKey(), true);
			getIcon(positionNext).setClickAction(new ClickAction() {
				@Override
				public void handle(CustomInventory customInventory, Player player, ClickType clickType, boolean shift, InventoryClickEvent event) {
					instance.getServer().getScheduler().runTask(instance, () -> player.closeInventory());
					openAdminPage(player, page + 1);
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
						openAdminPage(player, page - 1);
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
