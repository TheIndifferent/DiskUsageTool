package io.github.theindifferent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;

class ScanningInProgressWindow extends JWindow {

    private final DefaultListModel<Path> model;
    private final JScrollBar verticalScrollBar;

    ScanningInProgressWindow() {
        model = new DefaultListModel<>();
        var list = new JList<>(model);
        list.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setAnchorSelectionIndex(int anchorIndex) {
            }

            @Override
            public void setLeadSelectionIndex(int leadIndex) {
            }

            @Override
            public void setSelectionInterval(int index0, int index1) {
            }

            @Override
            public void setLeadAnchorNotificationEnabled(boolean flag) {
            }
        });

        var scrollPane = new JScrollPane(list);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        verticalScrollBar = scrollPane.getVerticalScrollBar();

        var panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Scanning..."));
        setContentPane(panel);
    }

    public void progress(List<Path> paths) {
        paths.forEach(model::addElement);
        SwingUtilities.invokeLater(() -> {
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
    }
}
