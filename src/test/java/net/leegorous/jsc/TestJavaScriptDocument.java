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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestJavaScriptDocument extends JavaScriptFileTestSupport {

	protected Log log = LogFactory.getLog(this.getClass());

	private void _testFindImport() throws Exception {
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		Set classPaths = new TreeSet();
		classPaths.add(new File(getFileName("/scripts")));
		JavaScriptDocument jsdoc = new JavaScriptDocument(null, file,
				classPaths);
		jsdoc.findImports();

		assertNotNull(jsdoc.getImportedDocs());
		assertEquals("b.js only imported 2 file.", 2, jsdoc.getImportedDocs()
				.size());
		assertEquals("It should be 3 documents imported in total.", 3, jsdoc
				.getImportedFiles().size());

		String content = jsdoc.getImportedContent().toString();
		assertTrue("a.js should be imported.", content.indexOf("var A;") > 0);
		assertTrue("c.js should be imported.", content.indexOf("var C;") > 0);
	}

	public void testFindImportException() throws Exception {
		File file = new File(getFileName("/scripts/test/pkg/n1.js"));
		Set classpath = new TreeSet();
		classpath.add(new File(getFileName("/scripts/test")));
		try {
			JavaScriptDocument jsdoc = new JavaScriptDocument(null, file,
					classpath);
			jsdoc.findImports();
			fail("Cycle import exception excepted.");
		} catch (LoopedImportException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			fail("Exception unexcepted.");
		}
	}

	public void testGetClassName() throws Exception {
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		String content = JavaScriptDocument.readFile(file);
		String name = JavaScriptDocument.getClassName(content);
		assertEquals("pkg.b", name);
	}

	public void testGetImportConfig() throws Exception {
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		String content = JavaScriptDocument.readFile(file);
		JavaScriptDocument jsdoc = new JavaScriptDocument();
		ArrayList config = jsdoc.getImportConfig(content);

		assertEquals("a", config.get(0).toString());
		assertEquals("pkg.c", config.get(1).toString());
		assertTrue(config.size() >= 2);
	}

	public void testReadFile() throws Exception {
		File file = new File(getFileName("/scripts/a.js"));
		JavaScriptDocument.readFile(file);
		log.debug(file.getAbsoluteFile() + (file.exists() ? "" : "does not")
				+ " exist");
	}

	public void testReadFileException() throws URISyntaxException {
		File file = new File(getFileName("/scripts/a.js") + "s");
		try {
			JavaScriptDocument.readFile(file);
			fail("Exception expected.");
		} catch (Exception e) {
			log.debug(e.getMessage());
			assertNotNull(e.getMessage());
		}
	}

	public void testResolveClasspath() throws Exception {
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		File path = JavaScriptDocument.resolveClasspath(file);
		assertEquals(new File(getFileName("/scripts/test")), path);
	}

	public void testConfigClasspath() throws Exception {
		File file = new File(getFileName("/scripts/test/config.js"));
		Set cp = JavaScriptDocument.configClasspath(file);
		assertTrue(cp.contains(new File(getFileName("/scripts"))));
	}

}
