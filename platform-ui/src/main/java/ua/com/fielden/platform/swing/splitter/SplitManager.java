package ua.com.fielden.platform.swing.splitter;

import java.awt.Container;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 * manager for building splitPanels based on the JSplitPane. Alternative to the Docking manager.
 * 
 * @author oleh
 * 
 */
public class SplitManager {

    /**
     * Component that must managed with splitter manage (split into regions)
     */
    private final Container containerToSplit;
    /**
     * holds all {@link JSplitPane} instances associated with it's id
     */
    private final Map<String, JSplitPane> idSplit;

    /**
     * current split panel that is currently build and is used for next splitting
     */
    private JSplitPane currentSplitPane;
    /**
     * panel that must be added to the frame during flush method invoke and removed from it during reset method invoke
     */
    private JSplitPane rootSplitPane;
    /**
     * split enumeration value of the current split panel
     */
    private SplitEnum currentSplitEnum;

    /**
     * creates new splitManager instance for the given {@link Container} instance
     * 
     * @param componentToSplit
     */
    public SplitManager(final Container componentToSplit) {

        this.containerToSplit = componentToSplit;
        this.idSplit = new HashMap<String, JSplitPane>();

        this.currentSplitEnum = null;
        this.currentSplitPane = null;
    }

    /**
     * split the current {@link JSplitPane} instance into two parts according to the side and sets id for the newly created {@link JSplitPane} instance
     * 
     * @param side
     * @param id
     * @return
     */
    public SplitManager split(final SplitEnum side, final String id) {
        if (rootSplitPane == null) {
            switch (side) {
            case NORTH:
            case SOUTH:
                currentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, null);
                break;
            case EAST:
            case WEST:
                currentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, null);
            }
            rootSplitPane = currentSplitPane;
        } else {
            currentSplitPane = side.splitNext(currentSplitEnum, currentSplitPane);
        }
        currentSplitPane.setOneTouchExpandable(rootSplitPane.isOneTouchExpandable());
        currentSplitPane.setContinuousLayout(rootSplitPane.isContinuousLayout());
        currentSplitPane.setDividerSize(rootSplitPane.getDividerSize());
        idSplit.put(id, currentSplitPane);
        currentSplitEnum = side;
        return this;
    }

    /**
     * creates new split manager for the same frame as this one and splits it
     * 
     * @param side
     * @param id
     * @return
     */
    public SplitManager splitNew(final SplitEnum side, final String id) {
        return new SplitManager(containerToSplit).split(side, id);
    }

    /**
     * removes the root split panel from the frame and rests all the properties of this instance
     */
    public void reset() {
        if (rootSplitPane != null) {
            containerToSplit.remove(rootSplitPane);
        }
        idSplit.clear();
        currentSplitEnum = null;
        currentSplitPane = null;
        rootSplitPane = null;
    }

    /**
     * returns onTouchExpandable property of the root split pane
     * 
     * @return
     */
    public boolean isOneTouchExpandable() {
        return rootSplitPane.isOneTouchExpandable();
    }

    /**
     * set the onTouchExpandable property for each split panel in the those were created by this SplitManager
     * 
     * @param oneTouchExpandable
     */
    public void setOneTouchExpandable(final boolean oneTouchExpandable) {
        final Iterator<JSplitPane> splitIterator = idSplit.values().iterator();
        while (splitIterator.hasNext()) {
            splitIterator.next().setOneTouchExpandable(oneTouchExpandable);
        }
    }

    /**
     * adds the root split panel to the frame if it's not null
     * 
     * @param constraints
     *            parameter used by the layout manager
     */
    public void flush(final Object constraints) {
        if (rootSplitPane != null) {
            if (containerToSplit.getComponentCount() != 0) {
                containerToSplit.removeAll();
            }
            containerToSplit.add(rootSplitPane, constraints);
            containerToSplit.validate();
            containerToSplit.repaint();
        }
    }

    /**
     * set the component on the specified side of the split panel represented by the id
     * 
     * @param id
     * @param component
     * @param side
     */
    public void setComponent(final String id, final JComponent component, final String side) {
        final JSplitPane pane = idSplit.get(id);
        if (pane != null) {
            pane.add(component, side);
        }
    }

    /**
     * returns continuesLayout property of the root split pane
     * 
     * @return
     */
    public boolean isContinuesLayout() {
        return rootSplitPane.isContinuousLayout();
    }

    /**
     * set the continuesLayout property for each split panel in the those were created by this SplitManager
     * 
     * @param continuesLayout
     */
    public void setContinuesLayout(final boolean continuesLayout) {
        final Iterator<JSplitPane> splitIterator = idSplit.values().iterator();
        while (splitIterator.hasNext()) {
            splitIterator.next().setContinuousLayout(continuesLayout);
        }
    }

    /**
     * returns dividerSize property of the root split pane
     * 
     * @return
     */
    public int getDividerSize() {
        return rootSplitPane.getDividerSize();
    }

    /**
     * set the dividerSize property for each split panel in the those were created by this SplitManager
     * 
     * @param dividerSize
     */
    public void setDividerSize(final int dividerSize) {
        final Iterator<JSplitPane> splitIterator = idSplit.values().iterator();
        while (splitIterator.hasNext()) {
            splitIterator.next().setDividerSize(dividerSize);
        }
    }

    /**
     * return the container that must be split
     * 
     * @return
     */
    public Container getContainerToSplit() {
        return containerToSplit;
    }

}
