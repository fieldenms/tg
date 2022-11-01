package ua.com.fielden.platform.security.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.algorithm.search.ITreeNode;

/**
 * A node in a tree-like structure for representing security tokens in a hierarchical order. Natural ordering happens according to token's short description.
 *
 * @author TG Team
 *
 */
public class SecurityTokenNode implements Comparable<SecurityTokenNode>, ITreeNode<String> {
    /**
     * Security token type represented by this node.
     */
    private final String token;
    /**
     * A list of nodes representing direct sub-tokens.
     */
    private final Map<String, SecurityTokenNode> subTokenNodes;
    /**
     * Short security token description.
     */
    private final String shortDesc;
    /**
     * Short security token description.
     */
    private final String longDesc;

    /**
     * A node representing a super token. Can be null is this token is the top level one.
     */
    private SecurityTokenNode superTokenNode;

        /**
     * A principle constructor.
     *
     * @param token
     * @param superTokenNode
     */
    public SecurityTokenNode(final String token, final String shortDesc, final String longDesc) {
        this.token = token;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.subTokenNodes = new HashMap<>();
    }

    /**
     * Provides a way to add direct sub token nodes to this node.
     *
     * @param subTokenNode
     * @return
     */
    public SecurityTokenNode add(final SecurityTokenNode subTokenNode) {
        if (subTokenNode.getSuperTokenNode() != null) {
            subTokenNode.getSuperTokenNode().remove(subTokenNode);
        }
        subTokenNodes.put(subTokenNode.getToken(), subTokenNode);
        subTokenNode.superTokenNode = this;
        return this;
    }

    public SecurityTokenNode remove(final SecurityTokenNode subTokenNode) {
        if (subTokenNode.getSuperTokenNode() == this) {
            subTokenNode.superTokenNode = null;
            return subTokenNodes.remove(subTokenNode.getToken());
        }
        return null;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public String getToken() {
        return token;
    }

    public SecurityTokenNode getSuperTokenNode() {
        return superTokenNode;
    }

    public SortedSet<SecurityTokenNode> getSubTokenNodes() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(subTokenNodes.values()));
    }

    public SecurityTokenNode getSubTokenNode(final String token) {
        return subTokenNodes.get(token);
    }

    @Override
    public int hashCode() {
        return token.hashCode() * 23;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SecurityTokenNode)) {
            return false;
        }

        return getToken().equals(((SecurityTokenNode) obj).getToken());
    }

    @Override
    public int compareTo(final SecurityTokenNode anotherToken) {
        final int comparedByShortDesc = shortDesc.compareTo(anotherToken.shortDesc);
        return comparedByShortDesc == 0 ? token.compareTo(anotherToken.getToken()) : comparedByShortDesc;
    }

    @Override
    public String toString() {
        return shortDesc;
    }

    @Override
    public List<SecurityTokenNode> daughters() {
        return new ArrayList<>(new TreeSet<>(subTokenNodes.values()));
    }

    @Override
    public String state() {
        return token;
    }

}
