package ua.com.fielden.platform.expression.ast;

import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * A contract for AST tree visitor.
 * 
 * @author TG Team
 * 
 */
public interface IAstVisitor {
    /**
     * Should encapsulate logic executed upon visitation of the provided node. The post-, pre- and inner- order of method execution is determined by the tree walker implementation.
     * 
     * @param node
     */
    void visit(final AstNode node) throws SemanticException;

    /**
     * This is a convenient method to implemented visitor specific post visitation logic.
     * 
     * @param rootNode
     * @throws SemanticException
     */
    void postVisit(final AstNode rootNode) throws SemanticException;
}
