package io.github.theindifferent;

import javax.swing.*;
import java.nio.file.Path;

class MainWindow extends JFrame {

    MainWindow(Path path) {
        super("Disk Usage Tool");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new MainPanel(path));
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
}