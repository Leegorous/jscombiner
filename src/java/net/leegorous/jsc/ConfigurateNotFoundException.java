package net.leegorous.jsc;

public class ConfigurateNotFoundException extends Exception {
	private static final long serialVersionUID = 7063707226655638085L;

	public ConfigurateNotFoundException() {
		super("Configurate file does not exist.");
	}
}
