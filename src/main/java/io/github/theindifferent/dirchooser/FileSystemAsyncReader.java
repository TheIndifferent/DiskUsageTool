package io.github.theindifferent.dirchooser;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class FileSystemAsyncReader {

    private final Executor ioExecutor = ForkJoinPool.commonPool();
    private final Executor edtExecutor = SwingUtilities::invokeLater;

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
                .thenApplyAsync(fileSystemBlockingReader::readRootNodes, ioExecutor)
                .thenApply(rootNode::setNodes);
        rootNodesFuture
                .thenAcceptAsync(node -> node.nodes().forEach(this::loadNode), ioExecutor);
        rootNodesFuture
                .thenAcceptAsync(this::updateRootNodeInEDT, edtExecutor);
    }

    CompletionStage<TreeNode> loadNode(TreeNode node) {
        var futureNode = CompletableFuture.completedStage(node);
        if (node.nodes() != null) {
            return futureNode;
        }
        var futureLoadedNode = futureNode
                .thenApplyAsync(this::loadNodeBlocking, ioExecutor)
                .thenApply(node::setNodes);
        var futureNodePath = futureNode
                .thenApplyAsync(this::nodePath, ioExecutor);
        futureLoadedNode
                .thenCombine(futureNodePath, (n, path) -> new TreeModelEvent(this, path))
                .whenCompleteAsync(
                        (r, t) -> {
                            if (t != null) {
                                errorConsumer.accept("Failed to list directories", t);
                            } else {
                                treeModelEventConsumer.accept(r);
                            }
                        },
                        edtExecutor);
        return futureLoadedNode;
    }

    private void updateRootNodeInEDT(TreeNode rootNode) {
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
