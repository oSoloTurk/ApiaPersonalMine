package me.osoloturk.personalmine.gui_infrastructure;

public enum Size {
	HOPPER(5),
	ONE_ROWS(9),
	TWO_ROWS(18),
	THREE_ROWS(27),
	FOUR_ROWS(36),
	FIVE_ROWS(45),
	SIX_ROWS(54);
	
	private final int size;
	
	Size(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getLine() {
		return (size / 9) - 1;
	}
	
	public int getPosition() {
		return size / 9;
	}
	
	public static Size fit(int slots) {
		if(slots < 6)
			return HOPPER;
		else if(slots < 10)
			return ONE_ROWS;
		else if(slots < 19)
			return TWO_ROWS;
		else if(slots < 28)
			return THREE_ROWS;
		else if(slots < 37)
			return FOUR_ROWS;
		else if(slots < 46)
			return FIVE_ROWS;
		else
			return SIX_ROWS;
	}
}