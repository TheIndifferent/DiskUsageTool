package io.github.theindifferent.dirchooser;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class FileSystemAsyncReader {

    private final FileSystemBlockingReader fileSystemBlockingReader = new FileSystemBlockingReader();
    private final Consumer<TreeModelEvent> treeModelEventConsumer;
    private final BiConsumer<String, Throwable> errorConsumer;

    FileSystemAsyncReader(Consumer<TreeModelEvent> treeModelEventConsumer,
                          BiConsumer<String, Throwable> errorConsumer) {
        this.treeModelEventConsumer = treeModelEventConsumer;
        this.errorConsumer = errorConsumer;
    }

    void loadRootNode(TreeNode rootNode) {
        var rootNodesFuture = CompletableFuture
                .completedStage(rootNode)
                .thenApplyAsync(fileSystemBlockingReader::readRootNodes);
        rootNodesFuture
                .thenAcceptAsync(list -> list.forEach(this::loadNode));
        rootNodesFuture
                .thenAccept(nodes -> SwingUtilities.invokeLater(() -> updateRootNodeInEDT(rootNode, nodes)));
    }

    void loadNode(TreeNode node) {
        var futureNode = CompletableFuture.completedStage(node);
        var futureLoadedNode = futureNode.thenApplyAsync(this::loadNodeBlocking);
        var futureNodePath = futureNode.thenApplyAsync(this::nodePath);
        futureLoadedNode
                .thenCombine(futureNodePath,
                             (list, path) -> {
                                 var event = new TreeModelEvent(this, path);
                                 SwingUtilities.invokeLater(() -> {
                                     node.setNodes(list);
                                     treeModelEventConsumer.accept(event);
                                 });
                                 return null;
                             })
                .exceptionally(t -> {
                    SwingUtilities.invokeLater(() -> errorConsumer.accept("Failed to list directories", t));
                    return null;
                });
    }

    private void updateRootNodeInEDT(TreeNode rootNode, List<TreeNode> nodes) {
        rootNode.setNodes(nodes);
        var event = new TreeModelEvent(this, new Object[]{rootNode});
        treeModelEventConsumer.accept(event);
        // TODO decide if this logic is useful:
//        if (nodes.size() == 1) {
//            SwingUtilities.invokeLater(() -> tree.expandRow(0));
//        }
    }

    private List<TreeNode> loadNodeBlocking(TreeNode node) {
        try {
            return fileSystemBlockingReader.readDirNode(node);
        } catch (IOException ioex) {
            throw new CompletionException(ioex);
        }
    }

    private Object[] nodePath(TreeNode node) {
        var pathList = new ArrayList<TreeNode>();
        var n = node;
        while (n != null) {
            pathList.add(n);
            n = n.parent();
        }
        var path = new Object[pathList.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = pathList.get(path.length - 1 - i);
        }
        return path;
    }
}
