/**
 * 
 */
package net.leegorous.jsc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * @author leegorous
 * 
 */
public class JsContext {
	public static String LINE_BREAK = System.getProperty("line.separator");

	private JsContextManager manager;

	private JsNode hierarchy;

	private List list = new ArrayList();

	public JsContext(JsContextManager manager) {
		this.manager = manager;
	}

	public void buildHierarchy(String path) throws Exception {
		JsFile js = manager.getJs(path);
		if (hierarchy == null) {
			JsNode tree = new JsNode(js);
			tree.setManager(manager);
			hierarchy = tree;
			tree.process();
		} else {
			JsNode tree = new JsNode();
			tree.setManager(manager);
			hierarchy.setParent(tree);
			tree.addChild(hierarchy);
			hierarchy = tree;

			hierarchy.addChild(js);
		}
	}

	public JsContextManager getContextManager() {
		return manager;
	}

	/**
	 * @return the hierarchy
	 */
	public JsNode getHierarchy() {
		return hierarchy;
	}

	/**
	 * Get the merged scripts list. All scripts with dependencies and common
	 * imports defined in configuration file.<br/>
	 * <strong>Important</strong>: Invoke {@link #getList()} after loading
	 * scripts with {@link #load(String)}
	 * 
	 * @return the list
	 * @throws Exception
	 */
	public List getList() throws Exception {
		Set cp = new HashSet();
		for (Iterator it = list.iterator(); it.hasNext();) {
			JsFile js = (JsFile) it.next();
			cp.add(js.getClasspath());
		}
		List cfgs = new ArrayList();
		for (Iterator it = cp.iterator(); it.hasNext();) {
			String p = (String) it.next();
			JsFile cfg = manager.getClasspathConfig(p);
			if (cfg != null)
				cfgs.add(cfg);
		}

		List commons = new ArrayList();
		for (Iterator it = cfgs.iterator(); it.hasNext();) {
			JsFile js = (JsFile) it.next();
			List imported = processImport(new Stack(), js);
			if (imported == null)
				imported = new ArrayList();
			commons = mergeList(commons, imported);
		}
		for (Iterator it = commons.iterator(); it.hasNext();) {
			JsFile js = (JsFile) it.next();
			if (list.contains(js))
				list.remove(js);
		}
		commons.addAll(list);
		return commons;
	}

	/**
	 * Get the merged scripts content.<br/>
	 * <strong>Important</strong>: Invoke {@link #getScriptsContent()} after
	 * loading scripts with {@link #load(String)}
	 * 
	 * @return the merged scripts content
	 * @throws Exception
	 */
	public String getScriptsContent() throws Exception {
		List list = getList();
		StringBuffer buf = new StringBuffer();
		for (Iterator it = list.iterator(); it.hasNext();) {
			JsFile js = (JsFile) it.next();
			buf.append(JavaScriptDocument.readFile(js.getFile())).append(
					LINE_BREAK);
		}
		return buf.toString();
	}

	/**
	 * Load the script
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void load(String path) throws Exception {
		JsFile js = manager.getJs(path);
		Stack stack = new Stack();
		List scripts = processJs(stack, js);

		if (list == null) {
			list = scripts;
			return;
		}
		list = mergeList(list, scripts);
	}

	/**
	 * Merge two ordered list
	 * 
	 * @param list1
	 * @param list2
	 * @return a merged list
	 */
	protected List mergeList(List list1, List list2) {
		if (list1 == null || list2 == null) {
			throw new IllegalArgumentException("both list should not be null");
		}

		if (list1.size() == 0)
			return new ArrayList(list2);
		if (list2.size() == 0)
			return new ArrayList(list1);

		List result = new ArrayList();
		int start = 0;
		for (Iterator it = list1.iterator(); it.hasNext();) {
			Object obj = it.next();
			int idx = list2.indexOf(obj);
			if (idx > -1) {
				if (start > idx)
					throw new RuntimeException("unexpected order");
				for (; start <= idx; start++) {
					result.add(list2.get(start));
				}
			} else {
				result.add(obj);
			}
		}
		for (; start < list2.size(); start++) {
			result.add(list2.get(start));
		}
		return result;
	}

	protected List processImport(Stack stack, JsFile js) throws Exception {
		List result = new ArrayList();

		List imported = js.getImported();
		if (imported != null) {
			for (Iterator it = imported.iterator(); it.hasNext();) {
				String clazz = (String) it.next();
				List classes = manager.getJsClasses(clazz);

				for (Iterator it2 = classes.iterator(); it2.hasNext();) {
					JsFile j = (JsFile) it2.next();
					List cls = processJs(stack, j);
					result = mergeList(result, cls);
				}
			}
		}

		return result;
	}

	protected List processJs(Stack stack, JsFile js) throws Exception {
		List result = null;
		if (!stack.contains(js)) {
			stack.add(js);

			result = processImport(stack, js);
			if (result == null) {
				result = new ArrayList();
			}
			result.add(js);

			stack.pop();
		} else {
			if (stack.contains(js))
				throw new LoopedImportException("found " + js.getName()
						+ " in dependency path " + stack);
		}
		return result;
	}

}
