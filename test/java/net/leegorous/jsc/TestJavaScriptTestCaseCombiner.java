package net.leegorous.jsc;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestJavaScriptTestCaseCombiner extends TestCase {

	private String classPath = "E:\\projects\\lab\\trunk\\jsunit\\";
	private String testScriptPath = "E:\\projects\\lab\\trunk\\jsunit\\tests\\";
	
	private String testFile1 = "testAssert.js";
	private String testFile2 = "testAssert2.js";
	private String testFile3 = "testAssert3.js";
	
	private String jsUnitPath = "E:\\projects\\lab\\trunk\\jsunit\\app\\jsUnitCore.js";
	
	public void testGetTemplate() throws Exception {
		String template = JavaScriptTestCaseCombiner.getTemplate();
		assertTrue("Test case template should include \"{![testCaseName]}\"",
				template.indexOf("{![testCaseName]}")>0);
	}
	
	public void testSaveTestCase() throws Exception {
		File file = new File(testScriptPath+testFile1.replaceAll(".js", ".html"));
		JavaScriptTestCaseCombiner.saveTestCase(file, 
				JavaScriptTestCaseCombiner.getTemplate());
	}
	
	public void testAssemble() throws Exception {
		ArrayList classPaths = new ArrayList();
		classPaths.add(new File(classPath));
		
		JavaScriptTestCaseCombiner jstcc = new JavaScriptTestCaseCombiner();
		jstcc.setDoc(new File(testScriptPath+testFile3));
		jstcc.setClassPaths(classPaths);
		jstcc.setJsUnit(new File(jsUnitPath));
		jstcc.assemble();
	}
}
