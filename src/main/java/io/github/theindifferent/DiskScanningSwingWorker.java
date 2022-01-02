package io.github.theindifferent;

import javax.swing.SwingWorker;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

class DiskScanningSwingWorker extends SwingWorker<DiskUsageDirectory, Path> {

    private final Path pathToScan;
    private final Consumer<List<Path>> progressConsumer;
    private final Consumer<DiskUsageDirectory> doneConsumer;

    DiskScanningSwingWorker(Path pathToScan, Consumer<List<Path>> progressConsumer, Consumer<DiskUsageDirectory> doneConsumer) {
        this.pathToScan = pathToScan;
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
        return new DiskScanner(pathToScan, this::publish)
                .scan();
    }
}
