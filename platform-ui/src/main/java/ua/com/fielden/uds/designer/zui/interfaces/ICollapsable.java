package ua.com.fielden.uds.designer.zui.interfaces;

/**
 * This interface is part of the expander/collapsable nodes framework. Every node, which needs to be collapsable is required to implement this interface in order to interact with
 * ExpandableNode.
 * 
 * @author 01es
 * 
 */
public interface ICollapsable {
    void collapse();

    void expand();
}
