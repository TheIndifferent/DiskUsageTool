package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class DiskScanner {

    private final Path scanPath;
    private final Consumer<String> currentScanningDir;

    public DiskScanner(Path scanPath, Consumer<String> currentScanningDir) {
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

        private final Consumer<String> currentScanningDir;
        private DiskUsageDirectory dir;

        private VisitorImpl(Consumer<String> currentScanningDir) {
            this.currentScanningDir = currentScanningDir;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) {
            currentScanningDir.accept(dirPath.toString());
            dir = new DiskUsageDirectory(dirPath, Optional.ofNullable(dir));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            var file = new DiskUsageFile(filePath, Optional.of(dir));
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
            var parent = dir.parent();
            parent.ifPresent(p -> p.files.add(dir));
            dir = parent.orElse(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
