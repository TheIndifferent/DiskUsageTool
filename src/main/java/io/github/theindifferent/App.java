package io.github.theindifferent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.nio.file.Path;
import java.util.function.Consumer;

public class App implements Runnable {
    public static void main(String[] args) {
        System.setProperty("swing.boldMetal", "false");
        SwingUtilities.invokeLater(new App());
    }

    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        chooseDirectoryToScan(path -> new MainWindow(path).setVisible(true));
    }

    private void chooseDirectoryToScan(Consumer<Path> dirConsumer) {
        var folderChooser = new JFileChooser(System.getProperty("user.home"));
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setMultiSelectionEnabled(false);
        folderChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        folderChooser.setDialogTitle("Choose folder to scan disk usage");
        if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            dirConsumer.accept(folderChooser.getSelectedFile().toPath());
        }
    }
}
