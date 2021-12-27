package io.github.theindifferent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;

class MainPanel extends JPanel {

    MainPanel(Path path) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        var label = new JLabel("Scanning: ");
        add(label, BorderLayout.CENTER);
        new DiskScanningSwingWorker(
                path,
                progress -> label.setText("Scanning: " + progress),
                this::scanningDone)
                .execute();
    }

    private void scanningDone(DiskUsageDirectory rootItem) {
        var model = new DiskUsageListModel(rootItem);
        var list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DiskUsageItemCellRenderer(model));
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enterDirectory");
        list.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "goUp");
        list.getInputMap().put(KeyStroke.getKeyStroke('r'), "refreshCurrent");
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
                refreshCurrent(model);
            }
        });

        var scrollPane = new JScrollPane(list);

        removeAll();
        add(scrollPane, BorderLayout.CENTER);
        updateUI();
        list.requestFocusInWindow();
    }

    void refreshCurrent(DiskUsageListModel listModel) {
        var progressPanel = new ProgressPanel();
        var window = new JWindow((Frame) null);
        window.setContentPane(progressPanel);
        window.setSize(getWidth(), 100);
        window.setLocationRelativeTo(MainPanel.this);
        window.setVisible(true);
        var currentDir = listModel.currentDir();
        new DiskScanningSwingWorker(
                currentDir.path(),
                progressPanel::updateProgress,
                dir -> {
                    window.dispose();
                    listModel.refreshCurrent(dir);
                })
                .execute();
    }
}
