package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

public class DiskUsageFile implements DiskUsageItem {

    private final Path path;
    private final Optional<DiskUsageDirectory> parent;

    private final String name;
    private final long size;

    private boolean error;

    public DiskUsageFile(Path path, Optional<DiskUsageDirectory> parent) {
        this.path = path;
        this.parent = parent;
        this.name = path.getFileName().toString();
        var fileSize = fileSizeOrEmptyOnError(path);

        this.size = fileSize.orElse(0);
        this.error = fileSize.isEmpty();
    }

    private OptionalLong fileSizeOrEmptyOnError(Path path) {
        try {
            return OptionalLong.of(Files.size(path));
        } catch (IOException ioex) {
            return OptionalLong.empty();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Optional<DiskUsageDirectory> parent() {
        return parent;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean directory() {
        return false;
    }

    @Override
    public boolean error() {
        return error;
    }
}
