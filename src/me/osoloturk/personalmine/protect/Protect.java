package me.osoloturk.personalmine.protect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.bukkit.Bukkit;

import me.osoloturk.personalmine.APM;


public class Protect {
	public static String uuid = "%%__USER__%%";
	public static String nonce = "%%__NONCE__%%";
	
	public static boolean auth() {
		try {
			URLConnection localURLConnection = new URL("https://gist.githubusercontent.com/bertek41/92a7149ea4bae103a2836c168fd3f449/raw/ApiaTeamBlacklist").openConnection();
			localURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			localURLConnection.connect();
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
			}
			String str2 = localStringBuilder.toString();
			if(str2.contains(String.valueOf(uuid)) || uuid.equals(getConvert("CEHHAF"))) {
				disableLeak();
				return false;
			}
		} catch(IOException e) {
			return true;
		}
		if(nonce.isEmpty() || uuid.isEmpty() || !nonce.isEmpty() && uuid.isEmpty()) {
			disableLeak();			
			return false;
		}
		return true;
	}
	
	private static void disableLeak() {
		APM.getInstance().getServer().getConsoleSender().sendMessage("§c[§eAPM§c] §4Your APM has license problem.");
		try {
			Bukkit.getPluginManager().getPlugin(APM.getInstance().getName()).getConfig().save(APM.getInstance().getPluginFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		APM.getInstance().disablePlugin();
		return;
	}
	
	private static String getConvert(String key) {
		String chars = "ABCDEFGHIJ";
		HashMap<String, Integer> convert = new HashMap<>();
		int i = 0; 
		for(String singleChar : chars.split("")) {
			convert.put(singleChar, i);
			i++;
		}
		String output = "";
		for(String keyChar : key.split("")) {
			output = output + convert.get(keyChar);
		}
		return output;
	}
}
