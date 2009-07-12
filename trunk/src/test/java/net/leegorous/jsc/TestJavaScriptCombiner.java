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

import junit.framework.TestCase;

public class TestJavaScriptCombiner extends JavaScriptFileTestSupport {

	private String configPath;

	protected void setUp() throws Exception {
		super.setUp();
		configPath = getFileName("/scripts/test.cfg.js");
	}

	private StringBuffer testsConfig = null;

	private String testTarget0 = "test/";
	private String testTarget1 = "test/pkg/b.js";
	private String testTarget2 = "../tests/";

	private String getTestConfig(String target) {
		String prefix = " * @test ";
		String subfix = ";";
		testsConfig = new StringBuffer();

		testsConfig.append(prefix);
		testsConfig.append(target);
		testsConfig.append(subfix);

		return testsConfig.toString();
	}

	public void testFindMode() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		jsc.load();
		jsc.configMode();
		assertTrue(jsc.isAgentMode());
	}

	public void testFindClassPaths() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		jsc.load();
		jsc.findClasspath();

		assertEquals("It has classpaths.", 2, jsc.getClasspath().size());
	}

	public void testAssemble() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		jsc.assemble();
	}

	public void testReadJsUnitConfig() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		String content1 = " * @jsUnit jsunit/;";
		assertNotNull("It should find jsUnit core.", jsc
				.readJsUnitConfig(content1));

		content1 = " * @jsUnit .;";
		try {
			jsc.readJsUnitConfig(content1);
			fail("Exception expected.");
		} catch (IOException e) {
			assertNotNull(e.getMessage());
		}
	}

	public void testReadTestsConfig() throws Exception {

		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);

		// Read directory configuration
		ArrayList list = jsc.readTestConfig(getTestConfig("."));
		assertEquals(3, list.size());

		// Read file configuration
		list = jsc.readTestConfig(getTestConfig(testTarget1));
		assertEquals(1, list.size());
	}

	public void testReadTestConfigException() throws IllegalPathException,
			JavaScriptNotFoundException {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		try {
			ArrayList list = jsc.readTestConfig(getTestConfig(testTarget2));
			fail("IOException expected.");
		} catch (IOException e) {
			assertNotNull(e.getMessage());
		}
	}

	public void testSetDoc() throws IllegalPathException,
			JavaScriptNotFoundException {
		JavaScriptCombiner jsc = new JavaScriptCombiner();
		jsc.setDoc(configPath);
		File file = jsc.getDoc();
		TestCase.assertEquals(new File(configPath), file);
	}
}
