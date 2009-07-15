/**
 * 
 */
package net.leegorous.tag.jsc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.leegorous.jsc.JsContext;
import net.leegorous.jsc.JsContextManager;
import net.leegorous.jsc.JsFile;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author leegorous
 * 
 */
public class JsCombinerTag extends BodyTagSupport {

	/**
	 * The serialVersionUID of JsCombinerTag
	 */
	private static final long serialVersionUID = -5327283965223602242L;

	protected static JsContextManager jsContextManager;

	private String path;

	private StringBuffer result;

	private String preTag = "<script src=\"";

	private String subTag = "\" language=\"JavaScript\" type=\"text/javascript\"></script>\n";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		BodyContent content = getBodyContent();
		this.path = content.getString();
		content.clearBody();
		process();
		return SKIP_BODY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		path = StringUtils.trimToNull(path);
		if (path != null) {
			process();
			return SKIP_BODY;
		} else {
			return super.doStartTag();
		}
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

	public List normalizePath(String path) {
		List result = null;
		if (path != null) {
			result = new ArrayList();
			path = path.replaceAll("[\\r\\n]", ";");
			String[] li = StringUtils.split(path, " ;");
			for (int i = 0; i < li.length; i++) {
				String str = StringUtils.trimToNull(li[i]);
				if (str != null)
					result.add(str);
			}
		}
		return result;
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
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().write(result.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}

	private void process() {
		path = StringUtils.trimToNull(path);
		if (path == null)
			return;
		JsContextManager mgr = getJsContextManager();
		JsContext ctx = mgr.createContext();
		String root = getRealPath("/");
		String ctxPath = ((HttpServletRequest) pageContext.getRequest())
				.getContextPath();

		List li = normalizePath(path);
		for (Iterator it = li.iterator(); it.hasNext();) {
			String item = (String) it.next();
			String p = getRealPath(item);

			try {
				ctx.load(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List list = ctx.getList();

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
		result = buf;
	}

}
