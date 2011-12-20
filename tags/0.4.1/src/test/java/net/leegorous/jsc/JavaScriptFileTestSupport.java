package net.leegorous.jsc;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JavaScriptFileTestSupport extends TestCase {

	protected Log log = LogFactory.getLog(this.getClass());

	public JavaScriptFileTestSupport() {
		super();
	}

	public JavaScriptFileTestSupport(String name) {
		super(name);
	}

	protected String getFileName(String name) throws URISyntaxException {
		URL url = this.getClass().getResource(name);
		URI uri = new URI(url.toString());
		return uri.getPath();
	}

}