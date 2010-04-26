/**
 * 
 */
package net.leegorous.jsc;

import java.util.HashMap;

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

	public void testGetName() {
		assertEquals("", pkg.getName());
	}

	public void testRefresh() {
		HashMap pkgs = new HashMap();
		pkgs.put(pkg.getName(), pkg);
		pkg.refresh(pkgs);
		assertEquals(4, pkgs.size());
	}

}
