package main.commands;

public class Clear implements Command {

	@Override
	public void exec(String[] args) {
		System.out.print("\033[2J\033[H");
	}

	@Override
	public String desc() {
		return "clear the screen";
	}

}
