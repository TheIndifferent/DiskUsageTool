package io.github.theindifferent;

import java.nio.file.Path;
import java.util.Optional;

public interface DiskUsageItem extends Comparable<DiskUsageItem> {

    String name();

    Path path();

    Optional<DiskUsageDirectory> parent();

    long size();

    boolean directory();

    boolean error();

    default int compareTo(DiskUsageItem o) {
        var sizeDiff = size() - o.size();
        if (sizeDiff == 0) {
            return 0;
        }
        if (sizeDiff < 0) {
            return 1;
        }
        return -1;
    }
}
