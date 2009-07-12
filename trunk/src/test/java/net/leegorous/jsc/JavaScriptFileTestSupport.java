package net.leegorous.jsc;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

public abstract class JavaScriptFileTestSupport extends TestCase {

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