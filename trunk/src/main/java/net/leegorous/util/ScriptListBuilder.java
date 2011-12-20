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
public class ScriptListBuilder implements OutputBuilder {

    private String preTag = "<script src=\"";

    private String subTag = "\" language=\"JavaScript\" type=\"text/javascript\"></script>\n";

    /*
     * (non-Javadoc)
     * 
     * @see net.leegorous.util.OutputBuilder#build(java.util.List)
     */
    public String build(List jsNodes) {
        StringBuffer buf = new StringBuffer();
        if (jsNodes != null) {
            for (Iterator it = jsNodes.iterator(); it.hasNext();) {
                JsFile js = ((JsNode) it.next()).getFile();

                buf.append(preTag);
                buf.append(js.getWebPath());
                buf.append(subTag);
            }
        }
        return buf.toString();
    }

}
