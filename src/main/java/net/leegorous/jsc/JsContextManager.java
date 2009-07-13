/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * @author leegorous
 * 
 */
public class JsContextManager {

	private Set classpath = Collections.synchronizedSet(new HashSet());

	private Map files = Collections.synchronizedMap(new HashMap());

	private void checkUpdate(JsFile js) throws Exception {
		File file = new File(js.getPath());
		checkUpdate(js, file);
	}

	private void checkUpdate(JsFile js, File file) throws Exception {
		if (file.lastModified() > js.getLastModified()
				|| file.length() != js.getLength()) {
			if (js.getPath().equals(file.getAbsoluteFile()))
				refresh(js, file);
			else
				throw new RuntimeException("File not match [" + js.getPath()
						+ " vs. " + file.getAbsolutePath() + "]");
		}
	}

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

	private JsFile createJs(File file) throws Exception {
		JsFile js = new JsFile();
		refresh(js, file);

		files.put(file.getAbsoluteFile(), js);
		if (js.getName() != null) {
			files.put(js.getName(), js);
		}
		return js;
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
			js = createJs(file);
		} else {
			checkUpdate(js, file);
		}
		return js;
	}

	public JsFile getJsClass(String classname) throws Exception {
		JsFile js = (JsFile) files.get(classname);

		if (js == null) {
			for (Iterator it = classpath.iterator(); it.hasNext();) {
				File cp = (File) it.next();
				File pkg = cp;
				String clazz = classname;
				if (classname.indexOf('.') > 0) {
					int idx = classname.lastIndexOf('.');
					String pkgname = classname.substring(0, idx).replaceAll(
							"\\.", "/");
					clazz = classname.substring(idx + 1);
					String pkgPath = FilenameUtils.concat(cp.getAbsolutePath(),
							pkgname);
					pkg = new File(pkgPath);
					if (!pkg.exists())
						continue;
				}
				final String filename = clazz + ".js";
				File[] list = pkg.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						int i = name.lastIndexOf('.');
						if (i < 0)
							return false;
						// do a simple normalize
						name = name.substring(0, i)
								+ name.substring(i).toLowerCase();
						boolean r = name.equals(filename);
						return r;
					}
				});
				if (list == null || list.length == 0)
					continue;
				if (list.length == 1) {
					js = createJs(list[0]);
					break;
				} else
					throw new RuntimeException("There are " + list.length
							+ ", more than 1 script matche " + classname);
			}
		} else {
			checkUpdate(js);
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
		js.setImported(JavaScriptDocument.getImportInfo(content));
	}
}
