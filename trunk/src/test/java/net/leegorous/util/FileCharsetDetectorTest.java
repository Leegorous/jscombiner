/**
 * 
 */
package net.leegorous.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * @author leegorous
 * 
 */
public class FileCharsetDetectorTest {

	FileCharsetDetector det;

	private FileCharsetDetector create(String file) throws IOException {
		return new FileCharsetDetector(this.getClass()
				.getResourceAsStream(file));
	}

	/**
	 * Test method for
	 * {@link net.leegorous.util.FileCharsetDetector#getResult()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetResult() throws IOException {
		det = create("/scripts/test.cfg.js");
		assertEquals("ASCII", det.getResult());
	}

	@Test
	public void testProbableResult() throws IOException {
		det = create("/chardet-test.txt");
		assertTrue(!det.isFound());
		String[] prob = det.getProbableResult();
		assertNotNull(prob);
		boolean foundUTF8 = false;
		for (int i = 0; i < prob.length; i++) {
			if ("UTF-8".equals(prob[i])) {
				foundUTF8 = true;
				break;
			}
		}
		assertTrue(foundUTF8);
	}

}
