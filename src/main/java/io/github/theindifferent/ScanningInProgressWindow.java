package io.github.theindifferent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

class ScanningInProgressWindow extends JWindow {

    private final PlainDocument document = createDocument();
    private final JScrollBar verticalScrollBar;

    ScanningInProgressWindow() {
        var textArea = new JTextArea(document);
        textArea.setEditable(false);

        var scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        verticalScrollBar = scrollPane.getVerticalScrollBar();

        var panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Scanning..."));
        setContentPane(panel);
    }

    public void progress(List<Path> paths) {
        var appending = paths.stream()
                .map(Path::toString)
                .collect(Collectors.joining("\n", "", "\n"));
        try {
            document.insertString(document.getLength(), appending, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
    }

    private PlainDocument createDocument() {
        return new PlainDocument();
    }
}
