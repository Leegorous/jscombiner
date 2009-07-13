/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author leegorous
 * 
 */
public class JsContext {
	private JsContextManager manager;

	public JsContext(JsContextManager manager) {
		this.manager = manager;
	}

	public JsFile load(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}

		return manager.getJs(path);
	}

	public JsContextManager getContextManager() {
		return manager;
	}

}
