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
import java.util.Set;
import java.util.TreeSet;

public class TestJavaScriptTestCaseCombiner extends JavaScriptFileTestSupport {

	private String classPath;
	private String testScriptPath;

	private String testFile1 = "testAssert.js";
	private String testFile3 = "testAssert3.js";

	private String jsUnitPath;

	protected void setUp() throws Exception {
		super.setUp();
		classPath = getFileName("/scripts");
		testScriptPath = getFileName("/scripts/test");
		jsUnitPath = getFileName("/scripts/jsunit/jsUnitCore.js");
	}

	public void testGetTemplate() throws Exception {
		String template = JavaScriptTestCaseCombiner.getTemplate();
		assertTrue("Test case template should include \"{![testCaseName]}\"",
				template.indexOf("{![testCaseName]}") > 0);
	}

	public void testSaveTestCase() throws Exception {
		File file = new File(testScriptPath
				+ testFile1.replaceAll(".js", ".html"));
		JavaScriptTestCaseCombiner.saveTestCase(file,
				JavaScriptTestCaseCombiner.getTemplate());
	}

	public void testAssemble() throws Exception {
		Set classPaths = new TreeSet();
		classPaths.add(new File(classPath));

		JavaScriptTestCaseCombiner jstcc = new JavaScriptTestCaseCombiner();
		jstcc.setDoc(new File(testScriptPath + "/pkg/b.js"));
		jstcc.setClasspath(classPaths);
		jstcc.setJsUnit(new File(jsUnitPath));
		jstcc.assemble();
	}
}
