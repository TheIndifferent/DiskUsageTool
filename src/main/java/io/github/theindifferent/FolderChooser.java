package io.github.theindifferent;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class FolderChooser extends JPanel {

    private final Consumer<Path> dirConsumer;
    private final FileChooserTreeModel treeModel;
    private final TreeSelectionModel treeSelectionModel;
    private final JTextField pathField;
    private final JTree tree;

    public FolderChooser(Consumer<Path> dirConsumer) {
        super(new BorderLayout());
        this.dirConsumer = dirConsumer;

        var rootNode = new RootNode();
        treeModel = new FileChooserTreeModel(rootNode);
        treeSelectionModel = new DefaultTreeSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree = new JTree(treeModel);
        tree.addTreeExpansionListener(new FolderTreeExpansionListener());
        tree.setSelectionModel(treeSelectionModel);
        tree.setDragEnabled(false);
        tree.setEditable(false);
        tree.setRootVisible(false);

        var treeScrollPane = new JScrollPane(tree);
        add(treeScrollPane, BorderLayout.CENTER);


        pathField = new JTextField("");
        var pathPanel = new JPanel(new BorderLayout());
        pathPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        pathPanel.add(pathField, BorderLayout.CENTER);
        add(pathPanel, BorderLayout.NORTH);

        new LoadRootsWorker(rootNode).execute();
        SwingUtilities.invokeLater(tree::requestFocusInWindow);
    }

    private void showErrorDialog(String message, Exception ex) {
        var writer = new StringWriter();
        var print = new PrintWriter(writer);
        ex.printStackTrace(print);
        print.flush();
        print.close();

        JOptionPane.showMessageDialog(
                FolderChooser.this,
                message + "\n\n" + writer,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void loadNode(TreeNode node) {
        if (node instanceof DirNode) {
            var dir = (DirNode) node;
            if (dir.nodes == null) {
                new LoadDirWorker(dir)
                        .execute();
            }
        }
    }

    private class FileChooserTreeModel implements TreeModel {

        private final LoadingNode loadingNode = new LoadingNode();
        private final List<TreeModelListener> modelListeners = new ArrayList<>();
        private final RootNode rootNode;

        private FileChooserTreeModel(RootNode rootNode) {
            this.rootNode = rootNode;
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
                    return loadingNode;
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
            return node instanceof LoadingNode;
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

    private class FolderTreeExpansionListener implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            var path = event.getPath();
            var node = (TreeNode) path.getLastPathComponent();
            node.nodes()
                .forEach(FolderChooser.this::loadNode);
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    private interface TreeNode {
        List<TreeNode> nodes();

        TreeNode parent();
    }

    private class LoadingNode implements TreeNode {

        @Override
        public List<TreeNode> nodes() {
            return null;
        }

        @Override
        public TreeNode parent() {
            return null;
        }

        @Override
        public String toString() {
            return "...";
        }
    }

    private class DirNode implements TreeNode {

        private volatile List<TreeNode> nodes;
        private final Path path;
        private final TreeNode parent;

        private DirNode(Path path, TreeNode parent) {
            this.path = path;
            this.parent = parent;
        }

        @Override
        public List<TreeNode> nodes() {
            return nodes;
        }

        @Override
        public TreeNode parent() {
            return parent;
        }

        @Override
        public String toString() {
            return Objects.requireNonNullElse(path.getFileName(), path).toString();
        }
    }

    private class RootNode implements TreeNode {
        private volatile List<TreeNode> nodes;

        @Override
        public List<TreeNode> nodes() {
            return nodes;
        }

        @Override
        public TreeNode parent() {
            return null;
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private class LoadRootsWorker extends SwingWorker<List<TreeNode>, Void> {

        private final RootNode rootNode;

        private LoadRootsWorker(RootNode rootNode) {
            this.rootNode = rootNode;
        }

        @Override
        protected List<TreeNode> doInBackground() {
            var nodes = new ArrayList<TreeNode>();
            for (var path : FileSystems.getDefault().getRootDirectories()) {
                if (Files.isDirectory(path) && Files.isReadable(path)) {
                    var node = new DirNode(path, rootNode);
                    nodes.add(node);
                }
            }
            nodes.sort(Comparator.comparing(TreeNode::toString));

            nodes.forEach(n -> new LoadDirWorker((DirNode) n)
                    .execute());

            return nodes;
        }

        @Override
        protected void done() {
            try {
                var nodes = get();
                rootNode.nodes = nodes;
                var event = new TreeModelEvent(this, new Object[]{rootNode});
                treeModel.modelListeners
                        .forEach(listener -> listener.treeStructureChanged(event));
                if (nodes.size() == 1) {
                    SwingUtilities.invokeLater(() -> tree.expandRow(0));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                showErrorDialog("Failed to list file system roots", e);
            }
        }
    }

    private class LoadDirWorker extends SwingWorker<DirNodesLoadResult, Void> {

        private final DirNode node;

        private LoadDirWorker(DirNode node) {
            this.node = node;
        }

        @Override
        protected DirNodesLoadResult doInBackground() throws Exception {
            try (var dirStream = Files.newDirectoryStream(node.path)) {
                var nodes = new ArrayList<TreeNode>();
                for (var path : dirStream) {
                    if (Files.isDirectory(path) && Files.isReadable(path)) {
                        var n = new DirNode(path, node);
                        nodes.add(n);
                    }
                }
                nodes.sort(Comparator.comparing(TreeNode::toString));

                var pathList = new ArrayList<TreeNode>();
                TreeNode n = node;
                while (n != null) {
                    pathList.add(n);
                    n = n.parent();
                }
                var path = new Object[pathList.size()];
                for (int i = 0; i < path.length; i++) {
                    path[i] = pathList.get(path.length - 1 - i);
                }

                return new DirNodesLoadResult(nodes, path);

            } catch (IOException e) {
                throw e;
            }
        }

        @Override
        protected void done() {
            try {
                var res = get();
                node.nodes = res.nodes;
                var event = new TreeModelEvent(this, res.treePath);
                treeModel.modelListeners
                        .forEach(listener -> listener.treeStructureChanged(event));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                showErrorDialog("Failed to list directories", e);
            }
        }
    }

    private class DirNodesLoadResult {
        private final List<TreeNode> nodes;
        private final Object[] treePath;

        private DirNodesLoadResult(List<TreeNode> nodes, Object[] treePath) {
            this.nodes = nodes;
            this.treePath = treePath;
        }
    }
}
