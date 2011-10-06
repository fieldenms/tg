package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.security.SecurityTokenInfo.isSuperTokenOf;
import static ua.com.fielden.platform.security.SecurityTokenInfo.isTopLevel;
import static ua.com.fielden.platform.security.SecurityTokenInfo.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfo.shortDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final SortedSet<SecurityTokenNode> subTokenNodes;
    /**
     * Short security token description.
     */
    private final String shortDesc;
    /**
     * Short security token description.
     */
    private final String longDesc;

    /**
     * A principle constructor.
     *
     * @param token
     * @param superTokenNode
     */
    public SecurityTokenNode(final Class<? extends ISecurityToken> token, final SecurityTokenNode superTokenNode) {
	if (superTokenNode == null && !isTopLevel(token)) {
	    throw new IllegalArgumentException("Security token " + token.getName() + " is not a top level token, but super toke node is not provided.");
	} else if (superTokenNode != null && isTopLevel(token)) {
	    throw new IllegalArgumentException("Security token " + token.getName() + " is a top level token and should not have a super toke node, which was provided.");
	}

	shortDesc = shortDesc(token);
	longDesc = longDesc(token);
	this.token = token;
	this.superTokenNode = superTokenNode;
	subTokenNodes = new TreeSet<SecurityTokenNode>();

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
	if (!isSuperTokenOf(token, subTokenNode.getToken())) {
	    throw new IllegalArgumentException("Token " + token.getName() + " is not a super token for " + subTokenNode.getToken().getName() + ".");
	}
	subTokenNodes.add(subTokenNode);
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
	return Collections.unmodifiableSortedSet(subTokenNodes);
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
	return getShortDesc().compareTo(anotherToken.getShortDesc());
    }

    @Override
    public List<SecurityTokenNode> children() {
	return new ArrayList<SecurityTokenNode>(subTokenNodes);
    }

    @Override
    public Class<? extends ISecurityToken> state() {
	return token;
    }

}
