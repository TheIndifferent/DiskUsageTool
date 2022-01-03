package io.github.theindifferent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
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
            JOptionPane.showMessageDialog(null,
                                          "Unsupported LookAndFeel,\n" + e,
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        chooseDirectoryToScan(this::checkPathAndShowMainWindow);
    }

    private void chooseDirectoryToScan(Consumer<Path> dirConsumer) {
        var folderChooser = new JFileChooser(System.getProperty("user.home"));
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setMultiSelectionEnabled(false);
        folderChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        folderChooser.setDialogTitle("Choose folder to scan disk usage");
        if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            dirConsumer.accept(folderChooser.getSelectedFile().toPath());
        } else {
            System.exit(0);
        }
    }

    private void checkPathAndShowMainWindow(Path path) {
        var error = checkPath(path);
        error.ifPresentOrElse(
                this::showErrorMessage,
                () -> new MainWindow(path).setVisible(true));
    }

    private Optional<String> checkPath(Path path) {
        if (!Files.exists(path)) {
            return Optional.of("Path does not exist:\n" + path);
        }
        if (!Files.isDirectory(path)) {
            return Optional.of("Path is not a directory:\n" + path);
        }
        if (!Files.isReadable(path)) {
            return Optional.of("Path is not readable:\n" + path);
        }
        return Optional.empty();
    }

    private void showErrorMessage(String error) {
        JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
