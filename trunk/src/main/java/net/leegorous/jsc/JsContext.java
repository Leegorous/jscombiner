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
		addJs(stack, js);
	}

	protected void addJs(Stack stack, JsFile js) throws Exception {
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
						addJs(stack, j);
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

}
