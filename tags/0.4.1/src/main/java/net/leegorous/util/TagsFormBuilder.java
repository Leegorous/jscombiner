/**
 * 
 */
package net.leegorous.util;

import java.util.Iterator;
import java.util.List;

import net.leegorous.jsc.JsContext;
import net.leegorous.jsc.JsFile;
import net.leegorous.jsc.JsNode;

/**
 * @author leegorous
 * 
 */
public class TagsFormBuilder implements OutputBuilder {

    private static String preTag = "<script src=\"";

    private static String subTag = "\" language=\"JavaScript\" type=\"text/javascript\"></script>";

    public static String genTag(String content) {
        return new StringBuffer().append(preTag).append(content).append(subTag).toString();
    }

    public String build(List jsNodes) {
        StringBuffer buf = new StringBuffer();
        if (jsNodes != null) {
            for (Iterator it = jsNodes.iterator(); it.hasNext();) {
                JsFile js = ((JsNode) it.next()).getFile();
                if (buf.length() > 0) buf.append(JsContext.LINE_BREAK);
                buf.append(genTag(js.getWebPath()));
            }
        }
        return buf.toString();
    }

}
