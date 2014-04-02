package ua.com.fielden.uds.designer.zui.interfaces;

import java.awt.Stroke;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PDimension;

public interface ILink {
    /**
     * This method should provide link's behaviour where it is not relevant what is being changed (which node)
     */
    void reset();

    /**
     * This method should provide link's behaviour where it is important to know what is being changed (e.g. which node is being dragged).
     * 
     * @param activeNode
     *            -- node that is being modified
     */
    void reset(PNode activeNode, PDimension delta, Set<PNode> processedNodes);

    PNode getStartNode();

    PNode getEndNode();

    void hightlight(Stroke stoke);

    void dehightlight();

    /**
     * Determines which bounds should be used for link composition -- global or local. As a general rule is a node linked to a layer then isGlobalBounds should return true,
     * otherwise -- false.
     * 
     * @return
     */
    boolean isGlobalBounds();

    /**
     * See isGlobalBounds() method.
     * 
     * @param flag
     */
    void setGlobalBounds(boolean flag);
}
