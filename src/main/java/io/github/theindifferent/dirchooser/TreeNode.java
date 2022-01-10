package io.github.theindifferent.dirchooser;

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

    /**
     * It is possible to have race condition for the returned value
     * as it can be read both in Event Dispatch Thread and in background
     * worker threads.<br/>
     * But the returned list is never modified after the list reference is set,
     * which makes all usages of this race condition safe.
     *
     * @return list of child nodes, or {@code null} if the node has not been scanned yet.
     */
    List<TreeNode> nodes() {
        return nodes;
    }

    TreeNode parent() {
        return parent;
    }

    Path path() {
        return path;
    }

    TreeNode setNodes(List<TreeNode> nodes) {
        this.nodes = nodes;
        return this;
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
