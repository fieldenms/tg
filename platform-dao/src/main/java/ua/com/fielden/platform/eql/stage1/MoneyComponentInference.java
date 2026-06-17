package ua.com.fielden.platform.eql.stage1;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CaseWhen1;
import ua.com.fielden.platform.eql.stage1.operands.functions.Concat1;
import ua.com.fielden.platform.eql.stage1.operands.functions.SingleOperandFunction1;
import ua.com.fielden.platform.eql.stage1.operands.functions.TwoOperandsFunction1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.utils.CollectionUtil.dropRight;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPath;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

/// Provides the ability to infer EQL expressions for components of [Money] from an expression whose type is [Money].
///
public class MoneyComponentInference {

    private static final String
            ERR_NOT_SUPPORTED_FOR_SUBQUERIES = "Inference is not supported for sub-queries.",
            ERR_NO_RESULTS = "Inference did not produce any results.";

    private final IDomainMetadata domainMetadata;

    // TODO Make protected after refactoring EQL tests to use IoC.
    @Inject
    public MoneyComponentInference(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /// Infers the expression for a [Money] component.
    ///
    /// @param operand
    ///     [Money] expression to be analysed.
    /// @param componentName
    ///     name of the [Money] component.
    /// @param isMoneyWithComponent
    ///     a predicate that is true iff a [Prop1] has type [Money] with component `componentName`.
    ///     This class provides several methods to create such a predicate.
    /// @return
    ///     If inference is successful, [Right] with the inferred expression.
    ///     Otherwise, [Left] with an error message.
    ///
    public Either<String, ExpressionModel> infer(
            final ISingleOperand1<?> operand,
            final CharSequence componentName,
            final Predicate<Prop1> isMoneyWithComponent)
    {
        final var operands = tailPositionOperands(operand).toList();
        if (operands.stream().anyMatch(o -> o instanceof SubQuery1)) {
            return left(ERR_NOT_SUPPORTED_FOR_SUBQUERIES);
        }
        // Pick the first property whose type is Money with the required component.
        return operands.stream()
                .mapMulti(typeFilter(Prop1.class))
                .filter(isMoneyWithComponent)
                .map(prop1 -> expr().prop(prop1.propPath() + "." + componentName).model())
                .findFirst()
                .<Either<String, ExpressionModel>>map(Either::right)
                .orElseGet(() -> left(ERR_NO_RESULTS));
    }

    /// Creates a predicate for [#infer] which assumes that the [Prop1] under test represents a property path starting at `entityType`.
    /// This method should be used only when it is certain that the source of [Prop1] is `entityType`.
    ///
    public Predicate<Prop1> predicateIsMoneyWithComponent(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence componentName)
    {
        return prop1 -> {
            final var pm = domainMetadata.forProperty(entityType, prop1.propPath());
            return pm.type().javaType().equals(Money.class)
                   && domainMetadata.propertyMetadataUtils().hasSubProperty(pm, componentName);
        };
    }

    /// Creates a predicate for [#infer] which assumes that the source of [Prop1] under test can be obtained from `context`.
    /// This method should be used when [Prop1] comes from an arbitrary query that may include joins.
    ///
    public Predicate<Prop1> predicateIsMoneyWithComponent(
            final TransformationContextFromStage1To2 context,
            final CharSequence componentName)
    {
        return prop1 -> {
            final var resolution = Prop1.resolveProp(prop1, context);
            return resolution.getPath().getLast() instanceof QuerySourceItemForComponentType<?> item
                   && item.javaType().equals(Money.class)
                   && item.getSubitems().containsKey(componentName.toString());
        };
    }

    /// Given an arbitrary operand, returns a stream of operands that appear in tail position.
    /// A more suitable term would be "expression" rather than "operand", but "expression" in this context could be confused
    /// with a specific type of operand -- [Expression1].
    ///
    /// For an operand to be in tail position, it must directly contribute to the resulting value.
    ///
    /// ### Why sub-queries are unsupported
    ///
    /// For [SubQuery1], this method acts as identity, because its analysis and further transformation requires significant complexity.
    /// * It would require an analysis of the yields, which get enhanced in Stage 2, but we are operating on Stage 1 AST,
    ///   and switching to Stage 2 is rather complex.
    /// * It would require the yields to be transformed so that only [Money#currency] is yielded, but all other parts
    ///   of the sub-query to be preserved, such as where conditions.
    /// * The transformed sub-query would have to be converted from its AST form into "syntax", using the EQL Fluent API.
    ///
    private Stream<ISingleOperand1<?>> tailPositionOperands(final ISingleOperand1<?> operand) {
        return switch (operand) {
            // Although Expression1 itself already is in tail position, let's expand it into operands to avoid additional processing later.
            case Expression1 it -> it.streamOperands().flatMap(this::tailPositionOperands);
            case Concat1 it -> it.operands().stream().flatMap(this::tailPositionOperands);
            case CaseWhen1 it -> Stream.concat(it.whenThenPairs().stream().map(T2::_2), it.maybeElseOperand().stream())
                    .flatMap(this::tailPositionOperands);
            case SingleOperandFunction1<?> it -> tailPositionOperands(it.operand);
            case TwoOperandsFunction1<?> it -> Stream.of(it.operand1, it.operand2).flatMap(this::tailPositionOperands);
            default -> Stream.of(operand);
        };
    }

    private boolean pathEndsWith(
            final Class<? extends AbstractEntity<?>> root,
            final String path,
            final Class<?> lastPropSource,
            final String lastProp)
    {
        if (root.equals(lastPropSource) && path.equals(lastProp)) {
            return true;
        }

        final var pathList = splitPropPath(path);
        return pathList.size() > 1
               && pathList.getLast().equals(lastProp)
               && domainMetadata.forProperty(root, String.join(".", dropRight(pathList, 1))).type().javaType().equals(lastPropSource);
    }
}
