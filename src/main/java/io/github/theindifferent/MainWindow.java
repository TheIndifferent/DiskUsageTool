package io.github.theindifferent;

import io.github.theindifferent.DiskUsageListModel.DirChangeListener;

import javax.swing.JFrame;
import java.nio.file.Path;

class MainWindow extends JFrame implements DirChangeListener {

    MainWindow(Path path) {
        super("Disk Usage Tool: " + path);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new MainPanel(path, this));
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    @Override
    public void dirChanged(DiskUsageDirectory dir) {
        setTitle("Disk Usage Tool: " + dir.path());
    }
}
