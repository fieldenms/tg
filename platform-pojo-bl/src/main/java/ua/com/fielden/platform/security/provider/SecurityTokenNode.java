package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.isTopLevel;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A node in a tree-like structure for representing security tokens in a hierarchical order. Natural ordering happens according to token's short description.
 *
 * @author TG Team
 *
 */
public class SecurityTokenNode implements Comparable<SecurityTokenNode>, ITreeNode<Class<? extends ISecurityToken>> {
    /**
     * Security token type represented by this node.
     */
    private final Class<? extends ISecurityToken> token;
    /**
     * A node representing a super token. Can be null is this token is the top level one.
     */
    private final SecurityTokenNode superTokenNode;
    /**
     * A list of nodes representing direct sub-tokens.
     */
    private final Map<Class<? extends ISecurityToken>, SecurityTokenNode> subTokenNodes;
    /**
     * Short security token description.
     */
    private final String shortDesc;
    /**
     * Short security token description.
     */
    private final String longDesc;

    /**
     * A convenient factory method for creating nodes representing top level security tokens, which is mainly useful for creation of unit tests.
     *
     * @param topLevelToken
     * @return
     */
    public static SecurityTokenNode makeTopLevelNode(final Class<? extends ISecurityToken> topLevelToken) {
        return new SecurityTokenNode(topLevelToken, null);
    }

    /**
     * A principle constructor.
     *
     * @param token
     * @param superTokenNode
     */
    public SecurityTokenNode(final Class<? extends ISecurityToken> token, final SecurityTokenNode superTokenNode) {
        if (superTokenNode == null && !isTopLevel(token)) {
            throw new IllegalArgumentException("Security token " + token.getName() + " is not a top level token, but super toke node is not provided.");
        }

        this.shortDesc = shortDesc(token);
        this.longDesc = longDesc(token);
        this.token = token;
        this.superTokenNode = superTokenNode;
        this.subTokenNodes = new HashMap<>();

        if (superTokenNode != null) {
            superTokenNode.add(this);
        }
    }

    /**
     * A convenient constructor for top level tokens.
     *
     * @param token
     */
    public SecurityTokenNode(final Class<? extends ISecurityToken> token) {
        this(token, null);
    }

    /**
     * Provides a way to add direct sub token nodes to this node.
     *
     * @param subTokenNode
     * @return
     */
    private SecurityTokenNode add(final SecurityTokenNode subTokenNode) {
        subTokenNodes.put(subTokenNode.getToken(), subTokenNode);
        return this;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public Class<? extends ISecurityToken> getToken() {
        return token;
    }

    public SecurityTokenNode getSuperTokenNode() {
        return superTokenNode;
    }

    public SortedSet<SecurityTokenNode> getSubTokenNodes() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(subTokenNodes.values()));
    }

    public SecurityTokenNode getSubTokenNode(final Class<? extends ISecurityToken> token) {
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
        return comparedByShortDesc == 0 ? getToken().getName().compareTo(anotherToken.getToken().getName()) : comparedByShortDesc;
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
    public Class<? extends ISecurityToken> state() {
        return token;
    }

}
