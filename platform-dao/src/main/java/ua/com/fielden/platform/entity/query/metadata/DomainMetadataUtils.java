package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isContextual;
import static ua.com.fielden.platform.reflection.Finder.getFieldByName;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.Pair;

public class DomainMetadataUtils {

    /** Private default constructor to prevent instantiation. */
    private DomainMetadataUtils() {
    }
    
    public static ExpressionModel generateUnionEntityPropertyExpression(final Class<? extends AbstractUnionEntity> entityType, final String commonPropName) {
        final List<Field> props = unionProperties(entityType);
        final Iterator<Field> iterator = props.iterator();
        final String firstUnionPropName = iterator.next().getName();
        ICaseWhenFunctionWhen<IStandAloneExprOperationAndClose, AbstractEntity<?>> expressionModelInProgress = expr().caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName
                + "." + commonPropName);

        for (; iterator.hasNext();) {
            final String unionPropName = iterator.next().getName();
            expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName + "." + commonPropName);
        }

        return expressionModelInProgress.otherwise().val(null).end().model();
    }

    public static ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType) {
        final List<T3<String, Class<?>, Boolean>> keyMembersWithOptionality = new ArrayList<>();
        for (final Field field : getKeyMembers(entityType)) {
            keyMembersWithOptionality.add(new T3<String, Class<?>, Boolean>(field.getName(), field.getType(), getPropertyAnnotation(Optional.class, entityType, field.getName()) != null));
        }
        
        return composeExpression(keyMembersWithOptionality, getKeyMemberSeparator(entityType));
    }

    private static String getKeyMemberConcatenationExpression(final String keyMemberName, final Class<?> keyMemberType) {
        if (PropertyDescriptor.class != keyMemberType && isEntityType(keyMemberType)) {
            return keyMemberName + "." + KEY;
        } else {
            return keyMemberName;
        }
    }

    private static ExpressionModel composeExpression(final List<T3<String, Class<?>, Boolean>> original, final String separator) {
        ExpressionModel currExp = null;
        Boolean currExpIsOptional = null;

        for (final T3<String, Class<?>, Boolean>  originalField : original) {
            currExp = composeTwo(new Pair<ExpressionModel, Boolean>(currExp, currExpIsOptional), originalField, separator);
            currExpIsOptional = currExpIsOptional != null ? currExpIsOptional && originalField._3 : originalField._3;
        }

        return currExp;
    }

    private static ExpressionModel concatTwo(final ExpressionModel first, final String secondPropName, final String separator) {
        return expr().concat().expr(first).with().val(separator).with().prop(secondPropName).end().model();
    }

    private static ExpressionModel composeTwo(final Pair<ExpressionModel, Boolean> first, final T3<String, Class<?>, Boolean>  second, final String separator) {
        final ExpressionModel firstModel = first.getKey();
        final Boolean firstIsOptional = first.getValue();

        final String secondPropName = getKeyMemberConcatenationExpression(second._1, second._2);
        final boolean secondPropIsOptional = second._3;

        if (first.getKey() == null) {
            return expr().prop(secondPropName).model();
        } else {
            if (firstIsOptional) {
                if (secondPropIsOptional) {
                    return expr().caseWhen().expr(firstModel).isNotNull().and().prop(secondPropName).isNotNull().then().expr(concatTwo(firstModel, secondPropName, separator)). //
                    when().expr(firstModel).isNotNull().and().prop(secondPropName).isNull().then().expr(firstModel). //
                    when().prop(secondPropName).isNotNull().then().prop(secondPropName). //
                    otherwise().val(null).endAsStr(256).model();
                } else {
                    return expr().caseWhen().expr(firstModel).isNotNull().then().expr(concatTwo(firstModel, secondPropName, separator)). //
                    otherwise().prop(secondPropName).endAsStr(256).model();
                }
            } else {
                if (secondPropIsOptional) {
                    return expr().caseWhen().prop(secondPropName).isNotNull().then().expr(concatTwo(firstModel, secondPropName, separator)). //
                    otherwise().expr(firstModel).endAsStr(256).model();
                } else {
                    return concatTwo(firstModel, secondPropName, separator);
                }
            }
        }
    }

    public static ExpressionModel extractExpressionModelFromCalculatedProperty(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
        final Calculated calcAnnotation = getAnnotation(calculatedPropfield, Calculated.class);
        if (isNotEmpty(calcAnnotation.value())) {
            return createExpressionText2ModelConverter(entityType, calcAnnotation).convert().getModel();
        } else {
            try {
                final Field exprField = getFieldByName(entityType, calculatedPropfield.getName() + "_");
                exprField.setAccessible(true);
                return (ExpressionModel) exprField.get(null);
            } catch (final Exception e) {
                throw new EqlException(format("Can't extract hard-coded expression model for prop [%s] due to: [%s]", calculatedPropfield.getName(), e.getMessage()));
            }
        }
    }

    private static ExpressionText2ModelConverter createExpressionText2ModelConverter(final Class<? extends AbstractEntity<?>> entityType, final Calculated calcAnnotation)
            throws Exception {
        if (isContextual(calcAnnotation)) {
            return new ExpressionText2ModelConverter(getRootType(calcAnnotation), calcAnnotation.contextPath(), calcAnnotation.value());
        } else {
            return new ExpressionText2ModelConverter(entityType, calcAnnotation.value());
        }
    }

    private static Class<? extends AbstractEntity<?>> getRootType(final Calculated calcAnnotation) throws ClassNotFoundException {
        return (Class<? extends AbstractEntity<?>>) ClassLoader.getSystemClassLoader().loadClass(calcAnnotation.rootTypeName());
    }
    
    public static <ET extends AbstractEntity<?>> List<EntityResultQueryModel<ET>> produceUnionEntityModels(final Class<ET> entityType) {
        final List<EntityResultQueryModel<ET>> result = new ArrayList<>();
        if (!isUnionEntityType(entityType)) {
            return result;
        }

        final List<Field> unionProps = unionProperties((Class<? extends AbstractUnionEntity>) entityType);
        for (final Field currProp : unionProps) {
            result.add(generateModelForUnionEntityProperty(unionProps, currProp).modelAsEntity(entityType));
        }
        return result;
    }
    
    private static <PT extends AbstractEntity<?>> ISubsequentCompletedAndYielded<PT> generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp) {
        final IFromAlias<PT> startWith = select((Class<PT>) currProp.getType());
        final Field firstUnionProp = unionProps.get(0);
        final ISubsequentCompletedAndYielded<PT> initialModel = firstUnionProp.equals(currProp) ? startWith.yield().prop(ID).as(firstUnionProp.getName()) : startWith.yield().val(null).as(firstUnionProp.getName()); 
        return unionProps.stream().skip(1).reduce(initialModel, (m, f) -> f.equals(currProp) ? m.yield().prop(ID).as(f.getName()) : m.yield().val(null).as(f.getName()), (m1, m2) -> {throw new UnsupportedOperationException("Combining is not applicable here.");});
    }
}