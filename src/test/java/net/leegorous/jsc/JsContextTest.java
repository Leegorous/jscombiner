/**
 * 
 */
package net.leegorous.jsc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leegorous
 * 
 */
public class JsContextTest extends JavaScriptFileTestSupport {

	private JsContextManager mgr;
	private JsContext ctx;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		mgr = new JsContextManager();
		ctx = mgr.createContext();
		super.setUp();
	}

	public void testBuildHierarchy() throws Exception {
		mgr.addClasspath(getFileName("/scripts/test"));
		ctx.buildHierarchy("pkg.b");
		JsNode hierarchy = ctx.getHierarchy();
		assertNotNull(hierarchy.getFile());

		ctx.buildHierarchy("pkg.d");
		hierarchy = ctx.getHierarchy();
		assertNull(hierarchy.getFile());
		assertEquals(2, hierarchy.getChilds().size());

		List list = hierarchy.serialize();
		log.debug(list);

		// without classpath config, no more 5 but only 4 js imported
		assertEquals(4, list.size());
	}

	public void testBuildHierarchyLoopImported() throws Exception {
		mgr.addClasspath(getFileName("/scripts/test"));
		// String name = getFileName("/scripts/test/pkg/n1.js");
		try {
			ctx.buildHierarchy("pkg.n1");
			fail("LoopedImportException expected");
		} catch (Exception e) {
			assertTrue(e instanceof LoopedImportException);
		}
	}

	public void testGetScriptsContent() throws Exception {
		String name = getFileName("/scripts/test/pkg/b.js");
		ctx.load(name);
		String content = ctx.getScriptsContent();
		log.debug(content);
	}

	public void testLoad() throws Exception {
		String name = getFileName("/scripts/test/pkg/b.js");
		ctx.load(name);
		List list = ctx.getList();
		log.debug(list);
		assertEquals(4, list.size());
	}

	public void testLoadLoopImported() throws Exception {
		String name = getFileName("/scripts/test/pkg/n1.js");
		try {
			ctx.load(name);
			fail("LoopedImportException expected");
		} catch (Exception e) {
			assertTrue(e instanceof LoopedImportException);
		}
	}

	public void testMergeList() {
		List arr1 = new ArrayList() {
			{
				add("a");
				add("b");
				add("c");
				add("d");
				add("e");
			}
		};

		List arr2 = new ArrayList() {
			{
				add("k");
				add("c");
				add("m");
				add("g");
				add("d");
			}
		};

		List result = ctx.mergeList(arr1, arr2);
		assertEquals(8, result.size());

		arr1 = new ArrayList();
		assertEquals(arr2, ctx.mergeList(arr1, arr2));
	}

}
