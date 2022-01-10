package io.github.theindifferent.dirchooser;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.List;

class TreeNode {

    private final Path path;
    private final TreeNode parent;
    private volatile List<TreeNode> nodes;

    TreeNode(Path path, TreeNode parent) {
        this.path = path;
        this.parent = parent;
    }

    List<TreeNode> nodes() {
        return nodes;
    }

    TreeNode parent() {
        return parent;
    }

    Path path() {
        return path;
    }

    void setNodes(List<TreeNode> nodes) {
        this.nodes = nodes;
        if (!SwingUtilities.isEventDispatchThread()) {
            new Exception("List of nodes is set outside of Event Dispatch Thread")
                    .printStackTrace();
        }
    }

    @Override
    public String toString() {
        if (path == null) {
            return "";
        }
        var fileName = path.getFileName();
        if (fileName == null) {
            return path.toString();
        }
        return fileName.toString();
    }
}
