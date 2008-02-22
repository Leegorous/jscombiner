package net.leegorous.jsc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestJavaScriptCombiner extends TestCase {
	
	private String configPath = "E:\\projects\\lab\\trunk\\jsunit\\test.cfg.js.cfg.js";
	
	private StringBuffer testsConfig = null;
	
	private String testTarget0 = "tests/";
	private String testTarget1 = "tests/testAssert.js";
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
		jsc.findMode();
		assertTrue(jsc.isAgentMode());
	}
	
	public void testFindClassPaths() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		jsc.load();
		jsc.findClassPaths();
		
		assertTrue("It has classpaths.", jsc.getClassPaths().size()==2);
	}
	
	public void testAssemble() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		jsc.assemble();
	}
	
	public void testReadJsUnitConfig() throws Exception {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		String content1 = " * @jsUnit app/;";
		assertNotNull("It should find jsUnit core.", jsc.readJsUnitConfig(content1));
		
		content1 = " * @jsUnit .;";
		try {
			jsc.readJsUnitConfig(content1);
			fail("Exception expected.");
		} catch(IOException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	public void testReadTestsConfig() throws Exception {
		
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		
		// Read directory configuration
		ArrayList list = jsc.readTestConfig(getTestConfig(testTarget0));
		assertEquals(3,list.size());
		
		// Read file configuration
		list = jsc.readTestConfig(getTestConfig(testTarget1));
		assertEquals(1,list.size());
	}
	
	public void testReadTestConfigException() throws IllegalPathException, JavaScriptNotFoundException {
		JavaScriptCombiner jsc = new JavaScriptCombiner(configPath);
		try {
			ArrayList list = jsc.readTestConfig(getTestConfig(testTarget2));
			fail("IOException expected.");
		} catch (IOException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	public void testSetDoc() throws IllegalPathException, JavaScriptNotFoundException {
		JavaScriptCombiner jsc = new JavaScriptCombiner();
		jsc.setDoc(configPath);
		File file = jsc.getDoc();
		TestCase.assertEquals(configPath, file.getAbsolutePath());
	}
}
