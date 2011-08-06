package net.leegorous.util;

import java.util.Iterator;
import java.util.List;

import net.leegorous.jsc.JsFile;

public class DepdencyUtils {
    private static BTreeNode addRequire(BTreeNode tree, JsFile c) {
        BTreeNode node = new BTreeNode(c);
        return addRequire(tree, node, c, node);
    }

    private static BTreeNode addRequire(BTreeNode tree, BTreeNode node,
            JsFile c, BTreeNode frag) {
        List require = c.getImported();
        if (require != null) {
            for (Iterator it = require.iterator(); it.hasNext();) {
                JsFile rc = (JsFile) it.next();

                if (rc.equals(c))
                    throw new IllegalArgumentException(c.getName()
                            + " could not require " + rc.getName());
                if (tree != null && tree.contains(rc)) {
                    if (!node.contains(tree)) {
                        node.addLeft(tree);
                        tree = node;
                    }
                } else {
                    if (frag.is(rc) || BTreeNode.leftBetween(frag, node, rc)) {
                        throw new IllegalArgumentException(c.getName()
                                + " could not require " + rc.getName());
                    }
                    if (frag.contains(rc))
                        continue;
                    addRequire(tree, node.addLeft(rc), rc, frag);
                }
            }
        }
        return node;
    }

    /**
     * Build the binaryTree by &#064;Require
     * 
     * @param classes
     *            the class array
     * @return binaryTree contains all classes, the required classes on left
     *         side
     */
    public static BTreeNode loadDependencyTree(JsFile[] classes) {
        BTreeNode head = null;
        for (int i = 0; i < classes.length; i++) {
            if (head != null && head.contains(classes[i])) {
                continue;
            }
            head = merge(head, addRequire(head, classes[i]));
        }
        return head;
    }

    private static BTreeNode merge(BTreeNode tree, BTreeNode frag) {
        if (tree != null && !frag.contains(tree)) {
            frag.addRight(tree);
        }
        return frag;
    }
}
