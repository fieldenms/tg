package ua.com.fielden.platform.algorithm.bfs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.algorithm.search.ITreeNode;

/**
 * 
 * Tree node for testing purposes.
 * 
 * @author TG Team
 * 
 */
public class TreeNode implements ITreeNode<String> {

    private final String state;
    private final List<TreeNode> children = new ArrayList<TreeNode>();

    private int visits = 0;

    public TreeNode(final String content) {
        if (content == null) {
            throw new IllegalArgumentException("The content argument cannot be null");
        }
        this.state = content;
    }

    public TreeNode addChild(final TreeNode child) {
        children.add(child);
        return this;
    }

    @Override
    public List<TreeNode> daughters() {
        return children;
    }

    @Override
    public String state() {
        return state;
    }

    public void incVisitCount() {
        visits++;
    }

    @Override
    public int hashCode() {
        return state.hashCode() * 29;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TreeNode)) {
            return false;
        }
        
        final TreeNode that = (TreeNode) obj;
        return state.equals(that.state);
    }

    public int visits() {
        return visits;
    }

    public void reset() {
        visits = 0;
    }

    @Override
    public String toString() {
        return "State: " + state + "; " + "# of visits " + visits + "; # of children " + children.size();
    }

}
