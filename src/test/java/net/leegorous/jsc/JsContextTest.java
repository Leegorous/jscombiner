/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.util.Calendar;

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
		File file = new File(name);
		Calendar d = Calendar.getInstance();
		d.setTimeInMillis(file.lastModified());
		System.out.println(d.getTime());
		JsFile f = ctx.load(name);

	}

}
