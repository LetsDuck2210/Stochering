package main.commands;

import main.Main;

public class Help implements Command {

	@Override
	public void exec(String[] args) {
		if(args.length == 0)
			Main.commands.forEach((s,c) -> System.out.println(s + " - " + c.desc()));
		else if(args.length == 1)
			if(Main.commands.containsKey(args[0])) { System.out.println(Main.commands.get(args[0]).help()); }
			else { System.out.println("no such command '" + args[0] + "'"); }
		else
			System.out.println(help());
	}

	@Override
	public String desc() {
		return "print this help screen";
	}
	public String help() {
		return "usage: /help [command]";
	}

}
