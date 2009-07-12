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
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaScriptDocument {
	
	private JavaScriptDocument linker;
	
	private File doc;
	
	private ArrayList importedFiles = new ArrayList();
	
	private ArrayList importedDocs = new ArrayList();
	
	private ArrayList classPaths;
	
	protected String fileName;
	protected String filePath;
	
	private String content;
	
	public JavaScriptDocument() {}
	
	public JavaScriptDocument(JavaScriptDocument linker, File file, ArrayList classPaths) throws JavaScriptNotFoundException {
		this.setLinker(linker);
		this.setDoc(file);
		this.setClassPaths(classPaths);
	}
	
	protected void load() throws Exception {
		if (this.doc==null) throw new JavaScriptNotFoundException();
		content = readFile(this.doc);
		
		this.setFileName();
		this.setFilePath();
	}
	
	protected void findImports() throws Exception {
		if (this.content==null) {
			this.load();
			if (this.content==null) return;
		}
		if (classPaths==null) classPaths = new ArrayList();
		
		ArrayList config = this.getImportConfig(content);
		if (config==null) return;
		
		classPaths.add(this.doc.getParentFile());
		
		processImports(config);
	}
	
	protected ArrayList getImportedFiles() {
		ArrayList list = new ArrayList();
		for (int i=0, j=importedDocs.size(); i<j; i++) {
			list.addAll(((JavaScriptDocument) importedDocs.get(i)).getImportedFiles());
		}
		list.add(doc);
		return list;
	}
	
	protected StringBuffer getImportedContent() {
		StringBuffer sb = new StringBuffer();
		for (int i=0, j=importedDocs.size(); i<j; i++) {
			sb.append(((JavaScriptDocument) importedDocs.get(i)).getImportedContent());
		}
		sb.append(content);
		return sb;
	}
	
	protected boolean isImportable(File file) throws LoopedImportException {
		if (this.doc.equals(file)) throw new LoopedImportException();
		if (importedFiles.contains(file)) return false;
		else {
			importedFiles.add(file);
			boolean importable = true;
			if (linker!=null) importable = linker.isImportable(file);
			return importable;
		}
	}
	
	protected void processImports(ArrayList config) throws Exception {
		if (config==null) return;
		for (int i = 0; i < config.size(); i++) {
			String item = config.get(i).toString();
			
			ArrayList classes = this.getClasses(item.replaceAll("\\.", "/")+".js");
			for (int j = 0; j < classes.size(); j++) {
				File file = (File) classes.get(j);
				if (isImportable(file)) {
					JavaScriptDocument jsDoc = new JavaScriptDocument(this, file, classPaths);
					importedDocs.add(jsDoc);
					jsDoc.findImports();
				}
			}
		}
	}
	
	public ArrayList getClasses(String path) throws IOException {
		ArrayList cp = new ArrayList();
		SubFileFilter filter = new SubFileFilter();
		String[] paths = path.split("/");
		ArrayList result = new ArrayList();
		if (paths.length<1) throw new IOException("Can not find "+path);
		ArrayList tmpCp = classPaths;
		for (int i=0,j=paths.length-1; i<j; i++) {
			String item = paths[i].trim();
			if (item.length()==0) continue;
			if (tmpCp.size()==0) throw new IOException("Can not find "+path);
			for (int m=0,n=tmpCp.size(); m<n; m++) {
				File f = (File) tmpCp.get(m);
				filter.setDir(f);
				filter.setName(item);
				File[] subs = f.listFiles(filter);
				if (subs.length==1) cp.add(subs[0]);
			}
			tmpCp = cp;
		}
		if (cp.size()==0) cp = classPaths;
		String item = paths[paths.length-1];
		for (int i=0,j=cp.size(); i<j; i++) {
			File f = (File) cp.get(i);
			filter.setDir(f);
			filter.setName(item);
			File[] subs = f.listFiles(filter);
			if (subs.length>0) {
				for (int m=0,n=subs.length; m<n; m++) result.add(subs[m]);
			}
		}
		if (result.size()==0) throw new IOException("Can not find "+path);
		//File[] list = new File[result.size()];
		//for (int i=0,j=result.size(); i<j; i++) list[i] = (File) result.get(i);
		return result;
	}
	
	protected ArrayList getImportConfig(String content) {
		Pattern pattern = Pattern.compile("@import (\\*|\\w+(\\.\\w+)*(\\.\\*)?);");
		Matcher m = pattern.matcher(content);
		ArrayList config = null;
		
		if (m.find()) {
			config = new ArrayList();
			do {
				if (m.groupCount()>1) {
					config.add(m.group(1));
				}
			} while (m.find());
		}
		return config;
	}
	
	protected static String readFile(File file) throws Exception {
		if (file==null) throw new JavaScriptNotFoundException();
		String charset = JavaScriptCombiner.getCharset(file);
		return StreamReader.read(file, charset);
	}

	protected JavaScriptDocument getLinker() {
		return linker;
	}

	protected void setLinker(JavaScriptDocument linker) {
		this.linker = linker;
	}

	protected File getDoc() {
		return doc;
	}

	protected void setDoc(File doc) throws JavaScriptNotFoundException {
		if (doc==null || !doc.isFile() || !doc.getName().endsWith(".js")) throw new JavaScriptNotFoundException();
		this.doc = new File(doc.getAbsolutePath());
	}

	protected ArrayList getClassPaths() {
		return classPaths;
	}

	protected void setClassPaths(ArrayList classPaths) {
		this.classPaths = classPaths;
	}

	protected ArrayList getImportedDocs() {
		return importedDocs;
	}

	protected String getContent() {
		return content;
	}

	protected void setFileName() {
		this.fileName = this.doc.getName().substring(0, this.doc.getName().lastIndexOf(".js"));
	}
	
	protected void setFilePath() {
		this.filePath = this.doc.getAbsolutePath().substring(0, 
				this.doc.getAbsolutePath().lastIndexOf(this.fileName));
	}
}
