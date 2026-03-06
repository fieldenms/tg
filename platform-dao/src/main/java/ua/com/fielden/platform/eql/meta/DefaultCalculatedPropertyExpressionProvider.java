package ua.com.fielden.platform.eql.meta;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IDates;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.IMPLICIT;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public record DefaultCalculatedPropertyExpressionProvider(
        IUserProvider userProvider,
        IDates dates,
        IFilter filter,
        IDomainMetadata domainMetadata)
        implements ICalculatedPropertyExpressionProvider
{

    static final String
            ERR_COULD_NOT_DETERMINE_EXPRESSION = "Could not determine expression for calculated property [%s.%s].",
            ERR_COULD_NOT_LOAD_TYPE = "Could not load type [%s].";

    @Inject public DefaultCalculatedPropertyExpressionProvider {}

    @Override
    public Optional<CalcPropInfo> maybeExpression(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence property)
    {
        final var propPath = splitPropPath(property);
        final var fstPropMd = domainMetadata.forProperty(entityType, propPath.getFirst());
        if (propPath.size() == 2 && isUnionEntityType(fstPropMd.type().javaType())) {
            return domainMetadata.forProperty(entityType, property)
                    .asCalculated()
                    .map(_ -> maybePropertyInUnion((Class<? extends AbstractUnionEntity>) fstPropMd.type().javaType(), propPath.getFirst(), propPath.get(1))
                            .orElseThrow(missingExpression(entityType, property)));
        }
        else if (propPath.size() == 2 && fstPropMd.type().isComponent()) {
            return domainMetadata.forProperty(entityType, property)
                    .asCalculated()
                    .map(_ -> maybePropertyInComponent(entityType, propPath.getFirst(), propPath.get(1))
                            .orElseThrow(missingExpression(entityType, property)));
        }
        else if (propPath.size() == 1) {
            return fstPropMd
                    .asCalculated()
                    .map(_ -> maybeFromDeclaration(entityType, property.toString())
                            .or(() -> maybeImplicit(entityType, property.toString()))
                            .orElseThrow(missingExpression(entityType, property)));
        }
        else {
            throw new InvalidArgumentException(format("Expression is not defined for property [%s.%s].", entityType.getSimpleName(), property));
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

    private Optional<CalcPropInfo> maybePropertyInComponent(
            final Class<? extends AbstractEntity<?>> entityType,
            final String componentTypedProperty,
            final String subProperty)
    {
        final var componentTypedPropertyMd = domainMetadata.forProperty(entityType, componentTypedProperty);
        final var componentType = componentTypedPropertyMd.type().javaType();
        if (componentType.equals(Money.class)) {
            // For `amount` use the Money-typed property's expression.
            if (subProperty.equals("amount")) {
                return maybeExpression(entityType, componentTypedProperty);
            }
            else if (subProperty.equals("currency")) {
                // TODO Should attain the currency of the first Money-typed property used in tail position in the expression for `amount`.
                // TODO Support explicit definition of expressions for `currency` at the level of Money-typed properties.
                return Optional.of(new CalcPropInfo(expr().val(null).model(), EXPRESSION));
            }
            else {
                throw new EntityDefinitionException(format("Unsupported property: [%s.%s].", Money.class.getSimpleName(), subProperty));
            }
        }
        else {
            throw new EntityDefinitionException(format(
                    "Invalid calculated property [%s.%s]. Type [%s] is unsupported for calculated properties.",
                    entityType.getSimpleName(), componentTypedProperty, componentType.getSimpleName()));
        }
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
                    final ExpressionModel model;
                    try {
                        if (!atCalculated.value().equals(Calculated.EMPTY)) {
                            model = createExpressionText2ModelConverter(entityType, atCalculated).convert().getModel();
                        }
                        else {
                            model = getStaticFieldValue(getFieldByName(entityType, property + "_"));
                        }
                    } catch (final Exception ex) {
                        throw new DomainMetadataGenerationException(ERR_COULD_NOT_DETERMINE_EXPRESSION.formatted(entityType.getSimpleName(), property), ex);
                    }
                    return new CalcPropInfo(model, atCalculated.category());
                });
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
