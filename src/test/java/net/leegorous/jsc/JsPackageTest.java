/**
 * 
 */
package net.leegorous.jsc;

import java.util.HashMap;
import java.util.Set;

/**
 * @author leegorous
 * 
 */
public class JsPackageTest extends JavaScriptFileTestSupport {

	private JsPackage pkg;

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
		pkg = new JsPackage();
		pkg.add(getFileName("/scripts"));
	}

	public void testAdd() throws Exception {
		JsPackage p = new JsPackage();
		String path = getFileName("/") + "dummy";
		p.add(path);

		pkg.add(p);
		assertEquals(2, pkg.getPaths().size());
	}

	public void testGetJs() throws Exception {
		JsFile js = pkg.getJs("a");
		assertNotNull(js);
		assertEquals("a", js.getClazz());
		assertTrue(js.getLength() > 0);
	}

	public void testGetName() {
		assertEquals("", pkg.getName());
	}

	public void testListClazz() {
		Set clazzes = pkg.listClazz();
		assertTrue(clazzes.size() > 1);
		assertTrue(clazzes.contains("a") && clazzes.contains("global"));
	}

	public void testRefresh() {
		HashMap pkgs = new HashMap();
		pkgs.put(pkg.getName(), pkg);
		pkg.refresh(pkgs);
		assertEquals(4, pkgs.size());
	}

}
