package io.github.theindifferent;

import java.nio.file.Path;

public interface DiskUsageItem extends Comparable<DiskUsageItem> {

    String name();

    Path path();

    DiskUsageDirectory parent();

    long size();

    boolean directory();

    boolean error();

    String errorString();

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
