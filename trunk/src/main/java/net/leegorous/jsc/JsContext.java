/**
 * 
 */
package net.leegorous.jsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

	protected void addJs(Stack stack, JsFile js, List list) throws Exception {
		if (!list.contains(js)) {
			int idx = stack.size() > 0 ? list.indexOf(stack.peek()) : 0;
			// add the context list in a reverse order
			list.add(idx, js);

			stack.add(js);
			Set imported = js.getImported();
			if (imported != null) {
				for (Iterator it = imported.iterator(); it.hasNext();) {
					String clazz = (String) it.next();
					List classes = manager.getJsClasses(clazz);
					for (Iterator it2 = classes.iterator(); it2.hasNext();) {
						JsFile j = (JsFile) it2.next();
						addJs(stack, j, list);
					}
				}
			}
			stack.pop();
		} else {
			if (stack.contains(js))
				throw new LoopedImportException("found " + js.getName()
						+ " in dependency path " + stack);
		}
	}

	public JsContextManager getContextManager() {
		return manager;
	}

	/**
	 * @return the list
	 */
	public List getList() {
		return list;
	}

	public void load(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}

		JsFile js = manager.getJs(path);
		Stack stack = new Stack();
		List scripts = new ArrayList();
		addJs(stack, js, scripts);

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

}
