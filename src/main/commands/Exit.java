package main.commands;

import main.Main;

public class Exit implements Command {

	@Override
	public void exec(String[] args) {
		Main.stop();
	}

	@Override
	public String desc() {
		return "exit the program";
	}
	
}
