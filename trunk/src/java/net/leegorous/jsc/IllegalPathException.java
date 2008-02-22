package net.leegorous.jsc;

public class IllegalPathException extends Exception {
	private static final long serialVersionUID = 7427463462765972295L;
	
	public IllegalPathException() {
		super("Unexpected configurate file path.");
	}
}
