package main.commands;

import main.Main;
import main.options.Option;

public class OptionCmd implements Command {

	@Override
	public void exec(String[] args) {
		if(args.length != 2 && args.length != 1) {
			System.out.println("Expected at 1 or 2 arguments: /option <option> [new value]");
			return;
		}
		if(args.length == 2) {
			try {
				System.out.println("set '" + args[0] + "' to '" + Main.setOption(args[0], Main.parseParam(args[1], Main.getOptionType(args[0]))) + "'");
			} catch(Exception e) {
				System.out.println("couldn't set '" + args[0] + "' to '" + args[1] + "': " + e.getMessage());
			}
			return;
		}
	}

	@Override
	public String desc() {
		return "change options";
	}
	public String help() {
		String help = "usage: /option <option> [new value]\n\noptions:";
		for(Option<?> option : Main.options) {
			help += "\n" + option.name() + " - " + option.desc();
		}
		return help;
	}
	
}
