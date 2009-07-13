/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;

/**
 * @author leegorous
 * 
 */
public class JsContextManagerTest extends JavaScriptFileTestSupport {

	private JsContextManager mgr;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		mgr = new JsContextManager();
		super.setUp();
	}

	public void testConfigClasspath() throws Exception {
		File file = new File(getFileName("/scripts/test"));
		mgr.configClasspath(file);
		assertEquals(2, mgr.getClasspath().size());
	}

	public void testCreateJsContext() {
		JsContext ctx = mgr.createContext();
		assertEquals(mgr, ctx.getContextManager());
	}

	public void testGetJs() throws Exception {
		JsFile file = mgr.getJs(getFileName("/scripts/test/pkg/b.js"));
		assertNotNull(file);
		log.debug(file);
	}

	public void testRefreshJsFile() throws Exception {
		JsFile js = new JsFile();
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		mgr.refresh(js, file);

		assertEquals("pkg.b", js.getName());
		// To make sure the classpaths have been added
		assertEquals(2, mgr.getClasspath().size());

		// To make sure it got the import info
		assertEquals(2, js.getImported().size());
	}

}
