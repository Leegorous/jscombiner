/**
 * 
 */
package net.leegorous.jsc;

import java.util.Date;
import java.util.List;

/**
 * @author leegorous
 * 
 */
public class JsFile {

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

	/**
	 * The name of javascript
	 */
	private String name;

	private long lastModified;

	private long length;

	private List imported;

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

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String toString() {
		Date date = new Date();
		date.setTime(lastModified);
		StringBuffer buf = new StringBuffer();
		buf.append("{\n");
		buf.append("name:" + name + "\n");
		buf.append("path:" + path + "\n");
		buf.append("imported:" + imported + "\n");
		buf.append("modified:" + date + "\n");
		buf.append("length:" + length + "\n");
		buf.append('}');
		return buf.toString();
	}
}
