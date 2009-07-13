/**
 * 
 */
package net.leegorous.jsc;

import java.util.Date;
import java.util.Set;

/**
 * @author leegorous
 * 
 */
public class JsFile {

	/**
	 * The file path of javascript
	 */
	private String path;

	/**
	 * The class name of javascript
	 */
	private String name;

	private long lastModified;

	private long length;

	private Set imported;

	/**
	 * @return the imported
	 */
	public Set getImported() {
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
	 * @param imported
	 *            the imported to set
	 */
	public void setImported(Set imported) {
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
		return "{name:" + name + "; path:" + path + "; modified:" + date
				+ "; len:" + length + "}";
	}
}
