package me.osoloturk.personalmine.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class ChancePack {
	private boolean defaultMode;
	private long costMoney;
	private int costExp;
	private final List<String> desc;
	private boolean sell;
	private Map<Pair<Material, Byte>, Integer> chances;
	
	public ChancePack(APM instance, String path) {
		defaultMode = instance.getConfig().getBoolean(path + ".default");
		sell = instance.getConfig().getBoolean(path + ".sell");
		costMoney = instance.getConfig().getLong(path + ".cost.money");
		costExp = instance.getConfig().getInt(path + ".cost.exp");
		chances = new HashMap<>();
		for(String materialName : instance.getConfig().getConfigurationSection(path + ".blocks").getKeys(false)) {
			try {
				chances.put(Pair.of(Material.valueOf(materialName), (byte) instance.getConfig().getInt(path + ".blocks." + materialName + ".data_value", 0)),
						instance.getConfig().getInt(path + ".blocks." + materialName + ".chance", 1));
			} catch(Exception e) {
				continue;
			}
		}
		desc = Utils.color(instance.getConfig().getStringList(path + ".description"));
	}
	
	public ChancePack(boolean defaultMode, boolean sell, long costMoney, int costExp, Map<Pair<Material, Byte>, Integer> chances, List<String> desc) {
		this.defaultMode = defaultMode;
		this.sell = sell;
		this.costExp = costExp;
		this.costMoney = costMoney;
		this.chances = chances;
		this.desc = desc;
	}
	
	public boolean isSelling() {
		return sell;
	}
	
	public boolean isDefaultMode() {
		return defaultMode;
	}
	
	public long getCostMoney() {
		return costMoney;
	}
	
	public int getCostExp() {
		return costExp;
	}
	
	public Map<Pair<Material, Byte>, Integer> getChances() {
		return chances;
	}
	
	public int removeChanceItem(Pair<Material, Byte> item) {
		int previousValue = chances.getOrDefault(item, 0);
		chances.remove(item);
		return previousValue;
	}
	
	public int setChanceItem(Pair<Material, Byte> item, int value) {
		int previousValue = chances.getOrDefault(item, 0);
		chances.put(item, value);
		return previousValue;
	}
	
	public boolean setDefaultMode(boolean defaultMode) {
		this.defaultMode = defaultMode;
		return !defaultMode;
	}
	
	public long setCostMoney(long costMoney) {
		long previous = this.costMoney;
		this.costMoney = costMoney;
		return previous;
	}
	
	public long setCostExp(int costExp) {
		long previous = this.costExp;
		this.costExp = costExp;
		return previous;
	}
	
	public List<String> getDescription() {
		return desc;
	}
	
}
