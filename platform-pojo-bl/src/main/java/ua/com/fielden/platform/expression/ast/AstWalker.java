package ua.com.fielden.platform.expression.ast;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 *
 * Traverses an AST tree for semantic processing.
 *
 * @author TG Team
 *
 */
public class AstWalker {

    private final List<IAstVisitor> visitors = new ArrayList<IAstVisitor>();
    private final AstNode root;

    public AstWalker(final AstNode root, final IAstVisitor... visitors) {
	for (final IAstVisitor visitor : visitors) {
	    if (visitor == null) {
		throw new IllegalArgumentException("There should be no null visitors.");
	    }
	    this.visitors.add(visitor);
	}
	this.root = root;
    }

    /**
     * The main method for starting AST tree walking.
     */
    public AstNode walk() throws SemanticException {
	for (final IAstVisitor visitor : visitors) {
	    visitTree(root, visitor);
	    visitor.postVisit(root);
	}

	return root;
    }

    /**
     * Traverses tree with depth-firth algorithm visiting the nodes on post-order mode (i.e. ABR).
     *
     * @param visitor
     */
    public void visitTree(final AstNode root, final IAstVisitor visitor) throws SemanticException {
	for (int index = 0; index < root.getChildren().size(); index++) {
	    final AstNode child = root.getChildren().get(index);
	    visitTree(child, visitor);
	}
	visitor.visit(root);
    }
}
