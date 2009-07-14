/**
 * 
 */
package net.leegorous.jsc;

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

}
