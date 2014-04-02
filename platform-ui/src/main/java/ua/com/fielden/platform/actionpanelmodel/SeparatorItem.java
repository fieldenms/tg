package ua.com.fielden.platform.actionpanelmodel;

import java.awt.Dimension;

import javax.swing.JToolBar;

/**
 * Represents the tool bar separator
 * 
 * @author oleh
 * 
 */
public class SeparatorItem implements IActionItem {

    private final Dimension size;

    /**
     * Creates new {@link SeparatorItem} with default size of separator
     */
    public SeparatorItem() {
        this(null);
    }

    /**
     * Creates new {@link SeparatorItem} with specified size of separator
     */
    public SeparatorItem(final Dimension size) {
        this.size = size;
    }

    /**
     * adds separator to the specified tool bar
     */
    @Override
    public void build(final JToolBar toolBar) {
        if (size != null) {
            toolBar.addSeparator(size);
        } else {
            toolBar.addSeparator();
        }
    }

    /**
     * returns the size of separator
     * 
     * @return
     */
    public Dimension getSize() {
        return size;
    }

}
