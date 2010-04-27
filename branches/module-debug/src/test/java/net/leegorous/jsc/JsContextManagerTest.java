/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

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

	public void testAddClasspath() throws URISyntaxException {
		mgr.addClasspath(getFileName("/scripts/test"));
		assertEquals(1, mgr.getPackages().size());
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

	public void testGetClazz() throws Exception {
		mgr.addClasspath(getFileName("/scripts/test"));
		JsFile js = mgr.getClazz("pkg.b");
		assertNotNull(js);
		log.debug(js);
	}

	public void testGetClazzes() throws Exception {
		mgr.addClasspath(getFileName("/scripts/test"));
		Set clazzes = mgr.getClazzes("pkg.*");
		assertTrue(clazzes.size() > 4);
		log.debug(clazzes);
	}

	public void testGetJs() throws Exception {
		JsFile js = mgr.getJs(getFileName("/scripts/test/pkg/b.js"));
		assertNotNull(js);
		log.debug(js);
	}

	public void testGetJsClass() throws Exception {
		File file = new File(getFileName("/scripts/test"));
		mgr.configClasspath(file);
		JsFile js = mgr.getJsClass("a");
		assertNotNull(js);
	}

	public void testGetJsClasses() throws Exception {
		File file = new File(getFileName("/scripts/test"));
		mgr.configClasspath(file);
		List list = mgr.getJsClasses("*");
		assertTrue(list.size() > 1);
		log.debug(list);
	}

	public void testRefreshJsFile() throws Exception {
		JsFile js = new JsFile();
		File file = new File(getFileName("/scripts/test/pkg/b.js"));
		mgr.refresh(js, file);

		assertEquals("testB", js.getModule());
		assertEquals("b", js.getName());
		assertEquals("pkg.b", js.getClazz());
		// To make sure the classpaths have been added
		assertEquals(2, mgr.getClasspath().size());

		// To make sure it got the import info
		assertEquals(2, js.getImported().size());
	}

	public void testRefreshPackages() throws Exception {
		String path = getFileName("/scripts");
		JsPackage pkg = new JsPackage();
		pkg.add(path);
		mgr.addPackage(pkg);
		mgr.refreshPackages();
		assertEquals(4, mgr.getPackages().size());
	}

}
