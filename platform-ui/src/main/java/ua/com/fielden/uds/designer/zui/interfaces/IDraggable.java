package ua.com.fielden.uds.designer.zui.interfaces;

/**
 * This interface is mainly used as a market to identify a node that can be dragged. Plus some extra methods to support pre- and post- dragging actions.
 * 
 * @author 01es
 * 
 */
public interface IDraggable {
    /**
     * Indicates whether to remove a copy on a node after the drop in its original place.
     * 
     * @return
     */
    boolean getRemoveAfterDrop();

    void setRemoveAfterDrop(boolean flag);

    /**
     * Determines if a draggable node can be dragged.
     * 
     * @return
     */
    boolean canDrag();
}
