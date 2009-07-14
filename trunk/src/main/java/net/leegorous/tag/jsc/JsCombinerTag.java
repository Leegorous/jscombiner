/**
 * 
 */
package net.leegorous.tag.jsc;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.leegorous.jsc.JsContext;
import net.leegorous.jsc.JsContextManager;
import net.leegorous.jsc.JsFile;

/**
 * @author leegorous
 * 
 */
public class JsCombinerTag extends BodyTagSupport {

	protected static JsContextManager jsContextManager = new JsContextManager();

	private String path;

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		String p = pageContext.getServletContext().getRealPath(path);
		String root = pageContext.getServletContext().getRealPath("/");
		JsContext ctx = jsContextManager.createContext();
		try {
			StringBuffer buf = new StringBuffer();
			ctx.load(p);
			List list = ctx.getList();
			for (Iterator it = list.iterator(); it.hasNext();) {
				JsFile js = (JsFile) it.next();
				buf.append("<script src=\"");
				String jspath = js.getPath();
				String scriptPath = jspath.substring(root.length());
				String ctxPath = ((HttpServletRequest) pageContext.getRequest())
						.getContextPath();
				buf.append(ctxPath);
				buf.append(scriptPath);
				buf
						.append("\" language=\"JavaScript\" type=\"text/javascript\"></script>");

			}
			pageContext.getOut().write(buf.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BodyTagSupport.SKIP_BODY;
	}

}
