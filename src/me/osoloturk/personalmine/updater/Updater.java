package me.osoloturk.personalmine.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.osoloturk.personalmine.APM;

public class Updater implements Listener {
	
	public boolean updateRequired = false;
	public String spigotVersion;
	
	public Updater(APM instance) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				try{
					URLConnection localURLConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=90438").openConnection();
					spigotVersion = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream())).readLine();
				}
				catch (Exception e){
					e.printStackTrace();
				}
				if(instance.getDescription().getVersion().equals(spigotVersion)) {
					instance.getLogger().info("");
					instance.getLogger().info("");
					instance.getLogger().info("Your ApiaPersonalMine version is up to date!");
					instance.getLogger().info("Version: " + instance.getDescription().getVersion());
					instance.getLogger().info("");
					instance.getLogger().info("");
				} else {
					updateRequired = true;
					instance.getLogger().info("");
					instance.getLogger().info("");
					instance.getLogger().info("Your ApiaPersonalMine version is legacy!");
					instance.getLogger().info("Your Version: " + instance.getDescription().getVersion() + " New version: " + spigotVersion);
					instance.getLogger().info("");
					instance.getLogger().info("");
					instance.getServer().getPluginManager().registerEvents(Updater.this, instance);
				}				
			}
		});
	}
	
	@EventHandler
	public void sendNewUpdate(PlayerJoinEvent event) {
		if(event.getPlayer().isOp()) {
			event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l[&fAPM&8&l] &eYour APM version is legacy. &8&l[ &7New Version: " + spigotVersion + "&8&l]"));
		}
	}
	

}