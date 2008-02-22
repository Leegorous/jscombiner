package net.leegorous.jsc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(TestJavaScriptDocument.class);
		suite.addTestSuite(TestJavaScriptCombiner.class);
		suite.addTestSuite(TestJavaScriptTestCaseCombiner.class);
		return suite;
	}
}
