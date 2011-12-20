/**
 * 
 */
package net.leegorous.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.leegorous.jsc.JSC.PathResolver;
import net.leegorous.jsc.JavaScriptDocument;
import net.leegorous.jsc.JsContext;
import net.leegorous.jsc.JsFile;
import net.leegorous.jsc.JsNode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author leegorous
 * 
 */
public class FileFormBuilder implements OutputBuilder {

    protected Log log = LogFactory.getLog(FileFormBuilder.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    private String output;
    private PathResolver pathResolver;

    public FileFormBuilder(String output, PathResolver pathResolver) {
        setOutput(output);
        setPathResolver(pathResolver);
    }

    public String build(List jsNodes) {
        long last = 0;
        JsFile js = null;
        for (Iterator it = jsNodes.iterator(); it.hasNext();) {
            js = ((JsNode) it.next()).getFile();
            if (js.getLastModified() > last) last = js.getLastModified();
        }
        File file = getOutput(output, last);
        if (log.isDebugEnabled()) {
            log.debug(file.getAbsoluteFile());
        }
        String scriptPath = js.getWebPath(file.getAbsolutePath());

        if (!file.exists()) {
            String content = "";
            try {
                StringBuffer buf = new StringBuffer();
                for (Iterator it = jsNodes.iterator(); it.hasNext();) {
                    buf.append(
                            JavaScriptDocument.readFile(((JsNode) it.next()).getFile().getFile()))
                            .append(JsContext.LINE_BREAK);
                }
                content = buf.toString();
                FileUtils.writeStringToFile(file, content, DEFAULT_ENCODING);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        return TagsFormBuilder.genTag(scriptPath);
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
        File folder = new File(pathResolver.getPath(folderPath));
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

    public void setOutput(String output) {
        this.output = output;
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }
}
