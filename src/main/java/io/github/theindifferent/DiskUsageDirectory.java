package io.github.theindifferent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiskUsageDirectory implements DiskUsageItem {

    private final Path path;
    private final Optional<DiskUsageDirectory> parent;

    private final String name;

    boolean error;
    List<DiskUsageItem> files = new ArrayList<>();

    public DiskUsageDirectory(Path path, Optional<DiskUsageDirectory> parent) {
        this.path = path;
        this.parent = parent;

        var fileName = path.getFileName();
        if (fileName == null) {
            this.name = path.toString();
        } else {
            this.name = fileName.toString();
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
        return files.stream()
                    .mapToLong(DiskUsageItem::size)
                    .sum();
    }

    @Override
    public boolean directory() {
        return true;
    }

    @Override
    public boolean error() {
        return error;
    }
}
