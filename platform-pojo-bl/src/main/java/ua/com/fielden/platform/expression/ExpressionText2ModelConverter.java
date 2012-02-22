package ua.com.fielden.platform.expression;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor;
import ua.com.fielden.platform.expression.ast.visitor.EssentialPropertyValidationVisitor;
import ua.com.fielden.platform.expression.ast.visitor.LevelAllocatingVisitor;
import ua.com.fielden.platform.expression.ast.visitor.ModelGeneratingVisitor;
import ua.com.fielden.platform.expression.ast.visitor.TypeEnforcementVisitor;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * Binds together expression lexer, parser and AST visitors to validate an expression from both syntactic and semantic perspectives and to produce a computational model used as part of the Entity Query API.
 *
 * @author TG Team
 *
 */
public class ExpressionText2ModelConverter {
    private final String expressionText;
    private final Class<? extends AbstractEntity> higherOrderType;
    private final String contextProperty;

    /**
     * The <code>higherOrderType</code> argument should represent a top level type for which all referenced by the AST nodes properties should be its properties or subproperties.
     * The <code>contextProperty</code> argument specifies the relative location in the type tree against which all other AST nodes' levels should be checked for compatibility.
     * The value of the <code>contextProperty</code> argument should be null if the higher-order type represents a context.
     *
     * @param higherOrderType
     * @param contextProperty
     * @param expressionText -- a textual representations of the expression, which gets parsed and validated.
     */

    public ExpressionText2ModelConverter(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty, final String expressionText) {
	this.higherOrderType = higherOrderType;
	this.contextProperty = contextProperty;
	this.expressionText = expressionText;
    }

    /**
     * Convenient constructor where context matches the high-order type.
     *
     * @param higherOrderType
     * @param expressionText
     */

    public ExpressionText2ModelConverter(final Class<? extends AbstractEntity> higherOrderType, final String expressionText) {
	this(higherOrderType, null, expressionText);
    }


    /**
     * Performs syntactic, semantic analysis and produces a computational model, which can be used for entity query API.
     * Returns the root AST node in case of successful validation and model creation.
     * The computational model can be accessed by invoking method <code>getModel()<code> on the root node.
     *
     * @return
     * @throws RecognitionException
     * @throws SemanticException
     */
    public AstNode convert() throws RecognitionException, SemanticException {
	final Token[] tokens = new ExpressionLexer(expressionText).tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor epvv = new EssentialPropertyValidationVisitor(higherOrderType, contextProperty);
	final LevelAllocatingVisitor lav = new LevelAllocatingVisitor(higherOrderType, contextProperty);
	final CollectionalContextVisitor tv = new CollectionalContextVisitor(higherOrderType, contextProperty);
	final TypeEnforcementVisitor tev = new TypeEnforcementVisitor(higherOrderType, contextProperty);
	final ModelGeneratingVisitor mgv = new ModelGeneratingVisitor(higherOrderType, contextProperty);
	final AstNode node = new AstWalker(ast, epvv, lav, tv, tev, mgv).walk();
	return node;
    }
}
