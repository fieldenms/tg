package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.List;
/**
 * Represents the object in the hierarchy that can be loaded.
 *
 * @author TG Team
 *
 */
public interface ILoadingNode {

    /**
     * Returns the parent of this node in the hierarchy of loading nodes.
     *
     * @return
     */
    ILoadingNode getLoadingParent();

    /**
     * Set the parent for this loading node.
     */
    void setLoadingParent(ILoadingNode parent);

    /**
     * Returns loading node children.
     *
     * @return
     */
    List<? extends ILoadingNode> loadingChildren();

    /**
     * Tries to load this loading node. This node can be loaded only if it's children are loaded.
     */
    void tryLoading();

    /**
     * Determines whether this node is loaded or not.
     *
     * @return
     */
    boolean isLoaded();
}
