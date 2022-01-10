package io.github.theindifferent.dirchooser;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

class DirTreeExpansionListener implements TreeExpansionListener {

    private final FileSystemAsyncReader fileSystemReader;

    DirTreeExpansionListener(FileSystemAsyncReader fileSystemReader) {
        this.fileSystemReader = fileSystemReader;
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        var path = event.getPath();
        var node = (TreeNode) path.getLastPathComponent();
        if (node.nodes() == null) {
            fileSystemReader.loadNode(node);
            return;
        }
        node.nodes()
            .forEach(n -> {
                if (n.nodes() == null) {
                    fileSystemReader.loadNode(n);
                }
            });
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
    }
}
