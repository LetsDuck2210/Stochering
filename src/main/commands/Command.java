package main.commands;

public interface Command {
	public void exec(String[] args);
	public String desc();
	public String help();
}
