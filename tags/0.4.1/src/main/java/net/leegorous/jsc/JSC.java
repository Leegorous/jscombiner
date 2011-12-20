/*
 * Copyright 2008 leegorous.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import net.leegorous.util.ArrayFormBuilder;
import net.leegorous.util.FileFormBuilder;
import net.leegorous.util.OutputBuilder;
import net.leegorous.util.TagsFormBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JSC {

    public static interface PathResolver {
        public String getPath(String str);
    }

    public static class ServletPathResolver implements PathResolver {
        private ServletContext ctx;

        public ServletPathResolver(ServletContext ctx) {
            this.ctx = ctx;
        }

        public String getPath(String str) {
            String p = ctx.getRealPath(str);
            File file = new File(p);
            try {
                p = file.getCanonicalPath();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return p;
        }
    }

    protected static Log log = LogFactory.getLog(JSC.class);

    public static final String OUTPUT_ARRAY = "array";

    public static final String OUTPUT_TAGS = "tags";

    public static final String OUTPUT_FILE = "file";

    private Map cpMap = new HashMap();

    private JsContextManager mgr;

    private JsContextManager getJsContextMgr() {
        if (mgr == null) {
            mgr = new JsContextManager();
        }
        return mgr;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0 || args.length % 2 == 1) {
            System.out.println("Usage: JSA -path configPath");
        } else {
            for (int i = 0; i < args.length; i += 2) {
                if (args[i].equals("-path")) {
                    System.out.println("Set configurate path: " + args[i + 1]);
                    try {
                        JavaScriptCombiner jsa = new JavaScriptCombiner(args[i + 1]);
                        jsa.assemble();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Done.");
    }

    private PathResolver pathResolver;

    private void addClasspath(JsContextManager mgr, String rootPath, String cp) {
        if (cp == null) return;
        List classpath = (List) cpMap.get(cp);
        if (classpath != null) return;
        List cps = normalizePath(cp);
        List result = new ArrayList();
        for (Iterator it = cps.iterator(); it.hasNext();) {
            String str = (String) it.next();
            File f = new File(getRealPath("/" + str));
            if (f.exists()) {
                mgr.addClasspath(f);
                result.add(f.getAbsoluteFile());
                continue;
            }
            String path = FilenameUtils.concat(rootPath, str);
            mgr.addClasspath(path);
            result.add(path);
        }
        cpMap.put(cp, result);
    }

    private String getRealPath(String path) {
        return pathResolver.getPath(path);
    }

    public List normalizePath(String path) {
        List result = null;
        if (path != null) {
            result = new ArrayList();
            path = path.replaceAll("[\\r\\n]", ";");
            String[] li = StringUtils.split(path, " ;");
            for (int i = 0; i < li.length; i++) {
                String str = StringUtils.trimToNull(li[i]);
                if (str != null) result.add(str);
            }
        }
        return result;
    }

    public String process(String cp, String path, String outputType, String output) {
        if (OUTPUT_FILE.equals(outputType) && output == null) {
            throw new IllegalArgumentException(
                    "property 'output' could not be null when output to 'file'");
        }

        path = StringUtils.trimToNull(path);
        if (path == null) return null;

        JsContextManager mgr = getJsContextMgr();
        String root = getRealPath("/");
        log.debug(root);
        addClasspath(mgr, root, cp);

        JsContext ctx = mgr.createContext();

        List li = normalizePath(path);
        for (Iterator it = li.iterator(); it.hasNext();) {
            String item = (String) it.next();
            try {
                ctx.buildHierarchy(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List list = null;
        try {
            JsNode node = ctx.getHierarchy();
            list = node.serialize();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        for (Iterator it = list.iterator(); it.hasNext();) {
            ((JsNode) it.next()).getFile().setWebRoot(root);
        }

        OutputBuilder builder = null;
        if (OUTPUT_ARRAY.equals(outputType)) {
            builder = new ArrayFormBuilder();
        } else if (OUTPUT_FILE.equals(outputType)) {
            builder = new FileFormBuilder(output, pathResolver);
        } else {
            builder = new TagsFormBuilder();
        }
        return builder.build(list);
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

}
