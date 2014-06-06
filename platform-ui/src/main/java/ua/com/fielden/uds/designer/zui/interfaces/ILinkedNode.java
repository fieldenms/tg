package ua.com.fielden.uds.designer.zui.interfaces;

import java.util.List;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is an interface, which indicates that a node has a link.
 * 
 * @author 01es
 * 
 */
public interface ILinkedNode {
    List<ILink> getLinks();

    boolean isLinked(ILinkedNode node);

    /**
     * Resets all the links and recursively invokes resetAll() on its children of type ILinkedNode
     */
    void resetAll();

    /**
     * Resets all the links by delta and recursively invokes resetAll(PDimension delta, Set<PNode> processedNodes) on its children of type ILinkedNode
     */
    void resetAll(PDimension delta, Set<PNode> processedNodes);
}
