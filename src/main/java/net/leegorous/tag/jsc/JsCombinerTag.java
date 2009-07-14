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

import org.springframework.web.context.WebApplicationContext;

/**
 * @author leegorous
 * 
 */
public class JsCombinerTag extends BodyTagSupport {

	protected static JsContextManager jsContextManager;

	private String path;

	private String preTag = "<script src=\"";

	private String subTag = "\" language=\"JavaScript\" type=\"text/javascript\"></script>\n";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		JsContextManager mgr = getJsContextManager();
		JsContext ctx = mgr.createContext();

		String p = getRealPath(path);
		String root = getRealPath("/");
		String ctxPath = ((HttpServletRequest) pageContext.getRequest())
				.getContextPath();

		try {
			ctx.load(p);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List list = ctx.getList();

		try {
			StringBuffer buf = new StringBuffer();
			for (Iterator it = list.iterator(); it.hasNext();) {
				JsFile js = (JsFile) it.next();
				String jspath = js.getPath();
				String scriptPath = jspath.substring(root.length());

				buf.append(preTag);
				buf.append(ctxPath);
				buf.append(scriptPath);
				buf.append(subTag);

			}
			pageContext.getOut().write(buf.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BodyTagSupport.SKIP_BODY;
	}

	private JsContextManager getJsContextManager() {
		if (jsContextManager == null) {
			WebApplicationContext webAppContext = (WebApplicationContext) pageContext
					.getServletContext()
					.getAttribute(
							WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			if (webAppContext != null) {
				JsContextManager mgr = (JsContextManager) webAppContext
						.getBean("jsContextManager");
				if (mgr != null)
					jsContextManager = mgr;
			}

			if (jsContextManager == null)
				jsContextManager = new JsContextManager();
		}
		return jsContextManager;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	private String getRealPath(String path) {
		return pageContext.getServletContext().getRealPath(path);
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
