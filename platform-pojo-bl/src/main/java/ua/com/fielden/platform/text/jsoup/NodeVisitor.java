package ua.com.fielden.platform.text.jsoup;

import org.jsoup.nodes.Node;

/**
 * A depth-first visitor of nodes with explicit control over the visiting of a node's children, something that the
 * {@linkplain org.jsoup.select.NodeVisitor visitor} provided by the library is lacking.
 */
public interface NodeVisitor {

    default void visit(final Node node) {
        visitChildren(node);
    }

    default void visitChildren(final Node node) {
        node.childNodes().forEach(this::visit);
    }

}
