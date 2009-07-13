/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author leegorous
 * 
 */
public class JsContextManager {

	private Set classpath = Collections.synchronizedSet(new HashSet());

	private Map files = Collections.synchronizedMap(new HashMap());

	/**
	 * It will search a file named 'config.js' in the adding path, extract
	 * classpath information, and do the configuration recursively
	 * 
	 * @param path
	 *            the classpath
	 * @throws Exception
	 */
	public void configClasspath(File path) throws Exception {
		if (!classpath.contains(path)) {
			File config = new File(path, "config.js");
			if (config.exists()) {
				Set cp = JavaScriptDocument.configClasspath(config);
				for (Iterator it = cp.iterator(); it.hasNext();) {
					File item = (File) it.next();
					configClasspath(item);
				}
			}
			classpath.add(path);
		}
	}

	public JsContext createContext() {
		return new JsContext(this);
	}

	public Set getClasspath() {
		return classpath;
	}

	protected Map getFiles() {
		return files;
	}

	public JsFile getJs(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}
		JsFile js = (JsFile) files.get(path);

		if (js == null) {
			js = new JsFile();
			refresh(js, file);

			files.put(path, js);
		} else {
			if (file.lastModified() > js.getLastModified()
					|| file.length() != js.getLength()) {
				refresh(js, file);
			}
		}
		return js;
	}

	/**
	 * Refresh the jsFile object with the script file and the content within
	 * 
	 * @param js
	 * @param file
	 * @throws Exception
	 */
	protected void refresh(JsFile js, File file) throws Exception {
		String content = JavaScriptDocument.readFile(file);
		File cp = JavaScriptDocument.resolveClasspath(file, content);
		if (cp != null)
			configClasspath(cp);

		js.setName(JavaScriptDocument.getClassName(content));
		js.setPath(file.getAbsolutePath());
		js.setLastModified(file.lastModified());
		js.setLength(file.length());
	}

}
