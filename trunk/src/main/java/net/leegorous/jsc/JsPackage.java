/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author leegorous
 * 
 */
public class JsPackage {

	private String name;
	private Set paths;

	public JsPackage() {
		this("");
	}

	public JsPackage(String name) {
		if (name == null) {
			throw new IllegalArgumentException(
					"name for jsPackage should not be null");
		}
		this.name = name;

		paths = new HashSet();
	}

	public void add(File file) {
		if (file == null) {
			throw new IllegalArgumentException(
					"file for jsPackage should not be null");
		}
		paths.add(file);
	}

	public void add(JsPackage pkg) {
		if (name.equals(pkg.getName())) {
			paths.addAll(pkg.getPaths());
		} else {
			throw new IllegalArgumentException("package name not match");
		}
	}

	public void add(String path) {
		this.add(new File(path));
	}

	public String getName() {
		return name;
	}

	public Set getPaths() {
		return paths;
	}

	public Map refresh(Map pkgs) {
		Set tmp = new HashSet();

		for (Iterator it = paths.iterator(); it.hasNext();) {
			File path = (File) it.next();
			if (!path.exists())
				continue;

			File[] subs = path.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory()
							&& !file.getName().startsWith(".");
				}
			});

			for (int i = 0; i < subs.length; i++) {
				File sub = subs[i];
				String subName = sub.getName();
				subName = name + (name.length() > 0 ? "." : "") + sub.getName();
				JsPackage pkg = (JsPackage) pkgs.get(subName);
				if (pkg == null) {
					pkg = new JsPackage(subName);
					pkgs.put(subName, pkg);
				}
				pkg.add(sub);
				tmp.add(pkg);
			}
		}

		for (Iterator it = tmp.iterator(); it.hasNext();) {
			JsPackage pkg = (JsPackage) it.next();
			pkg.refresh(pkgs);
		}

		return pkgs;
	}

	public JsFile getJs(String name) {
		JsFile result;
		for (Iterator it = paths.iterator(); it.hasNext();) {
			File path = (File) it.next();
			File[] subs = path.listFiles();
			for (int i = 0; i < subs.length; i++) {
				File file = subs[i];
				if (file.isDirectory())
					continue;
				if (file.getName().equals(name + ".js")) {
					result = new JsFile();
					try {
						result.setClasspath(this.name);
						result.setClazz(name);
						result.setPath(file.getCanonicalPath());
						return result;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

}
