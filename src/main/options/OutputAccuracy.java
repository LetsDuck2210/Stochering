package main.options;

public class OutputAccuracy extends Option<Integer> {
	
	public OutputAccuracy() {
		super(8);
	}

	public String name() {
		return "output_accuracy";
	}
	public String desc() {
		return "accuracy that the output should be rounded to";
	}
	
	public Class<?> type() {
		return Integer.class;
	}

}
