package ua.com.fielden.platform.eql.meta;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.EqlCompilationResult;
import ua.com.fielden.platform.eql.antlr.EqlCompiler;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CaseWhen1;
import ua.com.fielden.platform.eql.stage1.operands.functions.Concat1;
import ua.com.fielden.platform.eql.stage1.operands.functions.SingleOperandFunction1;
import ua.com.fielden.platform.eql.stage1.operands.functions.TwoOperandsFunction1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ArrayUtils;
import ua.com.fielden.platform.utils.IDates;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.IMPLICIT;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.getFieldByNameOptionally;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.utils.CollectionUtil.dropRight;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

record DefaultCalculatedPropertyExpressionProvider(
        IUserProvider userProvider,
        IDates dates,
        IFilter filter,
        IDomainMetadata domainMetadata)
        implements ICalculatedPropertyExpressionProvider
{

    static final String
            ERR_COULD_NOT_DETERMINE_EXPRESSION = "Could not determine expression for calculated property [%s.%s].",
            ERR_COULD_NOT_LOAD_TYPE = "Could not load type [%s].",
            ERR_CANNOT_INFER_FROM_SUB_QUERY = "Expression for calculated property [%s.%s.%s] cannot be inferred when a sub-query is used. Please define [" + ExpressionModel.class.getSimpleName() + " %s_%s_].",
            ERR_CANNOT_INFER = "Expression for calculated property [%s.%s.%s] cannot be inferred. Please define [" + ExpressionModel.class.getSimpleName() + " %s_%s_].",
            ERR_EXPRESSION_NOT_DEFINED = "Expression is not defined for property [%s.%s].",
            ERR_UNSUPPORTED_CALCULATED_PROPERTY = "Unsupported calculated property: [%s.%s.%s].",
            ERR_UNSUPPORTED_TYPE_FOR_CALCULATED_PROPERTY = "Invalid calculated property [%s.%s]. Type [%s] is unsupported for calculated properties.";

    @Inject DefaultCalculatedPropertyExpressionProvider {}

    @Override
    public Optional<CalcPropInfo> maybeExpression(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence property)
    {
        final var propPath = splitPropPath(property);
        final var mdFstProp = domainMetadata.forProperty(entityType, propPath.getFirst());
        if (propPath.size() == 2 && isUnionEntityType(mdFstProp.type().javaType())) {
            return domainMetadata.forProperty(entityType, property)
                    .asCalculated()
                    .map(_ -> maybePropertyInUnion((Class<? extends AbstractUnionEntity>) mdFstProp.type().javaType(), propPath.getFirst(), propPath.get(1))
                            .orElseThrow(missingExpression(entityType, property)));
        }
        else if (propPath.size() == 2 && mdFstProp.type().isComponent()) {
            return maybePropertyInComponent(entityType, propPath.getFirst(), propPath.get(1));
        }
        else if (propPath.size() == 1) {
            return mdFstProp
                    .asCalculated()
                    .map(_ -> maybeFromDeclaration(entityType, property.toString())
                            .or(() -> maybeImplicit(entityType, property.toString()))
                            .orElseThrow(missingExpression(entityType, property)));
        }
        else {
            throw new InvalidArgumentException(format(ERR_EXPRESSION_NOT_DEFINED, entityType.getSimpleName(), property));
        }
    }

    private Optional<CalcPropInfo> maybePropertyInUnion(
            final Class<? extends AbstractUnionEntity> unionType,
            final @Nullable String unionTypedProperty,
            final String subProperty)
    {
        final var mdUnion = domainMetadata.forEntity(unionType).asUnion().orElseThrow();
        final var unionMembers = domainMetadata.entityMetadataUtils().unionMembers(mdUnion);
        if (subProperty.equals(DESC)) {
            final var membersWithDesc = unionMembers.stream()
                    .filter(member -> hasDescProperty(member.type().asEntity().orElseThrow().javaType()))
                    .map(PropertyMetadata::name)
                    .toList();
            final var model = expressionForCommonUnionProperty(membersWithDesc, unionTypedProperty, subProperty);
            return Optional.of(new CalcPropInfo(model, IMPLICIT));
        }
        else if (subProperty.equals(ID) || subProperty.equals(KEY) || commonProperties(mdUnion.javaType()).contains(subProperty)) {
            final var model = expressionForCommonUnionProperty(unionMembers.stream().map(PropertyMetadata::name).toList(), unionTypedProperty, subProperty);
            return Optional.of(new CalcPropInfo(model, IMPLICIT));
        }
        else {
            return Optional.empty();
        }
    }

    /// Returns an expression for a sub-property of a component-typed property if it is calculated.
    /// This method may accept properties of any nature.
    ///
    private Optional<CalcPropInfo> maybePropertyInComponent(
            final Class<? extends AbstractEntity<?>> entityType,
            final String componentTypedProperty,
            final String subProperty)
    {
        final var mdComponentTypedProperty = domainMetadata.forProperty(entityType, componentTypedProperty);
        final var componentType = mdComponentTypedProperty.type().javaType();
        if (componentType.equals(Money.class)) {
            return switch (subProperty) {
                // If the Money-typed property is calculated, then so is `amount`.
                // For `amount` use the Money-typed property's expression.
                case "amount" -> mdComponentTypedProperty
                        .asCalculated()
                        .map(_ -> maybeExpression(entityType, componentTypedProperty)
                                  .orElseThrow(missingExpression(entityType, componentTypedProperty)));
                // If the Money-typed property is calculated, then so is `currency`.
                case "currency" -> {
                    if (!mdComponentTypedProperty.isCalculated()) {
                        yield Optional.empty();
                    }

                    // Perhaps there is a declared model for currency.
                    yield maybeFromDeclaredExpressionField(entityType, "%s.%s".formatted(componentTypedProperty, subProperty))
                            // Try to infer it from the Money-typed property's expression.
                            .or(() -> {
                                final var moneyModel = maybeExpression(entityType, componentTypedProperty)
                                        .orElseThrow(missingExpression(entityType, componentTypedProperty))
                                        .expressionModel();
                                final var gen = new QueryModelToStage1Transformer(filter, userProvider.getUsername(), new QueryNowValue(dates), Map.of());
                                final Expression1 expr1 = new EqlCompiler(gen).compile(moneyModel.getTokenSource(), EqlCompilationResult.StandaloneExpression.class).model();
                                final var operands = tailPositionOperands(expr1).toList();
                                if (operands.stream().anyMatch(o -> o instanceof SubQuery1)) {
                                    throw new EntityDefinitionException(format(
                                            ERR_CANNOT_INFER_FROM_SUB_QUERY,
                                            entityType.getSimpleName(), componentTypedProperty, "currency", componentTypedProperty, "currency"));
                                }
                                // Pick the first property that is either Money-typed or refers to Money.amount.
                                // Since we do not support sub-queries here, this Prop1 should always belong to `entityType`.
                                final var currencyModel = operands.stream()
                                        .mapMulti(typeFilter(Prop1.class))
                                        .map(prop1 -> {
                                            final var normPath = pathEndsWith(entityType, prop1.propPath(), Money.class, "amount")
                                                    ? substringBefore(prop1.propPath(), ".amount")
                                                    : prop1.propPath();
                                            final var pm = domainMetadata.forProperty(entityType, normPath);
                                            // Checking for Money is not enough, as a custom Hibernate type without `currency` may be used.
                                            return pm.type().javaType().equals(Money.class)
                                                   && pm.hibType() instanceof ICompositeUserTypeInstantiate it
                                                   && ArrayUtils.contains(it.getPropertyNames(), "currency")
                                                    ? expr().prop(normPath + ".currency").model()
                                                    : null;
                                        })
                                        .filter(Objects::nonNull)
                                        .findFirst()
                                        .orElseThrow(() -> new EntityDefinitionException(format(
                                                ERR_CANNOT_INFER,
                                                entityType.getSimpleName(), componentTypedProperty, "currency", componentTypedProperty, "currency")));
                                return Optional.of(new CalcPropInfo(currencyModel, EXPRESSION));
                            });
                }
                default -> {
                    if (mdComponentTypedProperty.isCalculated()) {
                        throw new EntityDefinitionException(format(
                                ERR_UNSUPPORTED_CALCULATED_PROPERTY,
                                entityType.getSimpleName(), componentTypedProperty, subProperty));
                    }
                    yield Optional.empty();
                }
            };
        }
        else if (mdComponentTypedProperty.isCalculated()) {
            throw new EntityDefinitionException(format(
                    ERR_UNSUPPORTED_TYPE_FOR_CALCULATED_PROPERTY,
                    entityType.getSimpleName(), componentTypedProperty, componentType.getSimpleName()));
        }
        else {
            return Optional.empty();
        }
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

    private Optional<CalcPropInfo> maybeImplicit(
            final Class<? extends AbstractEntity<?>> entityType,
            final String property)
    {
        final var mdEntity = domainMetadata.forEntity(entityType);
        final var propType = PropertyTypeDeterminator.determinePropertyType(entityType, property);
        if (propType.equals(DynamicEntityKey.class)) {
            return Optional.of(new CalcPropInfo(generateCompositeKeyEqlExpression((Class) entityType), IMPLICIT));
        }
        // Synthetic one-2-one.
        else if (property.equals(ID) && mdEntity.isSynthetic() && isEntityType(getKeyType(entityType))) {
            return Optional.of(new CalcPropInfo(expr().prop(KEY).model(), IMPLICIT));
        }
        else if (isOne2One_association(entityType, property.toString())) {
            // Properties representing one-2-one associations are implicitly calculated nullable properties.
            // Instances of one-2-one are not required to exist, but in practice they always do get created and saved together with the main entity.
            final var expressionModel = expr()
                    .model(select((Class<? extends AbstractEntity<?>>) propType).where().prop(KEY).eq().extProp(ID).model())
                    .model();
            return Optional.of(new CalcPropInfo(expressionModel, IMPLICIT));
        }
        else if (mdEntity.isUnion()) {
            return maybePropertyInUnion(mdEntity.asUnion().orElseThrow().javaType(), null, property);
        }
        else {
            return Optional.empty();
        }
    }

    /// Extracts a calculated property's expression from its declaration.
    ///
    private Optional<CalcPropInfo> maybeFromDeclaration(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        // There are properties that do not correspond to fields (e.g., common union properties).
        // For them there will be no declaration information, hence we will return an empty optional.
        return maybeField(entityType, property)
                .flatMap(field -> getAnnotationOptionally(field, Calculated.class))
                .map(atCalculated -> {
                    try {
                        if (!atCalculated.value().equals(Calculated.EMPTY)) {
                            return new CalcPropInfo(createExpressionText2ModelConverter(entityType, atCalculated).convert().getModel(),
                                                    atCalculated.category());
                        }
                        else {
                            return maybeFromDeclaredExpressionField(entityType, property).orElseThrow();
                        }
                    } catch (final Exception ex) {
                        throw new DomainMetadataGenerationException(ERR_COULD_NOT_DETERMINE_EXPRESSION.formatted(entityType.getSimpleName(), property), ex);
                    }
                });
    }

    private Optional<CalcPropInfo> maybeFromDeclaredExpressionField(
            final Class<? extends AbstractEntity<?>> entityType,
            final String property)
    {
        final var staticFieldName = String.join("_", splitPropPath(property)) + "_";
        return getFieldByNameOptionally(entityType, staticFieldName)
                .<ExpressionModel>map(Finder::getStaticFieldValue)
                .map(model -> new CalcPropInfo(model, EXPRESSION));
    }

    private static ExpressionText2ModelConverter createExpressionText2ModelConverter(
            final Class<? extends AbstractEntity<?>> entityType,
            final Calculated atCalculated)
    {
        if (isContextual(atCalculated)) {
            return new ExpressionText2ModelConverter(getRootType(atCalculated), atCalculated.contextPath(), atCalculated.value());
        }
        else {
            return new ExpressionText2ModelConverter(entityType, atCalculated.value());
        }
    }

    private static Class<? extends AbstractEntity<?>> getRootType(final Calculated atCalculated) {
        try {
            return (Class<? extends AbstractEntity<?>>) DynamicEntityClassLoader.loadType(atCalculated.rootTypeName());
        } catch (final Exception ex) {
            throw new DomainMetadataGenerationException(ERR_COULD_NOT_LOAD_TYPE.formatted(atCalculated.rootTypeName()), ex);
        }
    }

    private ExpressionModel expressionForCommonUnionProperty(
            final List<String> unionMembers,
            final @Nullable String unionTypedProperty,
            final String subProperty)
    {
        if (unionMembers.isEmpty()) {
            return expr().val(null).model();
        }
        final var iterator = unionMembers.iterator();
        final var firstUnionPropName = (unionTypedProperty == null ? "" : unionTypedProperty + ".") + iterator.next();
        var expr = expr().caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName + "." + subProperty);
        while (iterator.hasNext()) {
            final var unionPropName = (unionTypedProperty == null ? "" : unionTypedProperty + ".") + iterator.next();
            expr = expr.when().prop(unionPropName).isNotNull().then().prop(unionPropName + "." + subProperty);
        }

        return expr.end().model();
    }

    private static Supplier<EqlMetadataGenerationException> missingExpression(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence property)
    {
        return () -> new EqlMetadataGenerationException(ERR_COULD_NOT_DETERMINE_EXPRESSION.formatted(entityType.getSimpleName(), property));
    }

    private Optional<Field> maybeField(final Class<?> root, final String name) {
        try {
            return Optional.of(root.getDeclaredField(name));
        } catch (final NoSuchFieldException e) {
            final var superclass = root.getSuperclass();
            return superclass == null ? Optional.empty() : maybeField(superclass, name);
        }
    }

}
