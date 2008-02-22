package net.leegorous.jsc;

public class LoopedImportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2808449338214356282L;

	public LoopedImportException() {
		super("Import loop occured.");
	}

	public LoopedImportException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LoopedImportException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LoopedImportException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
