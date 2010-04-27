/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.leegorous.util.ConfigPattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author leegorous
 * 
 */
public class JsFile {

	private static Log log = LogFactory.getLog(JsFile.class);

	private static ConfigPattern PATTERN_MODULE = new ConfigPattern("module",
			"\\w+(\\.\\w+)*");
	/**
	 * The class name of javascript
	 */
	private String clazz;

	/**
	 * The file path of javascript
	 */
	private String path;

	/**
	 * The classpath of javascript
	 */
	private String classpath;

	private File file;

	private String module;

	/**
	 * The name of javascript
	 */
	private String name;

	private long lastModified;

	private long length;

	private List imported;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null || file == null)
			return false;
		if (!(obj instanceof JsFile))
			return false;
		JsFile f = (JsFile) obj;
		return file.equals(f.getFile());
	}

	public boolean exist() {
		return file.exists();
	}

	/**
	 * @return the classpath
	 */
	public String getClasspath() {
		return classpath;
	}

	/**
	 * @return the clazz
	 */
	public String getClazz() {
		return clazz;
	}

	public File getFile() {
		return file;
	}

	/**
	 * @return the imported
	 */
	public List getImported() {
		return imported;
	}

	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	public String getModule() {
		return module;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the absolute path of the js file.
	 * 
	 * @return the path of the js file, null if file is null
	 */
	public String getPath() {
		if (file == null)
			return null;
		return file.getAbsolutePath();
	}

	/**
	 * Refresh the js setting: module, lastModified, length and imported classes
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception {
		String content = JavaScriptDocument.readFile(file);

		this.setModule(PATTERN_MODULE.getValue(content));

		this.setLastModified(file.lastModified());
		this.setLength(file.length());
		this.setImported(JavaScriptDocument.getImportInfo(content));

		if (log.isDebugEnabled()) {
			log.debug("JsFile created: " + this);
		}
	}

	/**
	 * @param classpath
	 *            the classpath to set
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	/**
	 * @param clazz
	 *            the clazz to set
	 */
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @param imported
	 *            the imported to set
	 */
	public void setImported(List imported) {
		this.imported = imported;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(long length) {
		this.length = length;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.setFile(new File(path));
	}

	public String toString() {
		Date date = new Date();
		date.setTime(lastModified);
		StringBuffer buf = new StringBuffer();
		buf.append("{\n");
		buf.append("name:" + name + "\n");

		if (clazz != null)
			buf.append("class: " + clazz + "\n");

		if (module != null)
			buf.append("module: " + module + "\n");

		buf.append("path:" + getPath() + "\n");

		if (imported != null && imported.size() > 0)
			buf.append("imported:" + imported + "\n");

		buf.append("modified:" + date + "\n");
		buf.append("length:" + length + "\n");
		buf.append('}');
		return buf.toString();
	}
}
