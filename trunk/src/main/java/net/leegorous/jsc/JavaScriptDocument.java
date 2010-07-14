/*
 * Copyright 2008 leegorous.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JavaScriptDocument {

	protected Log log = LogFactory.getLog(this.getClass());

	protected static String PATTERN_SUFFIX = "\\s*(;\\s*(\\r?\\n)?|\\r?\\n)";

	protected static String _NAME_PATTERN_SUFFIX = "\\s*(\\w+(\\.\\w+)*)"
			+ PATTERN_SUFFIX;

	protected static Pattern IMPORT_PATTERN = Pattern
			.compile("@import\\s*(\\*|[\\w_\\-]+(\\.[\\w_\\-]+)*(\\.\\*)?)"
					+ PATTERN_SUFFIX);

	protected static Pattern CLASS_PATTERN = Pattern.compile("@class"
			+ _NAME_PATTERN_SUFFIX);

	protected static Pattern INTERFACE_PATTERN = Pattern.compile("@interface"
			+ _NAME_PATTERN_SUFFIX);

	protected static Pattern CLASSPATH_PATTERN = Pattern
			.compile("@classpath\\s*(\\.|[\\w\\.]+|([\\w\\.]+/)+[\\w\\.]*)"
					+ PATTERN_SUFFIX);

	protected static Pattern NAME_PATTERN = Pattern.compile("@name"
			+ _NAME_PATTERN_SUFFIX);

	public static Set configClasspath(File file) throws Exception {
		String content = readFile(file);
		return configClasspath(file, content);
	}

	protected static Set configClasspath(File file, String content)
			throws FileNotFoundException {
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}
		Matcher m = CLASSPATH_PATTERN.matcher(content);
		Set cp = null;
		if (m.find()) {
			cp = new HashSet();
			do {
				String str = m.group(1);
				String path = FilenameUtils.concat(file.getAbsolutePath(), str);
				File p = new File(path);
				if (p.exists() && p.isDirectory()) {
					cp.add(p);
				} else {
					throw new FileNotFoundException(path + " from "
							+ file.getAbsolutePath() + " + " + str);
				}
			} while (m.find());
		}
		return cp;
	}

	public static String getClassName(String content) {
		String name = getMatched(content, CLASS_PATTERN);
		if (name == null) {
			name = getMatched(content, INTERFACE_PATTERN);
		}
		return name;
	}

	public static String getScriptName(String content) {
		return getMatched(content, NAME_PATTERN);
	}

	private static String getMatched(String content, Pattern pattern) {
		Matcher m = pattern.matcher(content);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	protected static String readFile(File file) throws Exception {
		if (file == null)
			throw new JavaScriptNotFoundException();
		String charset = JavaScriptCombiner.getCharset(file);
		return FileUtils.readFileToString(file, charset);
	}

	public static File resolveClasspath(File file) throws Exception {
		String content = readFile(file);
		return resolveClasspath(file, content);
	}

	public static File resolveClasspath(File file, String content)
			throws Exception {
		String className = getClassName(content);
		File classpath = file.getParentFile();
		if (className != null) {
			String[] ss = className.split("\\.");
			if (ss.length > 1) {
				for (int i = ss.length - 2; i >= 0; i--) {
					String pkg = ss[i];
					if (pkg.equals(classpath.getName())) {
						classpath = classpath.getParentFile();
					} else {
						throw new RuntimeException("class definition error");
					}
				}
			}
		}
		return classpath;
	}

	public static List getImportInfo(String content) {
		List config = null;
		Matcher m = IMPORT_PATTERN.matcher(content);

		if (m.find()) {
			config = new ArrayList();
			do {
				if (m.groupCount() > 1) {
					config.add(m.group(1));
				}
			} while (m.find());
		}
		return config;
	}

	private JavaScriptDocument linker;

	private File doc;

	private ArrayList importedFiles = new ArrayList();

	private ArrayList importedDocs = new ArrayList();

	private Set classpath;

	protected String fileName;

	protected String filePath;

	private String content;

	public JavaScriptDocument() {
	}

	public JavaScriptDocument(JavaScriptDocument linker, File file,
			Set classpath) throws JavaScriptNotFoundException {
		this.setLinker(linker);
		this.setDoc(file);
		this.classpath = classpath;
	}

	protected void findImports() throws Exception {
		if (content == null) {
			load();
			if (content == null)
				return;
		}
		// if (classPaths == null)
		// classPaths = new ArrayList();

		if (classpath == null) {
			classpath = new TreeSet();
		}

		classpath.add(resolveClasspath(this.doc, content));

		ArrayList config = getImportConfig(content);
		if (config == null)
			return;

		// classPaths.add(this.doc.getParentFile());

		processImports(config);
	}

	public ArrayList getClasses(String path) throws IOException {
		SubFileFilter filter = new SubFileFilter();
		String[] paths = path.split("/");
		ArrayList result = new ArrayList();
		if (paths.length < 1)
			throw new IOException("Can not find " + path);
		// ArrayList tmpCp = classPaths;
		Set tmpCp = classpath;
		Set cp = null;
		for (int i = 0, j = paths.length - 1; i < j; i++) {
			cp = new TreeSet();
			String item = paths[i].trim();
			if (item.length() == 0)
				continue;
			if (tmpCp.size() == 0)
				throw new IOException("Can not find " + path);
			for (Iterator it = tmpCp.iterator(); it.hasNext();) {
				File f = (File) it.next();
				filter.setDir(f);
				filter.setName(item);
				File[] subs = f.listFiles(filter);
				if (subs.length == 1)
					cp.add(subs[0]);

			}
			tmpCp = cp;
		}
		if (cp == null || cp.size() == 0)
			cp = classpath;
		String item = paths[paths.length - 1];
		for (Iterator it = cp.iterator(); it.hasNext();) {
			File f = (File) it.next();
			filter.setDir(f);
			filter.setName(item);
			File[] subs = f.listFiles(filter);
			if (subs.length > 0) {
				for (int m = 0, n = subs.length; m < n; m++)
					result.add(subs[m]);
			}
		}
		if (result.size() == 0)
			throw new IOException("Can not find " + path);
		// File[] list = new File[result.size()];
		// for (int i=0,j=result.size(); i<j; i++) list[i] = (File)
		// result.get(i);
		return result;
	}

	protected Set getClasspath() {
		return classpath;
	}

	protected String getContent() {
		return content;
	}

	protected File getDoc() {
		return doc;
	}

	protected ArrayList getImportConfig(String content) {
		ArrayList config = null;
		List info = getImportInfo(content);
		if (info != null) {
			config = new ArrayList(info);
			if (log.isDebugEnabled()) {
				String name = this.doc != null ? doc.getName()
						: "Unknow script ";
				log.debug(name + " found imports " + config);
			}
		}
		return config;
	}

	protected StringBuffer getImportedContent() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, j = importedDocs.size(); i < j; i++) {
			sb.append(((JavaScriptDocument) importedDocs.get(i))
					.getImportedContent());
		}
		sb.append(content);
		return sb;
	}

	protected ArrayList getImportedDocs() {
		return importedDocs;
	}

	protected ArrayList getImportedFiles() {
		ArrayList list = new ArrayList();
		for (int i = 0, j = importedDocs.size(); i < j; i++) {
			list.addAll(((JavaScriptDocument) importedDocs.get(i))
					.getImportedFiles());
		}
		list.add(doc);
		return list;
	}

	protected JavaScriptDocument getLinker() {
		return linker;
	}

	protected boolean isImportable(File file) throws LoopedImportException {
		if (this.doc.equals(file)) {
			log.error("File " + this.doc.getName() + " import itself");
			throw new LoopedImportException();
		}
		if (importedFiles.contains(file))
			return false;
		else {
			importedFiles.add(file);
			boolean importable = true;
			if (linker != null)
				importable = linker.isImportable(file);
			return importable;
		}
	}

	protected void load() throws Exception {
		if (this.doc == null)
			throw new JavaScriptNotFoundException();
		content = readFile(this.doc);

		this.setFileName();
		this.setFilePath();
	}

	protected void processImports(ArrayList config) throws Exception {
		if (config == null)
			return;
		for (int i = 0; i < config.size(); i++) {
			String item = config.get(i).toString();

			ArrayList classes = this.getClasses(item.replaceAll("\\.", "/")
					+ ".js");
			for (int j = 0; j < classes.size(); j++) {
				File file = (File) classes.get(j);
				if (isImportable(file)) {
					JavaScriptDocument jsDoc = new JavaScriptDocument(this,
							file, classpath);
					importedDocs.add(jsDoc);
					jsDoc.findImports();
				}
			}
		}
	}

	protected void setClasspath(Set classpath) {
		this.classpath = classpath;
	}

	protected void setDoc(File doc) throws JavaScriptNotFoundException {
		if (doc == null || !doc.isFile() || !doc.getName().endsWith(".js"))
			throw new JavaScriptNotFoundException();
		this.doc = new File(doc.getAbsolutePath());
	}

	protected void setDoc(String filePath) throws IllegalPathException,
			JavaScriptNotFoundException {
		this.setDoc(new File(filePath));
	}

	protected void setFileName() {
		this.fileName = this.doc.getName().substring(0,
				this.doc.getName().lastIndexOf(".js"));
	}

	protected void setFilePath() {
		this.filePath = this.doc.getAbsolutePath().substring(0,
				this.doc.getAbsolutePath().lastIndexOf(this.fileName));
	}

	protected void setLinker(JavaScriptDocument linker) {
		this.linker = linker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.fileName;
	}
}
