package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.tokens.PropToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

import java.util.List;

/**
 * Compiles EQL sentences (sequences of tokens) into Stage 1 representation.
 *
 * <h3>ANTLR visitor implementation patterns</h3>
 *
 * <ol>
 *     <li> Terminal derivation rules: context class with a {@code token} field holding the value of the concrete token.
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
 *     Visitor code should use the value of {@code token} to determine the actual token.
 *     <p>
 *     If the token is parameterised it will have a corresponding custom token type.
 *     For example, {@code prop("a.b.c")} would be represented by an instance of {@link PropToken} carrying the specified property path.
 *     In such cases the {@code token} field should be type-casted to the respective type.
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
 *     but rather will dispatch on its actual type and invoke the respective visiting method (for one of its subtypes).
 *
 * {@snippet :
 * // ConditionContext is not visited, but rather dispatches on its actual ype
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
 *              One consequence of this is that visitor code must implement an additional method to handle the indirection.
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

    public EqlCompilationResult compile(final List<? extends Token> tokens) {
        final var tokenStream = new CommonTokenStream(new ListTokenSource(tokens));
        final var parser = new EQLParser(tokenStream);
        final var visitor = new Visitor();
        try {
            return parser.start().accept(visitor);
        } catch (RecognitionException e) {
            throw new RuntimeException("Compilation of an EQL expression failed", e);
        }
    }

    private final class Visitor extends EQLBaseVisitor<EqlCompilationResult> {

        @Override
        public EqlCompilationResult visitStart(final EQLParser.StartContext ctx) {
            return ctx.query().accept(this);
        }

        @Override
        public EqlCompilationResult visitQuery_Select(final EQLParser.Query_SelectContext ctx) {
            return new SelectVisitor(transformer).visitQuery_Select(ctx);
        }

        @Override
        public EqlCompilationResult visitStandaloneExpression(final EQLParser.StandaloneExpressionContext ctx) {
            return new StandaloneExpressionVisitor(transformer).visitStandaloneExpression(ctx);
        }

        @Override
        public EqlCompilationResult visitStandaloneCondExpr(final EQLParser.StandaloneCondExprContext ctx) {
            return new StandaloneConditionVisitor(transformer).visitStandaloneCondExpr(ctx);
        }

        @Override
        public EqlCompilationResult visitOrderBy(final EQLParser.OrderByContext ctx) {
            return new OrderByVisitor(transformer).visitOrderBy(ctx);
        }

    }

}
