package io.github.theindifferent.dirchooser;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
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
    private final JButton okButton;
    private final JButton cancelButton;

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

        okButton = createOkButton();
        cancelButton = createCancelButton();
        var buttonsPanel = createButtonsPanel(okButton, cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);

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

    private JButton createOkButton() {
        var button = new JButton("OK");
        button.addActionListener(this::directoryChosen);
        return button;
    }

    private JButton createCancelButton() {
        return new JButton("Cancel");
    }

    private JComponent createButtonsPanel(JButton okButton, JButton cancelButton) {
        var okDimensions = okButton.getMinimumSize();
        var cancelDimension = cancelButton.getMinimumSize();
        if (okDimensions.getWidth() < cancelDimension.getWidth()) {
            okButton.setMinimumSize(cancelDimension);
            okButton.setPreferredSize(cancelDimension);
        } else {
            cancelButton.setMinimumSize(okDimensions);
            cancelButton.setPreferredSize(okDimensions);
        }
        var box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(okButton);
        box.add(Box.createHorizontalStrut(6));
        box.add(cancelButton);
        return box;
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

    private void directoryChosen(ActionEvent event) {
        var selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            return;
        }
        var selectedNode = (TreeNode) selectedPath.getLastPathComponent();
        var path = selectedNode.path();
        choiceConsumer.accept(path);
    }

    public JButton getDefaultButton() {
        return okButton;
    }

    public void showChooserDialog(String title) {
        var dialog = new JDialog((Dialog)null, title);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // TODO this might close the dialog without making a selection
        okButton.addActionListener(event -> dialog.dispose());
        cancelButton.addActionListener(event -> dialog.dispose());
        dialog.setContentPane(this);
        dialog.getRootPane().setDefaultButton(getDefaultButton());
        dialog.pack();
        var dialogSize = dialog.getSize();
        int w = Math.max(dialogSize.width, 550);
        int h = Math.max(dialogSize.height, 700);
        dialog.setSize(w, h);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
