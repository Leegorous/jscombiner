package net.leegorous.tag.jsc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class JavaScriptAssemblerTag extends BodyTagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 661013686373300968L;
	private String path;
	private File file;
	private ArrayList packages;
	
	public void addPackage(String name) {
		packages.add(name);
	}
	
	public int doStartTag() throws JspException {
		String realPath = pageContext.getServletContext().getRealPath(path)+".html";
		file = new File(realPath);
		try {
			if (!file.exists()) {
				file=createFile(realPath);
				return EVAL_BODY_BUFFERED;
			}
			pageContext.getOut().write(pageContext.getServletConfig().getServletName());
		} catch(Exception e) {
			file=null;
			// here should make a log
			e.printStackTrace();
		}
		return BodyTagSupport.SKIP_BODY;
	}
	
	public int doAfterBody() throws JspException {
		return BodyTagSupport.SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		if (file==null) throw new JspException("File not exist.");
		return super.doEndTag();
	}
	
	public void release() {
		file=null;
		packages=null;
		super.release();
	}
	
	private File createFile(String path) throws IOException {
		File d = new File(path.substring(0, 
				path.replaceAll("[\\\\]", "/").lastIndexOf("/")));
		if (!d.exists() || !d.isDirectory()) d.mkdirs();
		d = new File(path);
		d.createNewFile();
		return d;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) throws Exception {
		Pattern pattern = Pattern.compile("(/\\w+)*");
		if (!Pattern.matches("(/\\w+)*", path)) throw new Exception();
		this.path = path;
	}
}
