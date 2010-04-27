/**
 * 
 */
package net.leegorous.tag.jsc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.leegorous.jsc.JsContext;
import net.leegorous.jsc.JsContextManager;
import net.leegorous.jsc.JsFile;
import net.leegorous.jsc.JsNode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author leegorous
 * 
 */
public class JsCombinerTag extends BodyTagSupport {

	public static final String DEFAULT_ENCODING = "UTF-8";

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * The serialVersionUID of JsCombinerTag
	 */
	private static final long serialVersionUID = -5327283965223602242L;

	public static final String MODE_DEV = "dev";

	public static final String MODE_PROD = "prod";

	protected static JsContextManager jsContextManager;

	private static Map cpMap = new HashMap();

	private String cp;

	private String path;

	private String mode = MODE_DEV;

	private String output;

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
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			String str = result != null ? result.toString() : "";
			pageContext.getOut().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			result = null;
			mode = null;
			output = null;
			path = null;
			cp = null;
		}
		return super.doEndTag();
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

	/**
	 * Get the classpath
	 * 
	 * @return cp
	 */
	public String getCp() {
		return cp;
	}

	private JsContextManager getJsContextManager() {
		try {
			if (jsContextManager == null) {
				WebApplicationContext webAppContext = (WebApplicationContext) pageContext
						.getServletContext()
						.getAttribute(
								WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
				if (webAppContext != null) {
					jsContextManager = (JsContextManager) webAppContext
							.getBean("jsContextManager");
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage());
			// kill the bean defined error
		}
		if (jsContextManager == null)
			jsContextManager = new JsContextManager();
		return jsContextManager;
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
			if (f.charAt(0) != '/')
				f = "/" + f;
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
					String tail = name.substring(qName.length() + 1, name
							.length() - 3);
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

		if (last[0] > lastModified)
			fileName = fName[0];
		else
			fileName = fileName + "." + (new Date().getTime()) + ".js";

		return new File(folder, fileName);
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	private String getRealPath(String path) {
		String p = pageContext.getServletContext().getRealPath(path);
		File file = new File(p);
		try {
			p = file.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return p;
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

	private void addClasspath(JsContextManager mgr, String rootPath) {
		if (cp == null)
			return;
		List classpath = (List) cpMap.get(cp);
		if (classpath != null)
			return;
		List cps = normalizePath(cp);
		List result = new ArrayList();
		for (Iterator it = cps.iterator(); it.hasNext();) {
			String str = (String) it.next();
			String path = FilenameUtils.concat(rootPath, str);
			mgr.addClasspath(path);
			result.add(path);
		}
		cpMap.put(cp, result);
	}

	private void process() {
		if (MODE_PROD.equals(mode) && output == null) {
			throw new IllegalArgumentException(
					"property 'output' could not be null in 'prod' mode");
		}

		path = StringUtils.trimToNull(path);
		if (path == null)
			return;
		JsContextManager mgr = getJsContextManager();
		String root = getRealPath("/");
		log.debug(root);
		addClasspath(mgr, root);

		JsContext ctx = mgr.createContext();
		String ctxPath = ((HttpServletRequest) pageContext.getRequest())
				.getContextPath();

		List li = normalizePath(path);
		for (Iterator it = li.iterator(); it.hasNext();) {
			String item = (String) it.next();
			// String p = getRealPath(item);

			try {
				ctx.buildHierarchy(item);
				// ctx.load(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List list = null;
		try {
			JsNode node = ctx.getHierarchy();
			list = node.serialize();
			// list = ctx.getList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mode == null || MODE_DEV.equals(mode)) {
			StringBuffer buf = new StringBuffer();
			if (list != null) {
				for (Iterator it = list.iterator(); it.hasNext();) {
					JsFile js = ((JsNode) it.next()).getFile();
					String jspath = js.getPath();
					String scriptPath = translateScriptPath(root, jspath);

					buf.append(preTag);
					buf.append(ctxPath);
					buf.append(scriptPath);
					buf.append(subTag);
				}
			}
			result = buf;
		}

		if (MODE_PROD.equals(mode)) {
			long last = 0;
			for (Iterator it = list.iterator(); it.hasNext();) {
				JsFile js = (JsFile) it.next();
				if (js.getLastModified() > last)
					last = js.getLastModified();
			}
			File file = getOutput(output, last);
			String scriptPath = translateScriptPath(root, file
					.getAbsolutePath());

			if (!file.exists()) {
				String content = "";
				try {
					content = ctx.getScriptsContent();
					FileUtils
							.writeStringToFile(file, content, DEFAULT_ENCODING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			StringBuffer buf = new StringBuffer();
			buf.append(preTag);
			buf.append(ctxPath);
			buf.append(scriptPath);
			buf.append(subTag);
			result = buf;
		}
	}

	/**
	 * Set the classpath
	 * 
	 * @param cp
	 *            classpath in format like this "scripts; clazz"
	 */
	public void setCp(String cp) {
		this.cp = cp;
	}

	public void setMode(String mode) {
		if (mode == null)
			return;
		mode = mode.toLowerCase();
		if (MODE_DEV.equals(mode) || MODE_PROD.equals(mode))
			this.mode = mode;
	}

	public void setOutput(String output) {
		this.output = StringUtils.trimToNull(output);
		if (output.endsWith("/")) {
			throw new IllegalArgumentException(
					"property 'output' should be /path/to/file , and it can't ends with '/'");
		}
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	private String translateScriptPath(String root, String jspath) {
		String scriptPath = jspath.substring(root.length());

		if (scriptPath.indexOf('\\') != -1) {
			scriptPath = scriptPath.replaceAll("\\\\", "/");
		}
		return scriptPath;
	}

}
