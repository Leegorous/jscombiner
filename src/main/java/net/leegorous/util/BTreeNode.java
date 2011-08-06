/**
 * 
 */
package net.leegorous.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leegorous
 * 
 */
public class BTreeNode {
    public static boolean leftBetween(BTreeNode start, BTreeNode end,
            Object target) {
        if (!start.onlyInLeft(end.value))
            return false;
        BTreeNode node = start;
        while (node != end) {
            if (node.is(target))
                return true;
            node = node.left;
        }
        return false;
    }

    public static boolean lessThan(BTreeNode root, Object c1, Object c2) {
        BTreeNode n1 = root.find(c1);
        if (n1 == null || !root.contains(c2)) {
            throw new IllegalArgumentException(c1.toString() + " or "
                    + c2.toString() + " is not in tree");
        }
        return !n1.inLeft(c2);
    }

    public BTreeNode left;
    public BTreeNode right;
    public Object value;

    public BTreeNode(Object v) {
        this.value = v;
    }

    public BTreeNode addLeft(Object v) {
        return addLeft(new BTreeNode(v));
    }

    public BTreeNode addLeft(BTreeNode node) {
        if (left == null) {
            return left = node;
        } else {
            return left.addRight(node);
        }
    }

    public BTreeNode addRight(BTreeNode node) {
        if (right == null) {
            return right = node;
        } else {
            return right.addRight(node);
        }
    }

    public boolean contains(Object c) {
        if (is(c))
            return true;
        if (left != null) {
            if (left.contains(c))
                return true;
        }
        if (right != null) {
            if (right.contains(c))
                return true;
        }
        return false;
    }

    public boolean contains(BTreeNode n) {
        return contains(n.value);
    }

    public BTreeNode find(Object c) {
        if (is(c))
            return this;
        BTreeNode node = null;
        if (left != null) {
            node = left.find(c);
        }
        if (node == null && right != null) {
            node = right.find(c);
        }
        return node;
    }

    public boolean inLeft(Object c) {
        if (left != null)
            return left.contains(c);
        return false;
    }

    public boolean is(Object c) {
        if (value == null)
            return false;
        return value.equals(c);
    }

    public boolean onlyInLeft(Object c) {
        if (left != null) {
            if (left.is(c)) {
                return true;
            }
            return left.onlyInLeft(c);
        }
        return false;
    }

    public List toList() {
        List list = new ArrayList();
        if (left != null) {
            list.addAll(left.toList());
        }
        if (value != null) {
            list.add(value);
        }
        if (right != null) {
            list.addAll(right.toList());
        }
        return list;
    }
}
