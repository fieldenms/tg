package ua.com.fielden.platform.expression.ast.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.IAstVisitor;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * A convenient visitor to extract nodes from AST based on their token category.
 *
 * @author TG Team
 *
 */
public class NodeByTokenExtractionVisitor implements IAstVisitor {

    private final List<AstNode> nodes = new ArrayList<AstNode>();
    private final EgTokenCategory category;

    public NodeByTokenExtractionVisitor(final EgTokenCategory category) {
	this.category = category;
    }

    @Override
    public void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat == category) {
	    nodes.add(node);
	}
    }

    public List<AstNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

}
