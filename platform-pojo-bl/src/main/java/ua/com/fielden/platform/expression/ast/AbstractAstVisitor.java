package ua.com.fielden.platform.expression.ast;

import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * A convenient base implementation for AST visitors where method {@link #postVisit(AstNode)} does nothing.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAstVisitor implements IAstVisitor {

    @Override
    public void postVisit(final AstNode rootNode) throws SemanticException {
    }

}
