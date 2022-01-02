package io.github.theindifferent;

import javax.swing.SwingWorker;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

class DiskScanningSwingWorker extends SwingWorker<DiskUsageDirectory, Path> {

    private final DiskUsageDirectory dirToScan;
    private final Consumer<List<Path>> progressConsumer;
    private final Consumer<DiskUsageDirectory> doneConsumer;

    DiskScanningSwingWorker(DiskUsageDirectory dirToScan, Consumer<List<Path>> progressConsumer, Consumer<DiskUsageDirectory> doneConsumer) {
        this.dirToScan = dirToScan;
        this.progressConsumer = progressConsumer;
        this.doneConsumer = doneConsumer;
    }

    @Override
    protected void process(List<Path> chunks) {
        progressConsumer.accept(chunks);
    }

    @Override
    protected void done() {
        try {
            doneConsumer.accept(get());
        } catch (Exception ignore) {
        }
    }

    @Override
    protected DiskUsageDirectory doInBackground() {
        return new DiskScanner(dirToScan, this::publish)
                .scan();
    }
}
