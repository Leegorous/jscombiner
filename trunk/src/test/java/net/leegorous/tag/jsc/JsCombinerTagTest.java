/**
 * 
 */
package net.leegorous.tag.jsc;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author leegorous
 * 
 */
public class JsCombinerTagTest extends TestCase {

	private JsCombinerTag tag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		tag = new JsCombinerTag();
		super.setUp();
	}

	public void testNormalizePath() {
		List list = tag.normalizePath("\r\n\tab.c; \tee \n dd\r\n  oo\r\n  ");
		System.out.println(list);
		assertEquals(4, list.size());

		list = tag.normalizePath("\r\n");
		assertEquals(0, list.size());

		list = tag.normalizePath(null);
		assertNull(list);
	}
}
