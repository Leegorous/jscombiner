/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.leegorous.util.ConfigPattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author leegorous
 * 
 */
public class JsContextManager {

	private static ConfigPattern PATTERN_MODULE = new ConfigPattern("module",
			"\\w+(\\.\\w+)*");

	private class Classname {
		String pkg = "";
		String pkgPath = "";
		String clazz;

		Classname(String name) {
			clazz = name;
			if (name.indexOf('.') > 0) {
				int idx = name.lastIndexOf('.');
				pkg = name.substring(0, idx);
				pkgPath = pkg.replaceAll("\\.", "/");
				clazz = name.substring(idx + 1);
			}
		}
	}

	protected Log log = LogFactory.getLog(this.getClass());

	private Set classpath = Collections.synchronizedSet(new TreeSet());

	private Map files = Collections.synchronizedMap(new HashMap());

	private Map classpathConfig = new HashMap();

	public JsFile getClasspathConfig(String path) {
		return (JsFile) classpathConfig.get(path);
	}

	private void checkUpdate(JsFile js) throws Exception {
		File file = new File(js.getPath());
		checkUpdate(js, file);
	}

	private void checkUpdate(JsFile js, File file) throws Exception {
		if (file.lastModified() > js.getLastModified()
				|| file.length() != js.getLength()) {
			if (js.getPath().equals(file.getAbsolutePath()))
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
			classpath.add(path);
			File config = new File(path, "config.js");
			if (config.exists()) {
				String content = JavaScriptDocument.readFile(config);
				Set cp = JavaScriptDocument.configClasspath(config, content);
				JsFile cfg = new JsFile();
				cfg.setLastModified(path.lastModified());
				cfg.setImported(JavaScriptDocument.getImportInfo(content));
				if (log.isDebugEnabled()) {
					log.debug("found classpath config: " + cfg);
				}

				classpathConfig.put(path.getAbsolutePath(), cfg);

				if (cp != null) {
					for (Iterator it = cp.iterator(); it.hasNext();) {
						File item = (File) it.next();
						configClasspath(item);
					}
				} else {
					log.debug("could not find classpath definition in "
							+ config.getAbsolutePath());
				}
			}
			log.info("found classpath: " + path.getAbsolutePath());
		}
	}

	public JsContext createContext() {
		return new JsContext(this);
	}

	private JsFile createJs(File file) throws Exception {
		return createJs(file, null);
	}

	/**
	 * Create the {@link JsFile} from file and cache it. The key could be the
	 * full file path, value of '@class' or the given classname
	 * 
	 * @param file
	 * @param classname
	 * @return
	 * @throws Exception
	 */
	private JsFile createJs(File file, String classname) throws Exception {
		JsFile js = new JsFile();
		refresh(js, file);

		files.put(file.getAbsolutePath(), js);
		if (js.getClazz() != null) {
			files.put(js.getClazz(), js);
		}
		if (js.getName() != null) {
			files.put(js.getName(), js);
		}
		if (js.getClazz() == null && js.getName() == null && classname != null) {
			files.put(classname, js);
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
			Classname cn = new Classname(classname);
			final String filename = cn.clazz + ".js";

			for (Iterator it = classpath.iterator(); it.hasNext();) {
				File pkg = (File) it.next();
				if (cn.pkgPath.length() > 0) {
					String pkgPath = FilenameUtils.concat(
							pkg.getAbsolutePath(), cn.pkgPath);
					pkg = new File(pkgPath);
					if (!pkg.exists())
						continue;
				}
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
					js = createJs(list[0], classname);
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

	public List getJsClasses(String classname) throws Exception {
		List result = new ArrayList();
		if (classname.indexOf('*') >= 0) {
			Classname cn = new Classname(classname);

			for (Iterator it = classpath.iterator(); it.hasNext();) {
				File cp = (File) it.next();
				File pkg = cp;
				if (cn.pkgPath.length() > 0) {
					String pkgPath = FilenameUtils.concat(cp.getAbsolutePath(),
							cn.pkgPath);
					pkg = new File(pkgPath);
					if (!pkg.exists())
						continue;
				}
				File[] cs = pkg.listFiles();
				for (int i = 0; i < cs.length; i++) {
					File file = cs[i];
					if (file.isDirectory())
						continue;
					String name = file.getName();
					if (name.toLowerCase().endsWith(".js")) {
						String clazz = name.substring(0, name.indexOf('.'));
						JsFile js = getJsClass(cn.pkg
								+ (cn.pkg.length() > 0 ? "." : "") + clazz);
						if (js != null && !result.contains(js))
							result.add(js);
					}
				}
			}
		} else {
			JsFile js = getJsClass(classname);
			if (js != null && !result.contains(js))
				result.add(js);
		}
		return result;
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
		if (cp != null) {
			configClasspath(cp);
			js.setClasspath(cp.getAbsolutePath());
		}

		js.setModule(PATTERN_MODULE.getValue(content));
		js.setClazz(JavaScriptDocument.getClassName(content));
		js.setName(JavaScriptDocument.getScriptName(content));
		js.setPath(file.getAbsolutePath());
		js.setLastModified(file.lastModified());
		js.setLength(file.length());
		js.setImported(JavaScriptDocument.getImportInfo(content));
		if (log.isDebugEnabled()) {
			log.debug("JsFile created: " + js);
		}
	}
}
