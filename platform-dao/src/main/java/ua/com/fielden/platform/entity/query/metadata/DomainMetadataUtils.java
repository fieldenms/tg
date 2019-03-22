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
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;

public class DomainMetadataUtils {

    private static final String EMPTY_STRING = "";

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
        final String keyMemberSeparator = getKeyMemberSeparator(entityType);
        final Iterator<Field> kmIter = getKeyMembers(entityType).iterator();
        final Field firstKeyMember = kmIter.next();
        
        if (!kmIter.hasNext()) {
            return processFirstKeyMember(firstKeyMember.getName(), firstKeyMember.getType(), keyMemberSeparator); 
        } else {
            IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> concatStart = expr().concat().expr(processFirstKeyMember(firstKeyMember.getName(), firstKeyMember.getType(), keyMemberSeparator));
            
            while (kmIter.hasNext()) {
                final Field nextKeyMember = kmIter.next();
                concatStart = getPropertyAnnotation(Optional.class, entityType, nextKeyMember.getName()) != null ? 
                        concatStart.with().expr(processOptionalKeyMember(nextKeyMember.getName(), nextKeyMember.getType(), keyMemberSeparator))
                        :
                            concatStart.with().val(keyMemberSeparator).with().expr(getKeyMemberConcatenationPropName(nextKeyMember.getName(), nextKeyMember.getType()));
            }
            
            return concatStart.end().model();
        }
    }

    private static ExpressionModel getKeyMemberConcatenationPropName(final String keyMemberName, final Class<?> keyMemberType) {
        return expr().prop(PropertyDescriptor.class != keyMemberType && isEntityType(keyMemberType) ? keyMemberName + "." + KEY : keyMemberName).model();
    }

    private static ExpressionModel processFirstKeyMember(final String keyMemberName, final Class<?> keyMemberType, final String separator) {
        return Integer.class.equals(keyMemberType) ? expr().concat().prop(keyMemberName).with().val(EMPTY_STRING).end().model() 
                : getKeyMemberConcatenationPropName(keyMemberName, keyMemberType);
    }
    
    private static ExpressionModel processOptionalKeyMember(final String keyMemberName, final Class<?> keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().val(separator).with().expr(getKeyMemberConcatenationPropName(keyMemberName, keyMemberType)).end().otherwise().val(EMPTY_STRING).end()/*.endAsStr(256)*/.model();
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
