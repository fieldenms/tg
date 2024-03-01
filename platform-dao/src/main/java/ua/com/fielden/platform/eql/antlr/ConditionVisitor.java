package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.*;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.emptyCondition;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.buildCondition;

final class ConditionVisitor extends AbstractEqlVisitor<ICondition1<?>> {

    ConditionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public Conditions1 visitAndCondition(final EQLParser.AndConditionContext ctx) {
        return Conditions1.and(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Conditions1 visitOrCondition(final EQLParser.OrConditionContext ctx) {
        return Conditions1.or(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public ICondition1<?> visitCompoundCondition(final EQLParser.CompoundConditionContext ctx) {
        return ctx.condition().accept(this);
    }

    @Override
    public Conditions1 visitNegatedCompoundCondition(final EQLParser.NegatedCompoundConditionContext ctx) {
        return Conditions1.conditions(ctx.condition().accept(this)).negate();
    }

    @Override
    public ICondition1<?> visitPredicateCondition(final EQLParser.PredicateConditionContext ctx) {
        return ctx.predicate().accept(this);
    }

    @Override
    public LikePredicate1 visitLikePredicate(final EQLParser.LikePredicateContext ctx) {
        final var visitor = new ComparisonOperandVisitor(transformer);
        return new LikePredicate1(ctx.left.accept(visitor), ctx.right.accept(visitor), LikeOptions.options().build());
    }

    @Override
    public ICondition1<?> visitSingleConditionPredicate(final EQLParser.SingleConditionPredicateContext ctx) {
        return switch (ctx.token) {
            case ExistsToken tok -> makeExistencePredicate(false, tok.model);
            case NotExistsToken tok -> makeExistencePredicate(true, tok.model);
            case ExistsAnyOfToken tok -> Conditions1.or(tok.models.stream().map(m -> makeExistencePredicate(false, m)).toList());
            case NotExistsAnyOfToken tok -> Conditions1.or(tok.models.stream().map(m -> makeExistencePredicate(true, m)).toList());
            case ExistsAllOfToken tok -> Conditions1.and(tok.models.stream().map(m -> makeExistencePredicate(false, m)).toList());
            case NotExistsAllOfToken tok -> Conditions1.and(tok.models.stream().map(m -> makeExistencePredicate(true, m)).toList());
            case CritConditionToken tok -> compileConditionModel(critConditionOperatorModel(tok));
            case ConditionToken tok -> compileConditionModel(tok.model);
            case NegatedConditionToken tok -> compileConditionModel(tok.model).negate();
            default -> unexpectedToken(ctx.token);
        };
    }

    private Conditions1 compileConditionModel(final ConditionModel model) {
        return new EqlCompiler(transformer).compile(model.getTokenSource(), EqlCompilationResult.StandaloneCondition.class).model();
    }

    private ExistencePredicate1 makeExistencePredicate(final boolean negated, final QueryModel model) {
        return new ExistencePredicate1(negated, transformer.generateAsSubQueryForExists(model));
    }

    @Override
    public ICondition1<?> visitComparisonPredicate(final EQLParser.ComparisonPredicateContext ctx) {
        return new ComparisonPredicate1(
                ctx.left.accept(new ComparisonOperandVisitor(transformer)),
                toComparisonOperator(ctx.comparisonOperator()),
                ctx.right.accept(new ComparisonOperandVisitor(transformer)));
    }

    // ----------------------------------------
    // CRIT CONDITION

    private ConditionModel critConditionOperatorModel(final CritConditionToken token) {
        final QueryProperty qp = (QueryProperty) getParamValue(queryPropertyParamName(token.critProp));

        if (qp != null && qp.isEmptyWithoutMnemonics()) {
            token.defaultValue.ifPresent(dv -> {
                if (dv instanceof List || dv instanceof String) {
                    qp.setValue(dv);
                }
                else if (dv instanceof T2<?,?> t2) {
                    qp.setValue(t2._1);
                    qp.setValue2(t2._2);
                }
                else {
                    throw new EqlException(
                            "Default value for property [%s] in a [critCondition] call has unsupported type [%s].".formatted(
                                    token.critProp, dv.getClass().getTypeName()));
                }
            });
        }

        if (qp == null || qp.isEmptyWithoutMnemonics()) {
            return emptyCondition();
        } else if (token.collectionQueryStart == null) {
            return buildCondition(qp, token.prop, false, transformer.nowValue.dates);
        } else {
            return collectionalCritConditionOperatorModel(token.collectionQueryStart, token.prop, qp);
        }
    }

    /**
     * The following rules are used to build {@code ConditionModel}.
     * <pre>
     * v n m
     * + + +  not (exists collectional element that matches any of the values || empty) == there are no collectional elements that match any of values && not empty
     * + + -  not (exists collectional element that matches any of the values && not empty) == there are no collectional elements that match any of values || empty
     * + - +  exists collectional element that matches any of the values || empty
     * + - -  exists collectional element that matches any of the values && not empty
     * - + +  not empty
     * - + -  no condition
     * - - +  empty
     * - - -  no condition
     * </pre>
     */
    private ConditionModel collectionalCritConditionOperatorModel(
            final ICompoundCondition0<?> collectionQueryStart, final String propName, final QueryProperty qp)
    {
        final boolean hasValue = !qp.isEmpty();
        final boolean not = TRUE.equals(qp.getNot());
        final boolean orNull = TRUE.equals(qp.getOrNull());

        final ConditionModel criteriaCondition = prepareCollectionalCritCondition(qp, propName);
        final EntityResultQueryModel<?> anyItems = collectionQueryStart.model();
        final EntityResultQueryModel<?> matchingItems = collectionQueryStart.and().condition(criteriaCondition).model();

        if (!hasValue) {
            return !orNull ? emptyCondition()/*---,-+-*/ : (not ? cond().exists(anyItems).model()/*-++*/ : cond().notExists(anyItems).model())/*--+*/;
        } else if (not){
            return orNull ? cond().notExists(matchingItems).and().exists(anyItems).model()/*+++*/ : cond().notExists(matchingItems).or().notExists(anyItems).model()/*++-*/;
        } else {
            return !orNull ? cond().exists(matchingItems).model()/*+--*/ : cond().exists(matchingItems).or().notExists(anyItems).model()/*+-+*/;
        }
    }

    private ConditionModel prepareCollectionalCritCondition(final QueryProperty qp, final String propName) {
        final Boolean originalOrNull = qp.getOrNull();
        final Boolean originalNot = qp.getNot();
        qp.setOrNull(null);
        qp.setNot(null);
        final ConditionModel result = qp.isEmptyWithoutMnemonics() ? emptyCondition() : buildCondition(qp, propName, false, transformer.nowValue.dates);
        qp.setOrNull(originalOrNull);
        qp.setNot(originalNot);
        return result;
    }

    // ----------------------------------------

    private static ComparisonOperator toComparisonOperator(final EQLParser.ComparisonOperatorContext ctx) {
        return switch (ctx.token.getType()) {
            case EQLLexer.EQ -> ComparisonOperator.EQ;
            case EQLLexer.NE -> ComparisonOperator.NE;
            case EQLLexer.GT -> ComparisonOperator.GT;
            case EQLLexer.GE -> ComparisonOperator.GE;
            case EQLLexer.LT -> ComparisonOperator.LT;
            case EQLLexer.LE -> ComparisonOperator.LE;
            default -> unexpectedToken(ctx.token);
        };
    }

}

