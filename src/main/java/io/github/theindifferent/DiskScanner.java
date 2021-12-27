package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.function.Consumer;

public class DiskScanner {

    private final Path scanPath;
    private final Consumer<Path> currentScanningDir;

    public DiskScanner(Path scanPath, Consumer<Path> currentScanningDir) {
        this.scanPath = scanPath;
        this.currentScanningDir = currentScanningDir;
    }

    public DiskUsageDirectory scan() {
        if (!Files.isDirectory(scanPath)) {
            throw new IllegalStateException("Scanning path is not a directory: " + scanPath);
        }

        var visitor = new VisitorImpl(currentScanningDir);
        try {
            Files.walkFileTree(scanPath, visitor);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return visitor.dir;
    }

    private static class VisitorImpl implements FileVisitor<Path> {

        private final Consumer<Path> currentScanningDir;
        private DiskUsageDirectory dir;

        private VisitorImpl(Consumer<Path> currentScanningDir) {
            this.currentScanningDir = currentScanningDir;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) {
            currentScanningDir.accept(dirPath);
            dir = new DiskUsageDirectory(dirPath, dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            var file = new DiskUsageFile(filePath, dir);
            dir.files.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path filePath, IOException exc) {
            dir.error = true;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dirPath, IOException exc) {
            Collections.sort(dir.files);
            if (dir.parent() != null) {
                dir.parent().files.add(dir);
                dir = dir.parent();
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
