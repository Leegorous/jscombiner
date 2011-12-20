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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import net.leegorous.util.ArrayFormBuilder;
import net.leegorous.util.OutputBuilder;
import net.leegorous.util.ScriptListBuilder;

import org.apache.commons.io.FileUtils;
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

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String MODE_DEV = "dev";

    public static final String MODE_PROD = "prod";

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

    private String preTag = "<script src=\"";

    private String subTag = "\" language=\"JavaScript\" type=\"text/javascript\"></script>\n";

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

    private File getOutput(String path, long lastModified) {
        int idx = path.lastIndexOf('/');
        String folderPath;
        String fileName;
        if (idx < 0) {
            folderPath = "/";
            fileName = path;
        } else {
            String f = path.substring(0, idx + 1);
            if (f.charAt(0) != '/') f = "/" + f;
            folderPath = f;
            fileName = path.substring(idx + 1);
        }
        File folder = new File(getRealPath(folderPath));
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (fileName.endsWith(".js")) {
            fileName = fileName.substring(0, fileName.length() - 3);
        }

        final String qName = fileName;
        final long[] last = new long[] { 0 };
        final String[] fName = new String[1];
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith(qName + ".") && name.endsWith(".js")) {
                    String tail = name.substring(qName.length() + 1, name.length() - 3);
                    if (StringUtils.isNumeric(tail)) {
                        long a = Long.parseLong(tail);
                        if (a > last[0]) {
                            last[0] = a;
                            fName[0] = name;
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.getName().equals(fName[0])) {
                    f.delete();
                } else if (last[0] < lastModified) {
                    f.delete();
                }
            }
        }

        if (last[0] > lastModified) fileName = fName[0];
        else
            fileName = fileName + "." + (new Date().getTime()) + ".js";

        return new File(folder, fileName);
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

    public String process(String mode, String cp, String path, String output, String outputType) {
        if (MODE_PROD.equals(mode) && output == null) {
            throw new IllegalArgumentException("property 'output' could not be null in 'prod' mode");
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

        StringBuffer result = null;
        if (mode == null || MODE_DEV.equals(mode)) {
            OutputBuilder builder = null;
            if ("array".equals(outputType)) {
                builder = new ArrayFormBuilder();
            } else {
                builder = new ScriptListBuilder();
            }
            return builder.build(list);
        }

        if (MODE_PROD.equals(mode)) {
            long last = 0;
            JsFile js = null;
            for (Iterator it = list.iterator(); it.hasNext();) {
                js = ((JsNode) it.next()).getFile();
                if (js.getLastModified() > last) last = js.getLastModified();
            }
            File file = getOutput(output, last);
            String scriptPath = js.getWebPath(file.getAbsolutePath());

            if (!file.exists()) {
                String content = "";
                try {
                    content = ctx.getScriptsContent();
                    FileUtils.writeStringToFile(file, content, DEFAULT_ENCODING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            StringBuffer buf = new StringBuffer();
            buf.append(preTag);
            buf.append(scriptPath);
            buf.append(subTag);
            result = buf;
        }
        return result.toString();
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

}
