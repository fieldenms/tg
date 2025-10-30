package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.*;
import ua.com.fielden.platform.eql.stage1.operands.*;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.emptyCondition;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.buildCondition;
import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

final class ConditionVisitor extends AbstractEqlVisitor<ICondition1<?>> {

    ConditionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public Conditions1 visitAndCondition(final AndConditionContext ctx) {
        final Stream.Builder<ConditionContext> builder = Stream.builder();
        spliceAnd(ctx, builder::add);
        return Conditions1.and(builder.build().map(c -> c.accept(this)).toList());
    }

    private void spliceAnd(final AndConditionContext ctx, final Consumer<? super ConditionContext> sink) {
        if (ctx.left instanceof AndConditionContext andCtx) {
            spliceAnd(andCtx, sink);
        } else {
            sink.accept(ctx.left);
        }

        if (ctx.right instanceof AndConditionContext andCtx) {
            spliceAnd(andCtx, sink);
        } else {
            sink.accept(ctx.right);
        }
    }

    @Override
    public Conditions1 visitOrCondition(final OrConditionContext ctx) {
        final Stream.Builder<ConditionContext> builder = Stream.builder();
        spliceOr(ctx, builder::add);
        return Conditions1.or(builder.build().map(c -> c.accept(this)).toList());
    }

    private void spliceOr(final OrConditionContext ctx, final Consumer<? super ConditionContext> sink) {
        if (ctx.left instanceof OrConditionContext orCtx) {
            spliceOr(orCtx, sink);
        } else {
            sink.accept(ctx.left);
        }

        if (ctx.right instanceof OrConditionContext orCtx) {
            spliceOr(orCtx, sink);
        } else {
            sink.accept(ctx.right);
        }
    }

    @Override
    public ICondition1<?> visitCompoundCondition(final CompoundConditionContext ctx) {
        return ctx.condition().accept(this);
    }

    @Override
    public Conditions1 visitNegatedCompoundCondition(final NegatedCompoundConditionContext ctx) {
        return Conditions1.conditions(ctx.condition().accept(this)).negate();
    }

    @Override
    public ICondition1<?> visitPredicateCondition(final PredicateConditionContext ctx) {
        return ctx.predicate().accept(this);
    }

    @Override
    public ICondition1<?> visitLikePredicate(final LikePredicateContext ctx) {
        final LikeOptions options = switch (ctx.likeOperator().token.getType()) {
            case LIKE -> LikeOptions.DEFAULT_OPTIONS;
            case NOTLIKE -> LikeOptions.options().negated().build();
            case ILIKE -> LikeOptions.options().caseInsensitive().build();
            case NOTILIKE -> LikeOptions.options().caseInsensitive().negated().build();
            case LIKEWITHCAST -> LikeOptions.options().withCast().build();
            case NOTLIKEWITHCAST -> LikeOptions.options().withCast().negated().build();
            case ILIKEWITHCAST -> LikeOptions.options().withCast().caseInsensitive().build();
            case NOTILIKEWITHCAST -> LikeOptions.options().withCast().caseInsensitive().negated().build();
            default -> unexpectedToken(ctx.likeOperator().token);
        };
        final var visitor = new ComparisonOperandVisitor(transformer);
        final var rightFinisher = ctx.right.accept(visitor);
        return ctx.left.accept(visitor).apply(left -> rightFinisher.apply(right -> new LikePredicate1(left, right, options)));
    }

