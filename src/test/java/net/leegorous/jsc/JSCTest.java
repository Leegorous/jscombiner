package net.leegorous.jsc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import net.leegorous.jsc.JSC.PathResolver;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class JSCTest {

    @Test
    public void test() throws URISyntaxException {
        final Class clazz = this.getClass();
        JSC jsc = new JSC();
        jsc.setPathResolver(new PathResolver() {
            public String getPath(String str) {
                try {
                    File f = new File(clazz.getResource(str).toURI());
                    return f.getAbsolutePath();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        String result = jsc.process("scripts/test", "pkg.b", "array", null);
        assertEquals(3, StringUtils.countMatches(result, ","));

        result = jsc.process("scripts/test", "pkg.b", null, null);
        assertEquals(3, StringUtils.countMatches(result, "\n"));

        result = jsc.process("scripts/test", "pkg.b", "file", "all.js");
        assertTrue(result.indexOf("all.") != -1);
    }
}
