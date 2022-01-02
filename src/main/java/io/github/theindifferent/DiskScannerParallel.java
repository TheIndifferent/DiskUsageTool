package io.github.theindifferent;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DiskScannerParallel {

    private final BlockingQueue<DiskUsageDirectory> dirsToScanQueue;
    private final BlockingQueue<Path> progressReportQueue;
    private final BlockingQueue<List<DiskUsageItem>> collectionsToSortQueue;
    private final Semaphore workInProgressSemaphore;
    private final AtomicBoolean phase2Marker;
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
        phase2Marker = new AtomicBoolean(false);
        collectionsToSortQueue = new LinkedBlockingQueue<>();

        this.threads = new Thread[cores];
        for (int i = 0; i < cores; i++) {
            var runnable = new DiskScannerRunnable(dirsToScanQueue, progressReportQueue, collectionsToSortQueue, workInProgressSemaphore, phase2Marker);
            var t = new Thread(runnable, "DiskScanner-" + i);
            t.start();
            threads[i] = t;
        }
    }

    public DiskUsageDirectory scan() {
        var rootDir = new DiskUsageDirectory(scanPath, null);
        dirsToScanQueue.add(rootDir);
        collectionsToSortQueue.add(rootDir.files);

        try {

            reportProgress();
            waitForAllThreadsToFinishWork();
            memoizeDirSizes(rootDir);
            phase2Marker.set(true);
            for (var t : threads) {
                t.interrupt();
            }
            waitForAllThreadsToFinishWork();

        } catch (InterruptedException iex) {
            System.err.println("Waiting for scanner threads was interrupted:");
            iex.printStackTrace();
        }
        return rootDir;
    }

    private void reportProgress() throws InterruptedException {
        Path progress;
        while ((progress = progressReportQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
            progressReporter.accept(progress);
        }
    }

    private void waitForAllThreadsToFinishWork() throws InterruptedException {
        try {
            workInProgressSemaphore.acquire(threads.length);
        } finally {
            workInProgressSemaphore.release(threads.length);
        }
    }

    private void memoizeDirSizes(DiskUsageDirectory dir) {
        dir.size();
    }
}
