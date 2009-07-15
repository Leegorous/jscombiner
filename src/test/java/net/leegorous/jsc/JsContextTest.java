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

	private JsContext ctx;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		ctx = new JsContextManager().createContext();
		super.setUp();
	}

	public void testLoad() throws Exception {
		String name = getFileName("/scripts/test/pkg/b.js");
		ctx.load(name);
		assertEquals(3, ctx.getList().size());
		log.debug(ctx.getList());
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
