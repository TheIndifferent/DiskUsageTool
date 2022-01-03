package io.github.theindifferent;

import java.nio.file.Path;

public class DiskUsageFile implements DiskUsageItem {

    private final Path path;
    private final DiskUsageDirectory parent;
    private final String name;
    private final long size;
    private final boolean error;
    private final String errorString;

    public DiskUsageFile(Path path, DiskUsageDirectory parent, String name, long size, boolean error, String errorString) {
        this.path = path;
        this.parent = parent;
        this.name = name;
        this.size = size;
        this.error = error;
        this.errorString = errorString;
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

    @Override
    public String errorString() {
        return errorString;
    }
}
