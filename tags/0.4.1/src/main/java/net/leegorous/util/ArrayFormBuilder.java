/**
 * 
 */
package net.leegorous.util;

import java.util.Iterator;
import java.util.List;

import net.leegorous.jsc.JsFile;
import net.leegorous.jsc.JsNode;

/**
 * @author leegorous
 * 
 */
public class ArrayFormBuilder implements OutputBuilder {

    public String build(List jsNodes) {
        StringBuffer buf = new StringBuffer();
        return buf.append("[").append(buildContent(jsNodes)).append("]").toString();
    }

    protected String buildContent(List jsNodes) {
        StringBuffer buf = new StringBuffer();
        if (jsNodes != null) {
            for (Iterator it = jsNodes.iterator(); it.hasNext();) {
                JsFile js = ((JsNode) it.next()).getFile();
                if (buf.length() > 0) buf.append(',');
                buf.append("\"" + js.getWebPath() + "\"");
            }
        }
        return buf.toString();
    }
}
