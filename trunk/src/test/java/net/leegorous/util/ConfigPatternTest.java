/**
 * 
 */
package net.leegorous.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author leegorous
 * 
 */
public class ConfigPatternTest {

	private ConfigPattern pattern;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		pattern = new ConfigPattern("module", "\\w*");
	}

	@Test
	public void testMatchFirst() {
		String value = pattern.getValue("@module abc;");
		assertEquals("abc", value);
	}
}
