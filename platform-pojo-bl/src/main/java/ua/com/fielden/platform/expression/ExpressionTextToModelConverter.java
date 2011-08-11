package ua.com.fielden.platform.expression;

import static ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor.ABOVE;
import static ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor.THIS;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.EssentialPropertyValidationVisitor;
import ua.com.fielden.platform.expression.ast.visitor.ModelGeneratingVisitor;
import ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor;
import ua.com.fielden.platform.expression.ast.visitor.TypeEnforcementVisitor;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

/**
 * Binds together expression lexer, parser and AST visitors to validate an expression from both syntactic and semantic perspectives and to produce a computational model used as part of the Entity Query API.
 *
 * @author TG Team
 *
 */
public class ExpressionTextToModelConverter {
    private final String text;
    private final Class<? extends AbstractEntity> context;

    /**
     * Primary constructor.
     *
     * @param context -- an entity type serving as an expression contexts; this usually means an new calculated property is going to be added to this type.
     * @param expressionText -- a textual representations of the expression, which gets parsed and validated.
     */
    public ExpressionTextToModelConverter(final Class<? extends AbstractEntity> context, final String expressionText) {
	this.context = context;
	this.text = expressionText;
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
	final Token[] tokens = new ExpressionLexer(text).tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor epvv = new EssentialPropertyValidationVisitor(context);
	final TaggingVisitor tv = new TaggingVisitor(context);
	final TypeEnforcementVisitor tev = new TypeEnforcementVisitor(context);
	final ModelGeneratingVisitor mgv = new ModelGeneratingVisitor();
	final AstNode node = new AstWalker(ast, epvv, tv, tev, mgv).walk();
	if (!THIS.equals(node.getTag()) && !ABOVE.equals(node.getTag())) {
	    throw new IncompatibleOperandException("Incompatible expression context (" + node.getTag() + ").", node.getToken());
	}
	return node;
    }
}
