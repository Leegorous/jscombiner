package net.leegorous.jsc;

import java.io.File;
import java.util.ArrayList;

public class JavaScriptTestCaseCombiner extends JavaScriptDocument {
	
	private static String TESTCASE_TEMPLATE_PATH = "/TestCaseTemplate.xml";
	private File jsUnit;
	
	protected void setJsUnit(File jsUnit) {
		this.jsUnit = jsUnit;
	}

	public JavaScriptTestCaseCombiner() {}

	public JavaScriptTestCaseCombiner(File file, ArrayList classPaths, File jsUnit) throws JavaScriptNotFoundException {
		super(null, file, classPaths);
		setJsUnit(jsUnit);
	}
	
	public void assemble() throws Exception {
		this.findImports();
		
		String template = getTemplate();
		
		File testCaseFile = new File(this.getDoc().getAbsolutePath().replaceAll("\\.js", ".html"));
		String testCaseName = this.getDoc().getName().replaceAll("\\.js", "");
		
		template = template.replaceAll("\\{!\\[testCaseName]}", testCaseName);
		template = template.replaceAll("\\{!\\[jsUnitSource]}", 
				JavaScriptCombiner.translate2RelatePath(this.getDoc(), jsUnit));
		
		ArrayList imports = this.getImportedFiles();
		StringBuffer sb = new StringBuffer();
		for (int i=0, j=imports.size(); i<j; i++) {
			sb.append("<script language=\"JavaScript\" type=\"text/javascript\" src=\"");
			sb.append(JavaScriptCombiner.translate2RelatePath(this.getDoc(), (File) imports.get(i)));
			sb.append("\"></script>\r\n");
		}
		template = template.replaceAll("\\{!\\[testCaseScripts]}", sb.toString());
		
		saveTestCase(testCaseFile, template);
	}
	
	protected static String getTemplate() throws Exception {
		return StreamReader.read(
				JavaScriptTestCaseCombiner.class
				.getResourceAsStream(TESTCASE_TEMPLATE_PATH));
	}
	
	protected static void saveTestCase(File file, String content) throws Exception {
		StreamWriter.write(file, content, "UTF-8");
	}
}
