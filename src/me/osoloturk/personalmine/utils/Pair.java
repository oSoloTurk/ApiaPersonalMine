package me.osoloturk.personalmine.utils;

public class Pair<F, S> {
	private F key;
	private S value;
	
	Pair(F key, S value) {
		this.key = key;
		this.value = value;
	}
	
	public F getKey() {
		return key;
	}
	
	public S getValue() {
		return value;
	}
	
	public void setLeft(F key) {
		this.key = key;
	}
	
	public void setRight(S value) {
		this.value = value;
	}
	
	public static <F, S> Pair<F, S> of(F key, S value) {
		return new Pair<F, S>(key, value);
	}
}
