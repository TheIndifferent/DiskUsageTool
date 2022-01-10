package io.github.theindifferent.dirchooser;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

class DirChooserTreeModel implements TreeModel {

    private final List<TreeModelListener> modelListeners = new ArrayList<>();
    private final TreeNode rootNode;

    DirChooserTreeModel(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    void fireTreeStructureChanged(TreeModelEvent event) {
        modelListeners.forEach(listener -> listener.treeStructureChanged(event));
    }

    @Override
    public Object getRoot() {
        return rootNode;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof TreeNode) {
            var node = (TreeNode) parent;
            if (node.nodes() == null) {
                return "...";
            }
            return node.nodes().get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof TreeNode) {
            var node = (TreeNode) parent;
            if (node.nodes() == null) {
                return 1;
            }
            return node.nodes().size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof TreeNode);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        var p = (TreeNode) parent;
        if (p.nodes() == null) {
            return 0;
        }
        for (int i = 0; i < p.nodes().size(); i++) {
            if (p.nodes().get(i) == child) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (l != null) {
            modelListeners.add(l);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null) {
            modelListeners.remove(l);
        }
    }
}
