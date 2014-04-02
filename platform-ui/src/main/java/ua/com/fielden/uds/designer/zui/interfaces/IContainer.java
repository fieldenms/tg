package ua.com.fielden.uds.designer.zui.interfaces;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

public interface IContainer {
    /**
     * Checks compatibility between a node and container. Particular implementation of the IContainer interface defines what "compatible" means.
     * 
     * @param node
     *            -- to be checked for compatibility
     * @return true is compatible, false otherwise
     */
    boolean isCompatible(PNode node);

    /**
     * This method should implement an action, which needs to be performed when a node is dropped into a container. It is envisaged that this method should be invoked from a
     * DragEventHandler.endDrag event.
     * 
     * @param event
     *            -- can be useful when it is necessary to determine mouse position upon attachment etc.
     * @param node
     * @param animate
     */
    void attach(PInputEvent event, PNode node, boolean animate);

    /**
     * This method should implement an action, which needs to be performed when a node is dragged and dropped outside from a container (i.e. detached). It is envisaged that this
     * method should be invoked from a DragEventHandler.endDrag event.
     * 
     * @param event
     *            -- can be useful when it is necessary to determine mouse position upon attachment etc.
     * @param node
     * @param animate
     */
    void detach(PInputEvent event, PNode node, boolean animate, boolean forcedDetach);

    /**
     * This methods needs to be overridden if a custom behaviour to be provided on attache action.
     * 
     * @param node
     *            -- a node that is being attached.
     */
    void doAfterAttach(PNode node);

    /**
     * This methods needs to be overridden if a custom behaviour to be provided on detach action.
     * 
     * @param node
     *            -- a node that is being detached.
     */
    void doAfterDetach(PNode node);

    /**
     * Reshapes container boundaries.
     */
    void reshape(boolean animate);
}
