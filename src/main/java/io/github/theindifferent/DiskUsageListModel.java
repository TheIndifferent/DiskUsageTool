package io.github.theindifferent;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class DiskUsageListModel extends AbstractListModel<DiskUsageItem> {

    private final List<DirChangeListener> dirChangeListeners = new ArrayList<>();
    private DiskUsageDirectory current;

    public DiskUsageListModel(DiskUsageDirectory current) {
        this.current = current;
    }

    DiskUsageDirectory currentDir() {
        return current;
    }

    Stream<String> namesStream() {
        return current.files.stream()
                            .map(DiskUsageItem::name);
    }

    LongStream sizesStream() {
        return current.files.stream()
                            .mapToLong(DiskUsageItem::size);
    }

    long largestFile() {
        if (current.files.isEmpty()) {
            return 0;
        }
        return current.files.get(0).size();
    }

    boolean hasParent() {
        return current.parent() != null;
    }

    void goToIndex(int index) {
        if (index < 0) {
            return;
        }
        if (index == 0 && hasParent()) {
            goToParent();
            return;
        }
        if (hasParent()) {
            index--;
        }
        var newCurrent = current.files.get(index);
        if (newCurrent.directory()) {
            var removeLength = current.files.size() + (hasParent() ? 1 : 0);
            current = (DiskUsageDirectory) newCurrent;
            var addedLength = current.files.size() + (hasParent() ? 1 : 0);
            fireIntervalRemoved(this, 0, removeLength);
            fireIntervalAdded(this, 0, addedLength);
            fireDirChanged();
        }
    }

    void goToParent() {
        if (current.parent() != null) {
            var removedLength = current.files.size() + 1;
            current = current.parent();
            var addedLength = current.files.size() + (hasParent() ? 1 : 0);
            fireIntervalRemoved(this, 0, removedLength);
            fireIntervalAdded(this, 0, addedLength);
            fireDirChanged();
        }
    }

    void refreshCurrent(int itemsBeforeRefresh) {
        var removeLength = itemsBeforeRefresh + (hasParent() ? 1 : 0);
        var addedLength = current.files.size() + (hasParent() ? 1 : 0);
        fireIntervalRemoved(this, 0, removeLength);
        fireIntervalAdded(this, 0, addedLength);
    }

    @Override
    public int getSize() {
        if (hasParent()) {
            return current.files.size() + 1;
        }
        return current.files.size();
    }

    @Override
    public DiskUsageItem getElementAt(int index) {
        if (hasParent()) {
            if (index == 0) {
                return current.parent();
            }
            return current.files.get(index - 1);
        }
        return current.files.get(index);
    }

    public void addDirChangeListener(DirChangeListener listener) {
        if (listener != null) {
            dirChangeListeners.add(listener);
        }
    }

    public void removeDirChangeListener(DirChangeListener listener) {
        if (listener != null) {
            dirChangeListeners.remove(listener);
        }
    }

    private void fireDirChanged() {
        dirChangeListeners.forEach(listener -> listener.dirChanged(current));
    }

    public interface DirChangeListener {
        void dirChanged(DiskUsageDirectory dir);
    }
}
