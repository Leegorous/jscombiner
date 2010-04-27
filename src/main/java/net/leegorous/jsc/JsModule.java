package net.leegorous.jsc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * @author leegorous
 * 
 */
public class JsModule {
	private static int id = 0;

	private String name;
	private List list;
	private List childs;
	private List refs = new ArrayList();

	/**
	 * @return the refs
	 */
	public List getRefs() {
		return refs;
	}

	public void collapse() {
		List cs = getChilds();
		if (cs.size() < 2)
			return;
		for (Iterator it = cs.iterator(); it.hasNext();) {
			JsModule child = (JsModule) it.next();
			child.collapse();
		}
		for (int i = 0; i < cs.size(); i++) {
			JsModule a = (JsModule) cs.get(i);

			for (int j = i + 1; j < cs.size(); j++) {
				JsModule b = (JsModule) cs.get(j);

				List al = a.getMergedList();
				List bl = b.getMergedList();
				if (CollectionUtils.containsAny(al, bl)) {
					List di = diff(al, bl);
					for (int k = 0; k < di.size(); k++) {
						JsNode node = (JsNode) di.get(k);
						JsModule am = a.search(node);
						JsModule bm = b.search(node);

						JsModule d = new JsModule();
						d.getList().add(node);

						if (am.isVirtual() && bm.isVirtual()) {
							List brefs = bm.getRefs();
							for (Iterator it = brefs.iterator(); it.hasNext();) {
								JsModule m = (JsModule) it.next();
								m.replaceChild(bm, am);
							}
						} else {
							if (am.isVirtual()) {
								d = am;
							}
							if (bm.isVirtual()) {
								d = bm;
							}
							if (!am.isVirtual()) {
								am.getList().remove(node);
								am.getChilds().add(d);
								d.getRefs().add(am);
							}

							if (!bm.isVirtual()) {
								bm.getList().remove(node);
								bm.getChilds().add(d);
								d.getRefs().add(bm);
							}
						}
					}
				}
			}
		}
	}

	public void mergeVirtual() {
		List cs = getChilds();
		List virtuals = new ArrayList();
		for (Iterator it = cs.iterator(); it.hasNext();) {
			JsModule module = (JsModule) it.next();

		}
	}

	private void replaceChild(JsModule oldOne, JsModule newOne) {
		List cs = getChilds();
		int idx = cs.indexOf(oldOne);
		if (cs.remove(oldOne)) {
			cs.add(idx, newOne);
		}
		oldOne.getRefs().remove(this);
		newOne.getRefs().add(this);
	}

	public boolean isVirtual() {
		return name == null;
	}

	public JsModule search(JsNode node) {
		if (getList().contains(node)) {
			return this;
		}
		List cs = getChilds();
		for (int i = 0; i < cs.size(); i++) {
			JsModule c = (JsModule) cs.get(i);
			JsModule result = c.search(node);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public boolean contains(JsModule module) {
		if (list == null || module == null || module.getList() == null)
			return false;
		List cs = getChilds();
		boolean result = false;
		for (Iterator it = cs.iterator(); it.hasNext();) {
			JsModule child = (JsModule) it.next();
			if (child.contains(module))
				result = true;
		}

		List li = module.getList();
		if (result) {
			list.removeAll(li);
			return result;
		}
		JsNode head = (JsNode) li.get(li.size() - 1);
		result = list.contains(head);
		if (result) {
			list.removeAll(li);
			insertChild(module);
		}
		return result;
	}

	private List diff(List a, List b) {
		List result = new ArrayList();
		for (int i = 0; i < a.size(); i++) {
			Object it = a.get(i);
			if (b.contains(it)) {
				result.add(it);
			}
		}
		return result;
	}

	/**
	 * @return the childs
	 */
	public List getChilds() {
		if (childs == null)
			childs = new ArrayList();
		return childs;
	}

	/**
	 * @return the list
	 */
	public List getList() {
		if (list == null)
			list = new ArrayList();
		return list;
	}

	public List getMergedList() {
		List result = new ArrayList();
		List cs = getChilds();
		for (Iterator it = cs.iterator(); it.hasNext();) {
			JsModule child = (JsModule) it.next();
			List mergedList = child.getMergedList();
			for (int i = 0; i < mergedList.size(); i++) {
				Object o = mergedList.get(i);
				if (!result.contains(o))
					result.add(o);
			}
		}
		result.addAll(getList());
		return result;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void insertChild(JsModule module) {
		getChilds().add(0, module);
	}

	/**
	 * @param childs
	 *            the childs to set
	 */
	public void setChilds(List childs) {
		this.childs = childs;
	}

	/**
	 * @param list
	 *            the list to set
	 */
	public void setList(List list) {
		this.list = list;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

}
