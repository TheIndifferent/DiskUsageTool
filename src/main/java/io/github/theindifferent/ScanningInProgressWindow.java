package io.github.theindifferent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

class ScanningInProgressWindow extends JWindow {

    private final PlainDocument document = createDocument();

    ScanningInProgressWindow() {
        var textArea = new JTextArea(document);
        var panel = new JPanel(new BorderLayout());
        panel.add(textArea, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Scanning..."));
        setContentPane(panel);
    }

    public void progress(String path) {
        try {
            document.insertString(document.getLength(), '\n' + path, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private PlainDocument createDocument() {
        return new PlainDocument();
    }
}
