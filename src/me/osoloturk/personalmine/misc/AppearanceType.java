package me.osoloturk.personalmine.misc;

public enum AppearanceType {
	SINGLE(0),
	MULTI(1),
	OPEN_WORLD(2),
	GENERATOR(3);
	
	private int id;
	
	private AppearanceType(int i) {
		this.id = i;
	}
	
	public int getId() {
		return id;
	}
	
	public static AppearanceType getType(int id) {
		switch(id) {
		case 0:
			return SINGLE;
		case 1:
			return MULTI;
		case 2:
			return OPEN_WORLD;
		case 3:
			return GENERATOR;
		default:
			return SINGLE;
		}
	}
	
	public static AppearanceType getType(String id) {
		switch(id) {
		case "0":
			return SINGLE;
		case "1":
			return MULTI;
		case "2":
			return OPEN_WORLD;
		case "3":
			return GENERATOR;
		default:
			return SINGLE;
		}
	}
}