    @Override
    public ICondition1<?> visitSingleConditionPredicate(final SingleConditionPredicateContext ctx) {
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
    public ICondition1<?> visitComparisonPredicate(final ComparisonPredicateContext ctx) {
        final var visitor = new ComparisonOperandVisitor(transformer);
        final var rightFinisher = ctx.right.accept(visitor);
        final var comparisonOperator = toComparisonOperator(ctx.comparisonOperator());
        return ctx.left.accept(visitor).apply(left -> rightFinisher.apply(right -> new ComparisonPredicate1(left, comparisonOperator, right)));
    }

    @Override
    public ICondition1<?> visitUnaryPredicate(final UnaryPredicateContext ctx) {
        return ctx.left.accept(new ComparisonOperandVisitor(transformer)).apply(chooseUnaryPredicate(ctx));
    }

    private static Function<ISingleOperand1<? extends ISingleOperand2<?>>, ICondition1<?>> chooseUnaryPredicate(final UnaryPredicateContext ctx) {
        final Token token = ctx.unaryComparisonOperator().token;

        return switch (token.getType()) {
            case ISNULL -> op -> new NullPredicate1(op, false);
            case ISNOTNULL -> op -> new NullPredicate1(op, true);
            default -> unexpectedToken(token);
        };
    }

    @Override
    public ICondition1<?> visitMembershipPredicate(final MembershipPredicateContext ctx) {
        final boolean negated = switch (ctx.membershipOperator().token.getType()) {
            case IN -> false;
            case NOTIN -> true;
            default -> unexpectedToken(ctx.membershipOperator().token);
        };
        return ctx.comparisonOperand().accept(new ComparisonOperandVisitor(transformer))
                // to be consistent with source ID generation logic, the membership operand has to be recompiled for each comparison operand
                // TODO room for optimisation
                .apply(operand -> new SetPredicate1(operand, negated, compileMembershipOperand(ctx.membershipOperand())));
    }

    private ISetOperand1<? extends ISetOperand2<?>> compileMembershipOperand(final MembershipOperandContext ctx) {
        return switch (ctx.token) {
            case ValuesToken tok -> new OperandsBasedSet1(tok.values.stream().map(v -> Value1.value(preprocessValue(v))).toList());
            case PropsToken tok -> new OperandsBasedSet1(tok.props.stream().map(p -> new Prop1(p, false)).toList());
            case ParamsToken tok -> new OperandsBasedSet1(tok.params.stream().flatMap(p -> substParam(p, false)).toList());
            case IParamsToken tok -> new OperandsBasedSet1(tok.params.stream().flatMap(p -> substParam(p, true)).toList());
            case QueryModelToken<?> tok -> new QueryBasedSet1(transformer.generateAsSubQuery(tok.model));
            default -> unexpectedToken(ctx.token);
        };
    }

    @Override
    public ICondition1<?> visitQuantifiedComparisonPredicate(final QuantifiedComparisonPredicateContext ctx) {
        final var operator = toComparisonOperator(ctx.comparisonOperator());
        final Quantifier quantifier = switch (ctx.quantifiedOperand().token) {
            case AllToken $ -> Quantifier.ALL;
            case AnyToken $ -> Quantifier.ANY;
            default -> unexpectedToken(ctx.quantifiedOperand().token);
        };
        return ctx.left.accept(new ComparisonOperandVisitor(transformer))
                // to be consistent with source ID generation logic, the quantified operand has to be recompiled for each comparison operand
                // TODO room for optimisation
                .apply(leftRand -> new QuantifiedPredicate1(leftRand, operator, quantifier, compileQuantifiedOperand(ctx.quantifiedOperand())));
    }

    private SubQuery1 compileQuantifiedOperand(final QuantifiedOperandContext ctx) {
        return switch (ctx.token) {
            case AllToken tok -> transformer.generateAsSubQuery(tok.model);
            case AnyToken tok -> transformer.generateAsSubQuery(tok.model);
            default -> unexpectedToken(ctx.token);
        };
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
        // TODO Ensure that `propName` refers to a property whose type is compatible with the type of `qp`.
        //      This validation requires access to semantic information about `collectionQueryStart`,
        //      which is available only in the EQL transformation pipeline (stages 1-3).
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

    private static ComparisonOperator toComparisonOperator(final ComparisonOperatorContext ctx) {
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

