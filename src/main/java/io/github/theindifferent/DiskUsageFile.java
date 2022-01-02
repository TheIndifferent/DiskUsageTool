package io.github.theindifferent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiskUsageFile implements DiskUsageItem {

    private final Path path;
    private final DiskUsageDirectory parent;
    private final String name;
    private final long size;
    private final boolean error;

    public DiskUsageFile(Path path, DiskUsageDirectory parent) {
        this.path = path;
        this.parent = parent;
        this.name = path.getFileName().toString();
        var fileSize = fileSizeOrEmptyOnError(path);

        this.size = fileSize < 0 ? 0 : fileSize;
        this.error = fileSize < 0;
    }

    public DiskUsageFile(Path path, DiskUsageDirectory parent, String name, long size, boolean error) {
        this.path = path;
        this.parent = parent;
        this.name = name;
        this.size = size;
        this.error = error;
    }

    private long fileSizeOrEmptyOnError(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return -1;
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
    public DiskUsageDirectory parent() {
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
