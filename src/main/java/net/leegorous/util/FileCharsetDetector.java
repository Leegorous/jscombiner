/**
 * 
 */
package net.leegorous.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * 
 * @author leegorous
 * 
 */
public class FileCharsetDetector {

	private boolean found;

	public boolean isFound() {
		return found;
	}

	private String result;
	private String[] probableCharsets;

	public FileCharsetDetector(InputStream in) throws IOException {
		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				found = true;
				result = charset;
			}
		});

		BufferedInputStream imp = new BufferedInputStream(in);

		byte[] buf = new byte[1024];
		int len;
		boolean done = false;
		boolean isAscii = true;

		while ((len = imp.read(buf, 0, buf.length)) != -1) {

			// Check if the stream is only ascii.
			if (isAscii)
				isAscii = det.isAscii(buf, len);

			// DoIt if non-ascii and not done yet.
			if (!isAscii && !done)
				done = det.DoIt(buf, len, false);
		}
		det.DataEnd();

		if (isAscii) {
			result = "ASCII";
			found = true;
		}

		if (!found) {
			probableCharsets = det.getProbableCharsets();
		}

	}

	public String getResult() {
		return result;
	}

	public String[] getProbableResult() {
		return probableCharsets;
	}

}
