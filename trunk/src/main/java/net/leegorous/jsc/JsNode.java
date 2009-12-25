/**
 * 
 */
package net.leegorous.jsc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author leegorous
 * 
 */
public class JsNode {
	private JsFile file;
	private JsNode parent;
	private List childs;
	private JsNode root;
	private List refs;

	private JsContextManager manager;

	public JsNode() {
		this(null, null);
	}

	public JsNode(JsFile file) {
		this(file, null);
	}

	public JsNode(JsFile file, JsNode parent) {
		this.setFile(file);
		this.setParent(parent);
	}

	public void addChild(JsFile j) throws Exception {
		checkImport(j);
		JsNode child = root.search(j);
		boolean exist = (child != null);
		if (!exist) {
			child = new JsNode(j, this);
		}
		addChild(child);
		if (!exist) {
			child.process();
		}
	}

	public void addChild(JsNode node) {
		if (childs == null) {
			childs = new ArrayList();
		}
		childs.add(node);
		node.addRef(this);
	}

	public void addRef(JsNode ref) {
		if (refs == null)
			refs = new ArrayList();
		refs.add(ref);
	}

	private void checkImport(JsFile js) throws LoopedImportException {
		JsNode n = this;
		do {
			JsFile f = n.getFile();
			if (f != null && f.equals(js)) {
				throw new LoopedImportException(f.toString());
			}
			n = n.getParent();
		} while (n != null);
	}

	/**
	 * @return the childs
	 */
	public List getChilds() {
		return childs;
	}

	/**
	 * @return the file
	 */
	public JsFile getFile() {
		return file;
	}

	/**
	 * @return the manager
	 */
	public JsContextManager getManager() {
		return manager;
	}

	/**
	 * @return the parent
	 */
	public JsNode getParent() {
		return parent;
	}

	/**
	 * @return the refs
	 */
	public List getRefs() {
		return refs;
	}

	/**
	 * @return the root
	 */
	public JsNode getRoot() {
		return root;
	}

	/**
	 * Process the dependencies
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		// process dependencies
		processImport(file);

		// process the common dependencies defined in classpath configuration
		JsFile cfg = manager.getClasspathConfig(file.getClasspath());
		processImport(cfg);
	}

	private void processImport(JsFile file) throws Exception {
		if (file == null)
			return;
		List imported = file.getImported();
		if (imported == null)
			return;
		for (Iterator it = imported.iterator(); it.hasNext();) {
			String clazz = (String) it.next();
			List classes = manager.getJsClasses(clazz);

			for (Iterator it2 = classes.iterator(); it2.hasNext();) {
				JsFile j = (JsFile) it2.next();
				addChild(j);
			}
		}
	}

	public JsNode search(JsFile js) throws LoopedImportException {
		if (file != null && file.equals(js))
			return this;
		if (childs != null) {
			JsNode result = null;
			for (Iterator it = childs.iterator(); it.hasNext();) {
				JsNode node = (JsNode) it.next();
				result = node.search(js);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	public List serialize() {
		return serialize(null);
	}

	private List serialize(List list) {
		if (list == null)
			list = new ArrayList();

		if (childs != null && childs.size() > 0) {
			for (Iterator it = childs.iterator(); it.hasNext();) {
				JsNode node = (JsNode) it.next();
				node.serialize(list);
			}
		}
		if (file != null && !list.contains(this)) {
			list.add(this);
		}
		return list;
	}

	/**
	 * @param childs
	 *            the childs to set
	 */
	public void setChilds(List childs) {
		this.childs = childs;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(JsFile file) {
		this.file = file;
	}

	/**
	 * @param manager
	 *            the manager to set
	 */
	public void setManager(JsContextManager manager) {
		this.manager = manager;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(JsNode parent) {
		if (parent == null) {
			this.root = this;
			return;
		}
		this.parent = parent;
		this.setRoot(parent.getRoot());
		this.setManager(parent.getManager());
	}

	/**
	 * @param root
	 *            the root to set
	 */
	public void setRoot(JsNode root) {
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (file == null)
			return "";
		return file.toString();
	}
}
