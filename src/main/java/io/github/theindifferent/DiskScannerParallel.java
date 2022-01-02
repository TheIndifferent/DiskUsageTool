package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DiskScannerParallel {

    private final LinkedBlockingQueue<DiskUsageDirectory> dirsQueue;
    private final LinkedBlockingQueue<Path> progressQueue;
    private final Thread[] threads;
    private final Semaphore semaphore;
    private final Path scanPath;
    private final Consumer<Path> progressReporter;

    public DiskScannerParallel(Path scanPath, Consumer<Path> progressReporter) {
        this.scanPath = scanPath;
        this.progressReporter = progressReporter;

        var cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        dirsQueue = new LinkedBlockingQueue<>();
        progressQueue = new LinkedBlockingQueue<>();
        this.threads = new Thread[cores];
        for (int i = 0; i < cores; i++) {
            var t = new Thread(this::diskScannerThreadRun, "DiskScanner-" + i);
            t.start();
            threads[i] = t;
        }
        semaphore = new Semaphore(cores);
    }

    private void diskScannerThreadRun() {
        scanFromQueueUntilInterrupted();
    }

    private void scanFromQueueUntilInterrupted() {
        while (!Thread.interrupted()) {
            try {
                var dir = dirsQueue.take();
                semaphore.acquire();
                scanDirectory(dir);
            } catch (InterruptedException iex) {
                return;
            } finally {
                semaphore.release();
            }
        }
    }

    private void scanDirectory(DiskUsageDirectory dir) {
        progressQueue.add(dir.path());
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
    }

    private DiskUsageItem itemForPath(Path path, DiskUsageDirectory parent) {
        try {
            var attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes.isRegularFile()) {
                return new DiskUsageFile(path, parent, path.getFileName().toString(), attributes.size(), false);
            }
            if (attributes.isDirectory()) {
                var dir = new DiskUsageDirectory(path, parent);
                dirsQueue.add(dir);
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

    public DiskUsageDirectory scan() {
        var rootDir = new DiskUsageDirectory(scanPath, null);
        dirsQueue.add(rootDir);

        try {
            Path progress;
            while ((progress = progressQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
                progressReporter.accept(progress);
            }
            semaphore.acquire(threads.length);
        } catch (InterruptedException iex) {
            System.err.println("Waiting for scanner threads was interrupted:");
            iex.printStackTrace();
        }
        for (var t : threads) {
            t.interrupt();
        }
        return rootDir;
    }
}
