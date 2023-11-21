package main.commands;

import main.Main;

public class Help implements Command {

	@Override
	public void exec(String[] args) {
		Main.commands.forEach((s,c) -> System.out.println(s + " - " + c.desc()));
	}

	@Override
	public String desc() {
		return "print this help screen";
	}

}
