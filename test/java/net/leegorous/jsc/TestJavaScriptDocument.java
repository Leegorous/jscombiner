package net.leegorous.jsc;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestJavaScriptDocument extends TestCase {
	
	private String classPath = "E:\\projects\\lab\\trunk\\jsunit\\";
	private String testScriptPath = "E:\\projects\\lab\\trunk\\jsunit\\tests\\";
	
	private String testFile1 = "testAssert.js";
	private String testFile2 = "testAssert2.js";
	private String testFile3 = "testAssert3.js";
	
	public void testFindImport() throws Exception {
		File file = new File(testScriptPath+testFile3);
		ArrayList classPaths = new ArrayList();
		classPaths.add(new File(classPath));
		JavaScriptDocument jsdoc = new JavaScriptDocument(null, file, classPaths);
		jsdoc.findImports();
		
		assertNotNull(jsdoc.getImportedDocs());
		assertEquals("testAssert3.js only imported 1 file.",1, jsdoc.getImportedDocs().size());
		assertEquals("It should be 3 documents imported in total.",3, jsdoc.getImportedFiles().size());
		
		String content = jsdoc.getImportedContent().toString();
		assertTrue("C.js should be imported.",content.indexOf("C = 1;")>0);
		assertTrue("D.js should be imported.",content.indexOf("D = 2;")>0);
	}
	
	public void testFindImportException() {
		File file = new File(testScriptPath + testFile2);
		ArrayList classPaths = new ArrayList();
		classPaths.add(new File(classPath));
		try {
			JavaScriptDocument jsdoc = new JavaScriptDocument(null, file, classPaths);
			jsdoc.findImports();
			fail("Cycle import exception excepted.");
		} catch (LoopedImportException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			fail("Exception unexcepted.");
		}
	}
	
	public void testGetImportConfig() throws Exception {
		String config0 = "util.ObjectLocker";
		File file = new File(testScriptPath+testFile1);
		String content = JavaScriptDocument.readFile(file);
		JavaScriptDocument jsdoc = new JavaScriptDocument();
		ArrayList config = jsdoc.getImportConfig(content);
		
		assertEquals(config0, config.get(0).toString());
		assertEquals(false, config.contains("importStateWithoutSemicolon"));
	}
	
	public void testReadFile() throws Exception {
		File file = new File(testScriptPath+testFile1);
		JavaScriptDocument.readFile(file);
	}
	
	public void testReadFileException() {
		File file = new File(testScriptPath+testFile1+"s");
		try {
			JavaScriptDocument.readFile(file);
			fail("Exception expected.");
		} catch (Exception e) {
			assertNotNull(e.getMessage());
		}
	}

}
