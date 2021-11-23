package me.osoloturk.personalmine.services;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import me.osoloturk.personalmine.APM;

public class SoundService {
	private final Pattern FORMAT_PATTERN = Pattern.compile("\\d+|\\W+");
	private final HashMap<Sounds, Sound> sounds;
	
	public SoundService(final APM instance) {
		sounds = new HashMap<>();
		validateSounds(instance);
	}
	
	private void validateSounds(final APM instance) {
		int validatorIndex = 1;
		for(final Sounds sound : Sounds.values()) {
			if(getSound(sound) == null) {
				instance.getLogger().warning("[Validator Index No: " + validatorIndex + "]");
				instance.getLogger().warning("Your sound doesn't match any sound data so will not play sound.");
				instance.getLogger().warning("Path of Excepted Sound Material: " + sound.getSoundPath());
				instance.getLogger().warning("Value of Excepted Sound Material: " + instance.getConfig().getString(sound.getSoundPath()));
				instance.getLogger().warning("[Validator Index No: " + validatorIndex++ + "]");
			}
		}
	}
	
	private final String format(String source) {
		return FORMAT_PATTERN.matcher(source.trim().replace('-', '_').replace(' ', '_')).replaceAll("").toUpperCase(Locale.ENGLISH);
	}
	
	public void playSound(final Player player, final Sounds sound) {
		final Sound playeableSound = getSound(sound);
		if(playeableSound == null)
			return;
		player.playSound(player.getLocation(), playeableSound, 1.0f, 1.0f);
	}
	
	public Sound getSound(final Sounds soundSource) {
		if(!sounds.containsKey(soundSource)) {
			Optional<Sound> sound = Enums.getIfPresent(Sound.class, format(soundSource.getSound()));
			sounds.put(soundSource, sound.orNull());
		}
		return sounds.get(soundSource);
	}
	
	public enum Sounds {
		BREAK_SINGLE_MINEBLOCK("sounds.break-single-mineblock"),
		BREAK_MULTI_MINEBLOCK("sounds.break-multi-mineblock"),
		BREAK_GENERATOR_MINEBLOCK("sounds.break-generator-mineblock"),
		BREAK_OPEN_WORLD_MINEBLOCK("sounds.break-open-world-mineblock"),
		COMMAND_SUCCESS("sounds.command-success"),
		COMMAND_FAILED("sounds.command-failed"),
		GENERATOR_PLACE("sounds.generator-place"),
		GUI_CLICK("sounds.gui-click"),
		UNDEFINED_ERRORS("sounds.undefined-errors"),;
		
		private String soundPath;
		
		Sounds(final String soundPath) {
			this.soundPath = soundPath;
		}
		
		public String getSoundPath() {
			return soundPath;
		}
		
		public String getSound() {
			return APM.getInstance().getConfig().getString(getSoundPath());
		}
		
	}
}
