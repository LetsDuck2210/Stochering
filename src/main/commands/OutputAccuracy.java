package main.commands;

import main.Main;

public class OutputAccuracy implements Command {

	@Override
	public void exec(String[] args) {
		if(args.length != 1) {
			System.out.println("output accuracy: " + Main.getOutputAccuracy());
			return;
		}
		if(!args[0].matches("\\d+")) {
			System.out.println("Expected int for: <accuracy>");
			return;
		}
		Main.setOutputAccuracy(Integer.parseInt(args[0]));
		System.out.println("set output accuracy to " + args[0]);
	}

	@Override
	public String desc() {
		return "set decimal accuracy in output (-1 for infinite)";
	}
	
}
