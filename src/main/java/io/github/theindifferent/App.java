package io.github.theindifferent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class App {
    public static void main(String[] args) {
        System.setProperty("swing.boldMetal", "false");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new NimbusLookAndFeel());
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            var folderChooser = new JFileChooser(System.getProperty("user.home"));
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setMultiSelectionEnabled(false);
            folderChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            folderChooser.setDialogTitle("Choose folder to scan disk usage");
            if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                new MainWindow(folderChooser.getSelectedFile().toPath()).setVisible(true);
            }
        });
//        new DiskScanner(Path.of("C:", "3D")).scan();
    }
}
