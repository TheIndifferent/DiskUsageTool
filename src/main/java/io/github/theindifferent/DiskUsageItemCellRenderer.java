package io.github.theindifferent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DiskUsageItemCellRenderer extends JComponent implements ListCellRenderer<DiskUsageItem> {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private final float[] gradientPoints = new float[]{0f, 0.5f, 1f};
    private final Color[] gradientColors = new Color[]{Color.GREEN, Color.YELLOW, Color.RED};
    private final Map<Long, String> formattedSizesMap = new HashMap<>();
    private final DiskUsageListModel listModel;

    private DiskUsageDirectory currentDir;
    private int longestFileName;
    private int longestFileSize;
    private int longestIcon;

    private DiskUsageItem currentItem;
    private boolean isGoToParentCell;

    public DiskUsageItemCellRenderer(DiskUsageListModel listModel) {
        this.listModel = listModel;
    }

    @Override
    public void paint(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        var clip = g2.getClipBounds();
        g2.setColor(getBackground());
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);

        var fontMetrics = g2.getFontMetrics();
        var fontAscent = fontMetrics.getAscent();

        var xOfIcon = clip.x + 6 + longestFileSize + 6;
        var xOfName = clip.x + 6 + longestFileSize + 6 + longestIcon + 6;

        if (isGoToParentCell) {
            paintIcon(g2, "FileChooser.upFolderIcon", xOfIcon, clip.y);
            g2.setColor(getForeground());
            g2.drawString("Up", xOfName, fontAscent);
            return;
        }

        var sizeText = formattedSizesMap.get(currentItem.size());
        var xOfSizeText = clip.x + 6 + longestFileSize - fontMetrics.stringWidth(sizeText);
        var xOfBar = clip.x + 6 + longestFileSize + 6 + longestIcon + 6 + longestFileName + 6;

        g2.setFont(getFont());

        if (currentItem.error()) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(getForeground());
        }

        // draw size aligned to the right:
        g2.drawString(sizeText, xOfSizeText, fontAscent);
        // draw file or folder icon:
        paintIcon(g2,
                  currentItem.directory()
                  ? "FileChooser.directoryIcon"
                  : "FileChooser.fileIcon",
                  xOfIcon,
                  clip.y);
        // draw name:
        g2.drawString(currentItem.name(), xOfName, fontAscent);
        // draw visual size comparison:
        var gradient = new LinearGradientPaint(xOfBar, clip.y + 1, clip.width - 6, clip.height - 2, gradientPoints, gradientColors);
        g2.setPaint(gradient);
        var relationBetweenLargestAndCurrentItem = listModel.largestFile() / currentItem.size();
        var maxGradientWidth = clip.width - xOfBar - 6;
        var currentGradientWidth = maxGradientWidth / relationBetweenLargestAndCurrentItem;
        if (currentGradientWidth < 2) {
            currentGradientWidth = 2;
        }
        g2.fillRect(xOfBar, clip.y + 1, (int) currentGradientWidth, clip.height - 2);
    }

    private void paintIcon(Graphics2D g2, String iconKey, int x, int y) {
        var icon = UIManager.getIcon(iconKey);
        if (icon == null) {
            var ex = new IllegalStateException("LookAndFeel does not have initialized icons for files and folders");
            ex.printStackTrace();
            throw ex;
        }
        icon.paintIcon(this, g2, x, y);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends DiskUsageItem> list,
                                                  DiskUsageItem value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        if (currentDir != listModel.currentDir()) {
            currentDir = listModel.currentDir();
            var fontMetrics = list.getFontMetrics(list.getFont());
            var dirIcon = UIManager.getIcon("FileChooser.directoryIcon");
            var fileIcon = UIManager.getIcon("FileChooser.fileIcon");
            var cellHeight = Math.max(fontMetrics.getHeight(), Math.max(dirIcon.getIconHeight(), fileIcon.getIconHeight()));
            setPreferredSize(new Dimension(list.getWidth(), cellHeight));

            longestIcon = Math.max(dirIcon.getIconWidth(), fileIcon.getIconWidth());

            formattedSizesMap.clear();
            listModel.sizesStream()
                     .forEach(size -> formattedSizesMap.put(size, sizeText(size)));
            longestFileSize = formattedSizesMap.values()
                                               .stream()
                                               .mapToInt(fontMetrics::stringWidth)
                                               .max()
                                               .orElse(0);
            longestFileName = listModel.namesStream()
                                       .mapToInt(fontMetrics::stringWidth)
                                       .max()
                                       .orElse(0);
        }
        currentItem = value;
        isGoToParentCell = index == 0 && listModel.hasParent();

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(list.getFont());

        return this;
    }

    private String sizeText(long size) {
        double s = size;
        if (s < 1024) {
            return decimalFormat.format(s) + "B";
        }
        s /= 1024;
        if (s < 1024) {
            return decimalFormat.format(s) + "kB";
        }
        s /= 1024;
        if (s < 1024) {
            return decimalFormat.format(s) + "MB";
        }
        s /= 1024;
        if (s < 1024) {
            return decimalFormat.format(s) + "GB";
        }
        s /= 1024;
        return decimalFormat.format(s) + "TB";
    }
}
