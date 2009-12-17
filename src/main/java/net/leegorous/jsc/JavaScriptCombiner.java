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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.leegorous.util.FileCharsetDetector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JavaScriptCombiner extends JavaScriptDocument {

	private boolean agentMode = false;

	private String agentFileName;
	private String scriptFileName;

	private static Pattern AGENT_PATTERN = Pattern
			.compile("@agent\\s*true\\s*;?\\s*\\r?\\n");

	protected Log log = LogFactory.getLog(this.getClass());

	protected static Pattern TEST_PATTERN = Pattern
			.compile("@test\\s*((\\.|([\\w\\.]+/)+)*((\\w+\\.)+js)?)"
					+ PATTERN_SUFFIX);

	public JavaScriptCombiner() {
	}

	public JavaScriptCombiner(String filePath) throws IllegalPathException,
			JavaScriptNotFoundException {
		this.setDoc(filePath);
	}

	public JavaScriptCombiner(JavaScriptCombiner linker, File file)
			throws JavaScriptNotFoundException {
		super(linker, file, null);
	}

	public void assemble() throws Exception {
		this.load();

		if (this.getLinker() == null)
			this.configMode();

		this.findBase();

		this.findClasspath();

		this.findImports();

		this.findTestConfig();

		this.save();
	}

	protected void findBase() throws Exception {
		Pattern pattern = Pattern
				.compile("@base ((\\.|([\\w\\.]+/)+)*(\\w+\\.)+cfg\\.js);");
		Matcher m = pattern.matcher(this.getContent());

		File baseFile;

		while (m.find()) {
			baseFile = this.getRelatedFile(m.group(1))[0];
			JavaScriptCombiner base = new JavaScriptCombiner(this, baseFile);
			base.setAgentMode(agentMode);
			base.assemble();
			this.getImportedDocs().add(base);
		}
	}

	/**
	 * @deprecated
	 */
	protected void findMode() {
		if (this.getContent().indexOf("@agent true;") != -1)
			this.setAgentMode(true);
	}

	protected void configMode() {
		Matcher m = AGENT_PATTERN.matcher(this.getContent());
		if (m.find()) {
			this.setAgentMode(true);
		}
	}

	protected void findClasspath() throws IOException {
		Matcher m = CLASSPATH_PATTERN.matcher(this.getContent());

		String path;
		Set paths = new TreeSet();

		paths.add(this.getDoc().getParentFile());

		while (m.find()) {
			path = m.group(1);
			File folder = this.getRelatedFile(path)[0];
			if (folder.isFile())
				folder = folder.getParentFile();
			if (!paths.contains(folder))
				paths.add(folder);
		}

		this.setClasspath(paths);
	}

	protected void findTestConfig() throws Exception {
		File jsUnit = this.readJsUnitConfig(this.getContent());
		if (jsUnit == null)
			return;

		ArrayList tests = this.readTestConfig(this.getContent());
		if (tests == null)
			return;

		StringBuffer sb = new StringBuffer();
		File testCase;
		String testCasePath;
		for (int i = 0, j = tests.size(); i < j; i++) {
			testCase = (File) tests.get(i);
			JavaScriptTestCaseCombiner jstcc = new JavaScriptTestCaseCombiner(
					testCase, this.getClasspath(), jsUnit);
			jstcc.assemble();

			testCasePath = JavaScriptCombiner.translate2RelatePath(this
					.getDoc(), testCase);
			testCasePath = testCasePath.substring(0, testCasePath
					.lastIndexOf(".js"))
					+ ".html";
			sb.append("testSuite.addTestPage(translatePath(\"");
			sb.append(testCasePath);
			sb.append("\"));");
		}

		File testSuiteName = new File(this.getTestSuiteName());
		String template = StreamReader.read(JavaScriptCombiner.class
				.getResourceAsStream("/TestSuiteTemplate.xml"));
		template = template.replaceAll("\\{!\\[testSuiteName]}", this.fileName);
		template = template.replaceAll("\\{!\\[jsUnitSource]}",
				JavaScriptCombiner.translate2RelatePath(this.getDoc(), jsUnit));
		template = template.replaceAll("\\{!\\[testSuiteScripts]}", sb
				.toString());
		saveFile(testSuiteName, template);
	}

	private String getTestSuiteName() {
		String path = this.getDoc().getAbsolutePath();
		path = path.substring(0, path.lastIndexOf(this.fileName));
		return path = path + "test" + this.fileName + ".html";
	}

	protected File readJsUnitConfig(String content) throws IOException {
		Pattern pattern = Pattern.compile("@jsUnit (\\.|([\\w\\.]+/)+);");
		Matcher m = pattern.matcher(content);
		File file = null;

		if (m.find()) {
			file = this.getRelatedFile(m.group(1) + "jsUnitCore.js")[0];
		}
		return file;
	}

	protected ArrayList readTestConfig(String content) throws IOException {
		Matcher m = TEST_PATTERN.matcher(content);
		ArrayList paths = null;
		if (m.find()) {
			paths = new ArrayList();
			do {
				if (m.groupCount() > 1) {
					paths.add(m.group(1));
				}
			} while (m.find());
		}

		ArrayList tests = new ArrayList();
		for (int i = 0, j = paths.size(); i < j; i++) {
			String path = paths.get(i).toString();
			File file = this.getRelatedFile(path)[0];

			if (file.isFile())
				tests.add(file);

			if (file.isDirectory()) {
				SubFileFilter filter = new SubFileFilter();
				filter.setDir(file);
				filter.setName("*.js");

				File[] files = file.listFiles(filter);

				for (int k = 0, v = files.length; k < v; k++) {
					tests.add(files[k]);
				}
			}
		}
		return tests;
	}

	protected void save() throws Exception {
		if (this.getLinker() != null)
			return;

		this.generateScript();

		this.generateAgent();
	}

	protected void generateAgent() throws Exception {
		String template = StreamReader.read(JavaScriptCombiner.class
				.getResourceAsStream("/AgentTemplate.xml"));

		File agentFile = new File(this.getAgentFileName());
		String moduleName = this.getDoc().getName().replaceAll("\\.cfg\\.js",
				"");
		template = template.replaceAll("\\{!\\[moduleName]}", moduleName);

		ArrayList imports = null;
		StringBuffer sb = new StringBuffer();
		if (this.isAgentMode())
			imports = this.getImportedFiles();
		else {
			imports = new ArrayList();
			imports.add(new File(this.getScriptFileName()));
		}

		for (int i = 0, j = imports.size(); i < j; i++) {
			sb
					.append("<script language=\"JavaScript\" type=\"text/javascript\" src=\"");
			sb
					.append(translate2RelatePath(this.getDoc(), (File) imports
							.get(i)));
			sb.append("\"></script>\r\n");
		}
		template = template.replaceAll("\\{!\\[scriptLinks]}", sb.toString());

		saveFile(agentFile, template);
	}

	protected void generateScript() throws Exception {
		File scriptFile = new File(this.getScriptFileName());

		saveFile(scriptFile, this.getImportedContent().toString());
	}

	private String getAgentFileName() {
		if (agentFileName == null) {
			// String filePath = this.getDoc().getAbsolutePath();
			agentFileName = this.filePath + this.fileName + ".html";
			// agentFileName = filePath.substring(0,
			// filePath.lastIndexOf(".cfg.js"))+".html";
		}
		return agentFileName;
	}

	private String getScriptFileName() {
		if (scriptFileName == null) {
			// String filePath = this.getDoc().getAbsolutePath();
			// scriptFileName = filePath.substring(0,
			// filePath.lastIndexOf(".cfg.js"))+".js";
			scriptFileName = this.filePath + this.fileName + ".js";
		}
		return scriptFileName;
	}

	protected static void saveFile(File file, String content) throws Exception {
		StreamWriter.write(file, content, "UTF-8");
	}

	/**
	 * Try to get the encoding of the specified file.
	 * 
	 * @param file
	 *            The file to be analyzed
	 * @return The encoding of the file or the probable one
	 * @throws Exception
	 *             If an error occurs
	 */
	public static String getCharset(File file) throws Exception {
		String charset = null;
		FileCharsetDetector detector = new FileCharsetDetector(
				new FileInputStream(file));
		if ((charset = detector.getResult()) == null)
			if (detector.getProbableResult() != null
					&& detector.getProbableResult().length > 0)
				charset = detector.getProbableResult()[0];
			else
				charset = "UTF-8";
		// System.out.println("CharSet is: "+charset);
		return charset;
	}

	public File[] getRelatedFile(String path) throws IOException {
		return this.getRelatedFile(this.getDoc().getParentFile(), path);
	}

	public File[] getRelatedFile(File source, String path) throws IOException {
		File target = source;
		File[] result = null;
		if (target.isFile())
			target = target.getParentFile();
		SubFileFilter filter = new SubFileFilter();
		if (!path.equals(".")) {
			String[] paths = path.split("/");
			for (int i = 0; i < paths.length; i++) {
				String item = paths[i].trim();
				if (item.length() == 0)
					continue;
				if (!item.equals(".")) {
					if (item.equals(".."))
						target = target.getParentFile();
					else {
						filter.setDir(target);
						filter.setName(item);
						File[] subs = target.listFiles(filter);
						if (subs.length > 0) {
							if (subs.length == 1)
								target = subs[0];
							result = subs;
						} else
							throw new IOException("Cannot find a file named \""
									+ item + "\" in \""
									+ target.getAbsolutePath() + "\"");
					}
				}
			}
		} else {
			result = new File[] { target };
		}
		return result;
	}

	public static String translate2RelatePath(File standard, File file)
			throws IOException {
		if (standard.isFile())
			standard = standard.getParentFile();
		String standardPath = standard.getCanonicalPath().replaceAll("\\\\",
				"/");
		File target = file;
		Stack stack = new Stack();
		if (file.isFile()) {
			stack.push(target.getName());
			target = file.getParentFile();
		}
		String targetPath = file.getCanonicalPath().replaceAll("\\\\", "/");
		StringBuffer result = new StringBuffer();
		while (!targetPath.startsWith(standardPath)) {
			standardPath = standardPath.substring(0, standardPath
					.lastIndexOf("/"));
			result.append("../");
		}
		while (!standardPath.equals(target.getCanonicalPath().replaceAll(
				"\\\\", "/"))) {
			stack.push(target.getName());
			target = target.getParentFile();
		}
		while (stack.size() > 1) {
			result.append((String) stack.pop()).append("/");
		}
		if (stack.size() > 0)
			result.append((String) stack.pop());
		// System.out.println(standardPath+"\n"+targetPath);
		return result.toString();
	}

	protected void setAgentMode(boolean agentMode) {
		this.agentMode = agentMode;
	}

	protected boolean isAgentMode() {
		return agentMode;
	}

	protected void setFileName() {
		this.fileName = this.getDoc().getName().substring(0,
				this.getDoc().getName().lastIndexOf(".cfg.js"));
	}

}
