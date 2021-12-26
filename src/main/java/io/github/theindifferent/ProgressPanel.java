package io.github.theindifferent;

import javax.swing.*;
import java.awt.*;

class ProgressPanel extends JPanel {

    private final JLabel label = new JLabel(" ");

    ProgressPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Scanning..."));
        add(label, BorderLayout.CENTER);
    }

    void updateProgress(String currentProgress) {
        label.setText(currentProgress);
    }
}
