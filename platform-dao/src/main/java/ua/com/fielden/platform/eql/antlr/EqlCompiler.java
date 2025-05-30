package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlCompilationException;
import ua.com.fielden.platform.eql.antlr.tokens.PropToken;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

/**
 * Compiles EQL sentences (sequences of tokens) into Stage 1 representation.
 *
 * <h3>ANTLR visitor implementation patterns</h3>
 *
 * <ol>
 *     <li> Terminal derivation rules: a context class with a {@code token} field holding the value of a specific token.
 * {@snippet :
 * unaryComparisonOperator :
 *       token=ISNULL
 *     | token=ISNOTNULL ;
 * }
 * {@snippet :
 * class UnaryComparisonOperatorContext extends ParserRuleContext {
 *     public Token token;
 *     public TerminalNode ISNULL() {...}
 *     public TerminalNode ISNOTNULL() {...}
 *     ...
 * }
 * }
 *     The visitor code should use the value of {@code token} to identify the actual token.
 *     <p>
 *     If the token is parameterised it will have a corresponding custom token type.
 *     For example, {@code prop("a.b.c")} would be represented by an instance of {@link PropToken} carrying the specified property path.
 *     In such cases field {@code token} should be typecast to a corresponding type.
 *
 *     <li> Specialization rules: a hierarchy of context classes.
 * {@snippet :
 * condition :
 *       predicate                          # PredicateCondition
 *     | left=condition AND right=condition # AndCondition
 *     | left=condition OR right=condition  # OrCondition ;
 * }
 * {@snippet :
 * class ConditionContext extends ParserRuleContext {...}
 * class PredicateContext extends ConditionContext {...}
 * class AndConditionContext extends ConditionContext {...}
 * class OrConditionContext extends ConditionContext {...}
 * }
 *
 *     The visitor interface will not contain an explicit visiting method for the base type ({@code ConditionContext})
 *     but will instead dispatch on its actual type and invoke a corresponding visiting method (for one of its subtypes).
 *
 * {@snippet :
 * // ConditionContext is not visited, but dispatches on its actual type
 * ConditionContext cc = ..;
 * cc.accept(visitor);
 * }
 *
 *     The {@code condition} rule above involves 2 different kinds of rule body:
 *     <ol>
 *         <li> Inlined. This kind is elegant as it results in generation of a class that directly corresponds to the body.
 * {@snippet :
 * left=condition AND right=condition # AndCondition
 * }
 * {@snippet :
 * class AndConditionContext extends ConditionContext {
 *     final ConditionContext left;
 *     final ConditionContext right;
 * }
 * }
 *
 *         <li> Not inlined. This kind is the opposite of elegant as it involves an additional step of indirection.
 *
 * {@snippet :
 * predicate # PredicateCondition
 * }
 * {@snippet :
 * class PredicateConditionContext extends ConditionContext {
 *     public PredicateContext predicate() {...}
 * }
 * }
 *
 *              One consequence of this is that the visitor code must implement an additional method to handle the indirection.
 *
 * {@snippet :
 * public Object visitPredicateCondition(PredicateConditionContext ctx) {
 *     return ctx.predicate().accept(this);
 * }
 * }
 *              One reason for having rule bodies that are not inlined is the ability to reuse them.
 *     </ol>
 *
 * </ol>
 */
public final class EqlCompiler {

    private final QueryModelToStage1Transformer transformer;

    public EqlCompiler(final QueryModelToStage1Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Throws exception {@link EqlCompilationException} if parsing fails.
     *
     * @param tokenSource  source of tokens representing an expression to compile
     * @return  compilation result
     */
    public EqlCompilationResult compile(final ListTokenSource tokenSource) {
        final var tokenStream = new CommonTokenStream(tokenSource);
        final var parser = new EQLParser(tokenStream);

        parser.addErrorListener(new ThrowingErrorListener(tokenSource));

        // parsing stage, results in a complete parse tree
        final StartContext tree = parser.start();

        // compilation stage
        final var visitor = new Visitor();
        try {
            return visitor.visitStart(tree);
        } catch (final Exception e) {
            throw new EqlCompilationException(
                    """
                    Failed to compile an EQL expression.
                    Expression: %s
                    Source: %s
                    Reason: %s
                    """.formatted(
                            TokensFormatter.getInstance().format(tokenSource),
                            requireNonNullElse(tokenSource.getSourceName(), "unknown"),
                            requireNonNullElse(e.getLocalizedMessage(), "unknown")),
                    e);
        }
    }

    /**
     * Similar to {@link #compile(ListTokenSource)} but also ensures that the result type matches the given one --
     * if it doesn't, throws a runtime exception.
     */
    public <T extends EqlCompilationResult> T compile(final ListTokenSource tokenSource, final Class<T> resultType) {
        requireNonNull(resultType, "resultType can't be null");

        final EqlCompilationResult result = compile(tokenSource);
        if (resultType.isInstance(result)) {
            return (T) result;
        }
        throw new EqlCompilationException("Expected EQL expression of type %s but was: %s".formatted(
                resultType.getSimpleName(), result.getClass().getSimpleName()));
    }

    private final class Visitor extends StrictEQLBaseVisitor<EqlCompilationResult> {

        @Override
        public EqlCompilationResult visitStart(final StartContext ctx) {
            return ctx.query().accept(this);
        }

        @Override
        public EqlCompilationResult visitSelect(final SelectContext ctx) {
            return new SelectVisitor(transformer).visitSelect(ctx);
        }

        @Override
        public EqlCompilationResult visitStandaloneExpression(final StandaloneExpressionContext ctx) {
            return new StandaloneExpressionVisitor(transformer).visitStandaloneExpression(ctx);
        }

        @Override
        public EqlCompilationResult visitStandaloneCondExpr(final StandaloneCondExprContext ctx) {
            return new StandaloneConditionVisitor(transformer).visitStandaloneCondExpr(ctx);
        }

        @Override
        public EqlCompilationResult visitStandaloneOrderBy(final StandaloneOrderByContext ctx) {
            return new StandaloneOrderByVisitor(transformer).visitStandaloneOrderBy(ctx);
        }
    }

    private static final class ThrowingErrorListener extends BaseErrorListener {

        private final ListTokenSource tokenSource;

        public ThrowingErrorListener(final ListTokenSource tokenSource) {
            this.tokenSource = tokenSource;
        }

        @Override
        public void syntaxError(
                final Recognizer<?, ?> recognizer, final Object offendingSymbol,
                final int line, final int charPositionInLine, final String msg,
                final RecognitionException e) {
            throw new ParseCancellationException(
                    """
                    Failed to parse an EQL expression.
                    Expression: %s
                    Source: %s
                    Reason: %s
                    """.formatted(
                            TokensFormatter.getInstance().format(tokenSource),
                            requireNonNullElse(tokenSource.getSourceName(), "unknown"),
                            msg),
                    e);
        }

    }

}
