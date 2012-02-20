package ua.com.fielden.platform.expression.ast.visitor;

import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * A demo visitor, which simply constructs postfix, prefix or infix form (this depends on the tree walking algorithm) of the expression while visiting an AST tree.
 * It can be used conveniently for testing purposes.
 *
 * @author TG Team
 *
 */
public class ExpressionToStringVisitor extends AbstractAstVisitor {

    private final StringBuilder expressionForm = new StringBuilder();

    @Override
    public void visit(final AstNode node) throws SemanticException {
	expressionForm.append(node.toString() + " ");
    }

    public String expression() {
        return expressionForm.toString();
    }

}
