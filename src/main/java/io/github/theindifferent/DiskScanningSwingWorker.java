package io.github.theindifferent;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

class DiskScanningSwingWorker extends SwingWorker<DiskUsageDirectory, String> {

    private final Path pathToScan;
    private final Consumer<String> progressConsumer;
    private final Consumer<DiskUsageDirectory> doneConsumer;

    DiskScanningSwingWorker(Path pathToScan, Consumer<String> progressConsumer, Consumer<DiskUsageDirectory> doneConsumer) {
        this.pathToScan = pathToScan;
        this.progressConsumer = progressConsumer;
        this.doneConsumer = doneConsumer;
    }

    @Override
    protected void process(List<String> chunks) {
        var fullPath = chunks.get(chunks.size() - 1);
        progressConsumer.accept(fullPath);
    }

    @Override
    protected void done() {
        try {
            doneConsumer.accept(get());
        } catch (Exception ignore) {
        }
    }

    @Override
    protected DiskUsageDirectory doInBackground() throws Exception {
        return new DiskScanner(pathToScan, this::publish)
                .scan();
    }
}
