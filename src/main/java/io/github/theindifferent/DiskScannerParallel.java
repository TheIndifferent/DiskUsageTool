package io.github.theindifferent;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DiskScannerParallel {

    private final LinkedBlockingQueue<DiskUsageDirectory> dirsToScanQueue;
    private final LinkedBlockingQueue<Path> progressReportQueue;
    private final Semaphore workInProgressSemaphore;
    private final Thread[] threads;

    private final Path scanPath;
    private final Consumer<Path> progressReporter;

    public DiskScannerParallel(Path scanPath, Consumer<Path> progressReporter) {
        this.scanPath = scanPath;
        this.progressReporter = progressReporter;

        var cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        dirsToScanQueue = new LinkedBlockingQueue<>();
        progressReportQueue = new LinkedBlockingQueue<>();
        workInProgressSemaphore = new Semaphore(cores);

        this.threads = new Thread[cores];
        for (int i = 0; i < cores; i++) {
            var runnable = new DiskScannerRunnable(dirsToScanQueue, progressReportQueue, workInProgressSemaphore);
            var t = new Thread(runnable, "DiskScanner-" + i);
            t.start();
            threads[i] = t;
        }
    }

    public DiskUsageDirectory scan() {
        var rootDir = new DiskUsageDirectory(scanPath, null);
        dirsToScanQueue.add(rootDir);

        try {
            Path progress;
            while ((progress = progressReportQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
                progressReporter.accept(progress);
            }
            workInProgressSemaphore.acquire(threads.length);
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
