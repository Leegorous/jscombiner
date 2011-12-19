package net.leegorous.jsc;

import static org.junit.Assert.assertEquals;

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
        String root = new File(clazz.getResource("/").toURI()).getAbsolutePath();
        String result = jsc.process("dev", root, "scripts/test", "pkg.b", null, "array");
        assertEquals(3, StringUtils.countMatches(result, ","));
    }

}
