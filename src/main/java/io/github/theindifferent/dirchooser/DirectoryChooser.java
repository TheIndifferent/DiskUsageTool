package io.github.theindifferent.dirchooser;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DirectoryChooser extends JPanel {

    private final Consumer<Path> choiceConsumer;
    private final FileSystemAsyncReader fileSystemReader;
    private final TreeNode rootNode;
    private final DirChooserTreeModel treeModel;

    private final JTextField pathField;
    private final JTree tree;

    // TODO cancellation callback
    // TODO ResourceBundle for translations
    // TODO error dialog factory
    public DirectoryChooser(Consumer<Path> choiceConsumer) {
        super(new BorderLayout());
        this.choiceConsumer = choiceConsumer;

        rootNode = new TreeNode(null, null);
        treeModel = new DirChooserTreeModel(rootNode);
        fileSystemReader = new FileSystemAsyncReader(treeModel::fireTreeStructureChanged, this::showErrorDialog);

        pathField = createPathField();
        add(pathField, BorderLayout.NORTH);

        tree = createTree(treeModel, fileSystemReader);
        var treeScrollPane = new JScrollPane(tree);
        add(treeScrollPane, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        fileSystemReader.loadRootNode(rootNode);
        SwingUtilities.invokeLater(tree::requestFocusInWindow);
    }

    private JTree createTree(TreeModel model, FileSystemAsyncReader fileSystemReader) {
        var treeSelectionModel = new DefaultTreeSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        var tree = new JTree(model);
        tree.addTreeExpansionListener(new DirTreeExpansionListener(fileSystemReader));
        // TODO
//        tree.addTreeSelectionListener(this::treeNodeSelected);
        tree.setSelectionModel(treeSelectionModel);
        tree.setDragEnabled(false);
        tree.setEditable(false);
        tree.setRootVisible(false);
        return tree;
    }

    private JTextField createPathField() {
        var pathField = new JTextField("");
        return pathField;
    }

    private void showErrorDialog(String message, Throwable ex) {
        var writer = new StringWriter();
        var print = new PrintWriter(writer);
        ex.printStackTrace(print);
        print.flush();
        print.close();

        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + writer,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
