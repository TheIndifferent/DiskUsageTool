package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class DiskScannerRunnable implements Runnable {

    private final BlockingQueue<DiskUsageDirectory> dirsToScanQueue;
    private final BlockingQueue<Path> progressReportQueue;
    private final Semaphore workInProgressSemaphore;

    public DiskScannerRunnable(BlockingQueue<DiskUsageDirectory> dirsToScanQueue,
                               BlockingQueue<Path> progressReportQueue,
                               Semaphore workInProgressSemaphore) {
        this.dirsToScanQueue = dirsToScanQueue;
        this.progressReportQueue = progressReportQueue;
        this.workInProgressSemaphore = workInProgressSemaphore;
    }

    @Override
    public void run() {
        scanFromQueueUntilInterrupted();
    }

    private void scanFromQueueUntilInterrupted() {
        while (!Thread.interrupted()) {
            try {
                var dir = dirsToScanQueue.take();
                workInProgressSemaphore.acquire();
                scanDirectory(dir);
            } catch (InterruptedException iex) {
                return;
            } finally {
                workInProgressSemaphore.release();
            }
        }
    }

    private void scanDirectory(DiskUsageDirectory dir) {
        progressReportQueue.add(dir.path());
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
                dirsToScanQueue.add(dir);
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
