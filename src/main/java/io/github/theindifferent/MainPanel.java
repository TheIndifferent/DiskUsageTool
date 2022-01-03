package io.github.theindifferent;

import io.github.theindifferent.DiskUsageListModel.DirChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

class MainPanel extends JPanel {

    MainPanel(Path path, DirChangeListener dirChangeListener) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        var model = new DiskUsageListModel(new DiskUsageDirectory(path, null));
        model.addDirChangeListener(dirChangeListener);
        var list = createList(model);
        var scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);
        updateUI();
        list.requestFocusInWindow();

        SwingUtilities.invokeLater(() -> refreshCurrent(model, list));
    }

    private JList<DiskUsageItem> createList(DiskUsageListModel model) {
        var list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DiskUsageItemCellRenderer(model));
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enterDirectory");
        list.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "goUp");
        list.getInputMap().put(KeyStroke.getKeyStroke('r'), "refreshCurrent");
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "enterDirectory");
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "goUp");
        list.getInputMap().put(KeyStroke.getKeyStroke("COPY"), TransferHandler.getCopyAction().getValue(Action.NAME));
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), TransferHandler.getCopyAction().getValue(Action.NAME));
        list.getActionMap().put("enterDirectory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.goToIndex(list.getSelectedIndex());
            }
        });
        list.getActionMap().put("goUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.goToParent();
            }
        });
        list.getActionMap().put("refreshCurrent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrent(model, list);
            }
        });
        list.getActionMap().put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        list.setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                return new StringSelection(model.currentDir().path().toString());
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }
        });
        return list;
    }

    void refreshCurrent(DiskUsageListModel listModel, JList<?> list) {
        var progressWindow = new ScanningInProgressWindow();
        progressWindow.setSize(list.getSize());
        progressWindow.setLocation(list.getLocationOnScreen());
        progressWindow.setVisible(true);
        var currentDir = listModel.currentDir();
        var itemsBeforeRefresh = currentDir.files.size();
        new DiskScanningSwingWorker(
                currentDir,
                progressWindow::progress,
                dir -> {
                    progressWindow.dispose();
                    listModel.refreshCurrent(itemsBeforeRefresh);
                })
                .execute();
    }
}
