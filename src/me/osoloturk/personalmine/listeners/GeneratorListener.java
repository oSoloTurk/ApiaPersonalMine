package me.osoloturk.personalmine.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.GeneratorMineBlock;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.services.SoundService.Sounds;
import me.osoloturk.personalmine.utils.Utils;

public class GeneratorListener implements Listener {
	
	private APM instance;
	private ItemStack generatorItem;
	private Material generatorMaterial;
	private List<Location> generators;
	
	public GeneratorListener(APM instance) {
		this.instance = instance;
		generatorItem = instance.getGuiManager().createItem(Settings.GENERATOR_BLOCK.getPath());
		if(Settings.GENERATOR_RECIPE_ENABLED.getBoolean()) {
			Map<Material, Character> itemMap = new HashMap<>();
			NamespacedKey key = new NamespacedKey(instance, "generator");
			ShapedRecipe recipe = new ShapedRecipe(key, generatorItem);
			removeOldRecipe(recipe, key);
			char identifierKey = 'a';
			String shapeWord = "";
			for(int index = 1; index <= 9; index++) {
				String path = Settings.GENERATOR_RECIPE_ITEMS.getPath() + "." + index;
				if(instance.getConfig().isSet(path)) {
					Material material;
					try {
						material = Material.getMaterial(instance.getConfig().getString(path));
					} catch(Exception e) {
						instance.getLogger().warning("Your " + index + ". item of generator recipe is wrong!");
						continue;
					}
					if(!itemMap.containsKey(material)) {
						itemMap.put(material, identifierKey++);
					}
					shapeWord += itemMap.get(material);
				} else
					shapeWord += " ";
			}
			recipe.shape(shapeWord.substring(0, 3), shapeWord.substring(3, 6), shapeWord.substring(6, 9));
			for(Map.Entry<Material, Character> entry : itemMap.entrySet()) {
				recipe.setIngredient(entry.getValue(), entry.getKey());
			}
			instance.getServer().getScheduler().runTask(instance, () -> instance.getServer().addRecipe(recipe));
			
		}
		generators = new ArrayList<>();
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			for(List<MineBlock> mineBlocks : instance.getMineBlockManager().getMineBlocks().values()) {
				for(MineBlock mineBlock : mineBlocks) {
					if(mineBlock instanceof GeneratorMineBlock) {
						generators.add(mineBlock.getLocations().get(0));
					}
				}
			}
		});
	}
	
	private void removeOldRecipe(Recipe recipe, NamespacedKey key) {
		if(Utils.isLegacy()) {
			Iterator<Recipe> iter = instance.getServer().recipeIterator();
			while(iter.hasNext()) {
				Recipe r = iter.next();
				if(r == recipe) {
					iter.remove();
				}
			}
		} else {
			instance.getServer().getScheduler().runTask(instance, () -> instance.getServer().removeRecipe(key));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		if(event.getItemInHand().isSimilar(generatorItem)) {
			GeneratorMineBlock generator = new GeneratorMineBlock(event.getBlock().getLocation(), System.currentTimeMillis());
			if(generator.getProductLocation().getBlock().getType() == Material.AIR)
				generator.getProductLocation().getBlock().setType(Material.COBBLESTONE);
			instance.getServer().getScheduler().runTaskLater(instance, () -> instance.getMineBlockManager().addMineBlock(generator), 1);
			saveGenerator(event.getPlayer(), event.getBlock().getLocation());
			log(event.getPlayer(), event.getBlock().getLocation(), false);
			if(generatorMaterial == null)
				generatorMaterial = event.getBlockPlaced().getType();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if(generatorMaterial != null && event.getBlock().getType() != generatorMaterial)
			return;
		Location loc = event.getBlock().getLocation();
		if(generators.contains(loc)) {
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			Block up = event.getBlock().getRelative(BlockFace.UP);
			if(up.getType() == Material.COBBLESTONE)
				up.setType(Material.AIR);
			loc.getWorld().dropItemNaturally(loc, generatorItem);
			generators.remove(loc);
			instance.getMineBlockManager().removeMineBlock(loc);
			log(event.getPlayer(), event.getBlock().getLocation(), true);
			if(generatorMaterial == null)
				generatorMaterial = event.getBlock().getType();
		}
		
	}
	
	private void saveGenerator(final Player player, final Location loc) {
		generators.add(loc);
		instance.getSoundService().playSound(player, Sounds.GENERATOR_PLACE);
	}
	
	private void log(Player player, Location generatorLoc, boolean isBreak) {
		if(!Settings.GENERATOR_CONSOLE_LOG.getBoolean())
			return;
		instance.getLogger().info(player.getDisplayName() + " (" + player.getUniqueId().toString() + ") " + (isBreak ? "broken" : "placed") + " a generator at "
				+ Utils.getPrettyStringFromLocation(generatorLoc) + " in the " + generatorLoc.getWorld().getName());
	}
	
	public ItemStack getGeneratorItem() {
		return generatorItem;
	}
}
