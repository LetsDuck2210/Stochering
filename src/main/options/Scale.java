package main.options;

public class Scale extends Option<Integer> {

	public Scale() {
		super(100);
	}
	
	public String name() {
		return "scale";
	}
	public String desc() {
		return "scale used for internal rounding (mainly for division)";
	}

	public Class<?> type() {
		return Integer.class;
	}
}
