package net.leegorous.jsc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * The class <code>StreamWrite</code> is, like its name, a file stream writer.
 * 
 * @author Leegorous
 * @version 0.2
 */
public class StreamWriter {
	private File file;
	private String charset = "UTF-8";
	
	private static StreamWriter writer = new StreamWriter();
	
	/**
	 * A quick way to save the content to the specified file.<br/>
	 * It do the same as: <p><code>new StreamWriter(file).write(content);</code></p>
	 * @param file The target file.
	 * @param content The content want to be saved.
	 * @throws Exception If an error occurs.
	 */
	public static void write(File file,String content) throws Exception {
		writer.setFile(file);
		writer.write(content);
	}
	
	/**
	 * A quick way to save the content to the specified file.<br/>
	 * It do the same as :<p><code>new StreamWriter(file, charset).write(content);</code></p>
	 * @param file The target file.
	 * @param content The content want to be saved.
	 * @param charset The charset of the target file.
	 * @throws Exception If an error occurs.
	 */
	public static void write(File file, String content, String charset) throws Exception {
		writer.setFile(file);
		writer.setCharset(charset);
		writer.write(content);
	}
	
	public StreamWriter() {}
	
	public StreamWriter(File file) {
		this.setFile(file);
	}
	
	public StreamWriter(File file, String charset) {
		this(file);
		this.setCharset(charset);
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Save the content as a specified file.
	 * @param content The content want to saved.
	 * @throws Exception If an error occurs
	 */
	public void write(String content) throws Exception {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
			writer.write(content.toString(),0,content.length());
			writer.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (writer!=null) writer.close();
		}
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}
