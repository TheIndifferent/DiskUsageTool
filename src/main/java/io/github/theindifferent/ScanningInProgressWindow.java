package io.github.theindifferent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

class ScanningInProgressWindow extends JWindow {

    private final PlainDocument document = createDocument();

    ScanningInProgressWindow() {
        var textArea = new JTextArea(document);
        var panel = new JPanel(new BorderLayout());
        panel.add(textArea, BorderLayout.CENTER);
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
    }

    private PlainDocument createDocument() {
        return new PlainDocument();
    }
}
