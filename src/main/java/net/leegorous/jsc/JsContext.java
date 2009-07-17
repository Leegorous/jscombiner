/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
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
	private JsContextManager manager;

	private List list = new ArrayList();

	public JsContext(JsContextManager manager) {
		this.manager = manager;
	}

	public JsContextManager getContextManager() {
		return manager;
	}

	/**
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

	public void load(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}

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

}
