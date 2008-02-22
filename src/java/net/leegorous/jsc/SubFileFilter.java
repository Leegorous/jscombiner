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
import java.io.FilenameFilter;

/**
 * The class <code>SubFileFilter</code> is helper class to select the subfiles.
 * 
 * @author Leegorous
 *
 */
public class SubFileFilter implements FilenameFilter {
	private File dir;
	
	private NameFilter filter;
	
	public void setDir(File dir) {
		this.dir = dir;
	}
	
	/**
	 * Set the subfile name or its pattern.<br/>
	 * It can be the exactly subfile name like "foo.js".<br/>
	 * Or the intangibly pattern, specifying the suffix, like "*.js".<br/>
	 * Note that it only support the suffix pattern at this version.
	 * 
	 * @param name The subfile name
	 */
	public void setName(String name) {
		if (name.startsWith("*.")) filter = new SuffixNameFilter(name);
		else filter = new CertainNameFilter(name);
	}
	public boolean accept(File dir, String name) {
		if (dir.equals(this.dir) && filter.accept(name)) return true;
		return false;
	}
	
	private interface NameFilter {
		public boolean accept(String name) ;
	}
	
	private class CertainNameFilter implements NameFilter {
		private String name;
		
		public CertainNameFilter(String name) {
			this.name = name;
		}
		
		public boolean accept(String name) {
			if (name.equals(this.name)) return true;
			return false;
		}
	}
	
	private class SuffixNameFilter implements NameFilter {
		private String suffix;
		
		public SuffixNameFilter(String suffix) {
			this.suffix = suffix.replaceAll("\\*", "");
		}

		public boolean accept(String name) {
			if (name.endsWith(this.suffix)) return true;
			return false;
		}
		
	}
}
