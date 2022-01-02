package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.function.Consumer;

public class DiskScanner {

    private final DiskUsageDirectory dirToScan;
    private final Consumer<Path> currentScanningDir;

    public DiskScanner(DiskUsageDirectory dirToScan, Consumer<Path> currentScanningDir) {
        this.dirToScan = dirToScan;
        this.currentScanningDir = currentScanningDir;
    }

    public DiskUsageDirectory scan() {
        var rootDir = dirToScan;
        rootDir.files.clear();
        rootDir.error = false;
        scanDirectory(rootDir);
        var d = rootDir.parent();
        while (d != null) {
            Collections.sort(d.files);
            d = d.parent();
        }
        return rootDir;
    }

    private void scanDirectory(DiskUsageDirectory dir) {
        currentScanningDir.accept(dir.path());
        try (var dirStream = Files.newDirectoryStream(dir.path())) {
            for (var path : dirStream) {
                var item = itemForPath(path, dir);
                dir.files.add(item);
            }
        } catch (IOException e) {
            System.err.println("Failed to read directory: " + dir.path());
            e.printStackTrace();
            dir.error = true;
        }
        Collections.sort(dir.files);
    }

    private DiskUsageItem itemForPath(Path path, DiskUsageDirectory parent) {
        try {
            var attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes.isRegularFile()) {
                return new DiskUsageFile(path, parent, path.getFileName().toString(), attributes.size(), false);
            }
            if (attributes.isDirectory()) {
                var dir = new DiskUsageDirectory(path, parent);
                scanDirectory(dir);
                return dir;
            }
            // anything that is not dir or file:
            //if (attributes.isSymbolicLink() || attributes.isOther()) {
            return new DiskUsageFile(path, parent, path.getFileName().toString(), 0, false);
            //}
        } catch (IOException ioex) {
            System.err.println("Failed to read file attributes: " + path);
            ioex.printStackTrace();
            return new DiskUsageFile(path, parent, path.getFileName().toString(), 0, true);
        }
    }
}
