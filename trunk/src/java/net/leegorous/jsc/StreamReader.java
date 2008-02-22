package net.leegorous.jsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The class <code>StreamReader</code> is like its name, a file stream reader.
 * 
 * @author Leegorous
 * @version 0.2
 */
public class StreamReader {
	private File file;
	private InputStream input;
	private String charset = "UTF-8";
	
	private static StreamReader reader = new StreamReader();
	
	/**
	 * A quick way to read a file, using the shared reader. <br/>
	 * It do the same as <code>new StreamReader(file).read()</code><br/>
	 * Note that the reader assume that the encoding of the file is UTF-8.
	 * 
	 * @param file The file to be opened for reading.
	 * @return The content read.
	 * @throws Exception If an error occurs
	 */
	public static String read(File file) throws Exception {
		reader.setFile(file);
		reader.setCharset("UTF-8");
		return reader.read();
	}
	
	/**
	 * A quick way to read a file, using the shared reader.<br/>
	 * It do the same as <code>new StreamReader(input).read()</code><br/>
	 * 
	 * @param input The input stream of that file.
	 * @return The content read.
	 * @throws Exception
	 */
	public static String read(InputStream input) throws Exception {
		reader.setInput(input);
		reader.setCharset("UTF-8");
		return reader.read();
	}
	
	/**
	 * A quick way to read a file with the specfied charset.<br/>
	 * It do the same as <code>new StreamReader(file,charset).read()</code><br/>
	 * 
	 * @param file       The file to be opened for reading.
	 * @param charset The name of a supported 
	 *              {@link java.nio.charset.Charset </code>charset<code>}
	 * @return The content read.
	 * @throws Exception If an error occurs.
	 */
	public static String read(File file, String charset) throws Exception {
		reader.setFile(file);
		reader.setCharset(charset);
		return reader.read();
	}
	
	/**
	 * A quick way to read a file with the specfied charset.<br/>
	 * It do the same as <code>new StreamReader(input,charset).read()</code><br/>
	 * 
	 * @param input The input stream of that file.
	 * @param charset The name of a supported 
	 *              {@link java.nio.charset.Charset </code>charset<code>}
	 * @return The content read.
	 * @throws Exception If an error occurs.
	 */
	public static String read(InputStream input, String charset) throws Exception {
		reader.setInput(input);
		reader.setCharset(charset);
		return reader.read();
	}
	
	/**
	 * Constructs a new empty reader.
	 */
	public StreamReader() {}
	
	/**
	 * Constructs a new reader with the specified file
	 * @param file The file to be opened for reading.
	 */
	public StreamReader(File file) {
		this.setFile(file);
	}
	
	/**
	 * Constructs a new reader with the specified input.
	 * @param input The input stream of the file to be opened for reading.
	 * @throws IOException If an error occurs.
	 */
	public StreamReader(InputStream input) throws IOException {
		this.setInput(input);
	}
	
	/**
	 * Constructs a new reader with the specified file and character set.
	 * @param file        The file to be opened for reading.
	 * @param charset  The name of a supported 
	 *              {@link java.nio.charset.Charset </code>charset<code>}
	 */
	public StreamReader(File file, String charset) {
		this(file);
		this.setCharset(charset);
	}
	
	/**
	 * Constructs a new reader with the specified file and character set.
	 * @param file        The input stream of that file.
	 * @param charset  The name of a supported 
	 *              {@link java.nio.charset.Charset </code>charset<code>}
	 */
	public StreamReader(InputStream input, String charset) throws IOException {
		this(input);
		this.setCharset(charset);
	}
	
	/**
	 * Set the source file
	 * @param file  The file to be opened for reading.
	 */
	public void setFile(File file) {
		if (file!=null) this.input = null;
		this.file = file;
	}
	
	public void setInput(InputStream input) throws IOException {
		if (input==null) throw new IOException("InputStream is null.");
		this.input = input;
	}

	/**
	 * Set the charset of the file to be opened.
	 * @param charset  The name of a supported 
	 *              {@link java.nio.charset.Charset </code>charset<code>}
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * Read the source file.
	 * @return The content read.
	 * @throws Exception if an error occurs.
	 */
	public String read() throws Exception {
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			if (input==null) input = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(input,charset));
			char[] c= null;
			while (reader.read(c=new char[8192])!=-1) {
				sb.append(c);
			}
			c=null;
		} catch (Exception e) {
			throw e;
		} finally {
			reader.close();
		}
		return sb.toString().trim();
	}
}
