package io.github.theindifferent.dirchooser;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class FileSystemBlockingReader {

    List<TreeNode> readRootNodes(TreeNode rootNode) {
        var nodes = new ArrayList<TreeNode>();
        for (var path : FileSystems.getDefault().getRootDirectories()) {
            if (Files.isDirectory(path) && Files.isReadable(path)) {
                var node = new TreeNode(path, rootNode);
                nodes.add(node);
            }
        }
        nodes.sort(Comparator.comparing(TreeNode::toString));
        return nodes;
    }

    List<TreeNode> readDirNode(TreeNode node) throws IOException {
        try (var dirStream = Files.newDirectoryStream(node.path())) {
            var nodes = new ArrayList<TreeNode>();
            for (var path : dirStream) {
                if (Files.isDirectory(path) && Files.isReadable(path)) {
                    var n = new TreeNode(path, node);
                    nodes.add(n);
                }
            }
            nodes.sort(Comparator.comparing(TreeNode::toString));
            return nodes;
        }
    }
}
