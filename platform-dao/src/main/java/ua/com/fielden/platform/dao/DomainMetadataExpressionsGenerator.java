package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

public class DomainMetadataExpressionsGenerator {
//    ExpressionModel generateUnionEntityIdExpression(final Class<? extends AbstractUnionEntity> entityType) {
//	final List<Field> props = AbstractUnionEntity.unionProperties(entityType);
//
//	final Iterator<Field> iterator = props.iterator();
//	IStandAloneExprOperationAndClose expressionModelInProgress = expr().ifNull().prop(iterator.next().getName()).then().val(0);
//
//	for (; iterator.hasNext();) {
//	    expressionModelInProgress = expressionModelInProgress.add().ifNull().prop(iterator.next().getName()).then().val(0);
//	}
//
//	return expressionModelInProgress.model();
//    }

    ExpressionModel generateUnionEntityIdExpression(final Class<? extends AbstractUnionEntity> entityType) {
	final List<Field> props = AbstractUnionEntity.unionProperties(entityType);

	final Iterator<Field> iterator = props.iterator();
	final String firstUnionPropName = iterator.next().getName();
	ICaseWhenFunctionWhen<IStandAloneExprOperationAndClose,AbstractEntity<?>> expressionModelInProgress = expr().caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName);

	for (; iterator.hasNext();) {
	    final String unionPropName = iterator.next().getName();
	    expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName);
	}

	return expressionModelInProgress.otherwise().val(null).end().model();
    }


    ExpressionModel generateUnionEntityKeyExpression(final Class<? extends AbstractUnionEntity> entityType) {
	final List<Field> props = AbstractUnionEntity.unionProperties(entityType);

	final Iterator<Field> iterator = props.iterator();
	final String firstUnionPropName = iterator.next().getName();
	ICaseWhenFunctionWhen<IStandAloneExprOperationAndClose,AbstractEntity<?>> expressionModelInProgress = expr().caseWhen().prop(firstUnionPropName).isNotNull().then().prop(firstUnionPropName + ".key");

	for (; iterator.hasNext();) {
	    final String unionPropName = iterator.next().getName();
	    expressionModelInProgress = expressionModelInProgress.when().prop(unionPropName).isNotNull().then().prop(unionPropName + ".key");
	}

	return expressionModelInProgress.otherwise().val(null).end().model();
    }

    ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final Class<? extends AbstractEntity<?>> entityType) {
	final List<Field> keyMembers = Finder.getKeyMembers(entityType);
	final Iterator<Field> iterator = keyMembers.iterator();
	IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> expressionModelInProgress = expr().concat().prop(getKeyMemberConcatenationExpression(iterator.next()));
	for (; iterator.hasNext();) {
	    expressionModelInProgress = expressionModelInProgress.with().val(DynamicEntityKey.KEY_MEMBERS_SEPARATOR);
	    expressionModelInProgress = expressionModelInProgress.with().prop(getKeyMemberConcatenationExpression(iterator.next()));
	}
	return expressionModelInProgress.end().model();
    }

    private String getKeyMemberConcatenationExpression(final Field keyMember) {
	if (EntityUtils.isEntityType(keyMember.getType())) {
	    return keyMember.getName() + ".key";
	} else {
	    return keyMember.getName();
	}
    }

    ExpressionModel extractExpressionModelFromCalculatedProperty(final Class<? extends AbstractEntity<?>> entityType, final Field calculatedPropfield) throws Exception {
	final Calculated calcAnnotation = calculatedPropfield.getAnnotation(Calculated.class);
	if (!"".equals(calcAnnotation.value())) {
	    return createExpressionText2ModelConverter(entityType, calcAnnotation).convert().getModel();
	} else {
	    try {
		final Field exprField = entityType.getDeclaredField(calculatedPropfield.getName() + "_");
		exprField.setAccessible(true);
		return (ExpressionModel) exprField.get(null);
	    } catch (final Exception e) {
		throw new IllegalStateException("Hard-coded expression model for prop [" + calculatedPropfield.getName() + "] is missing!");
	    }
	}
    }

    private ExpressionText2ModelConverter createExpressionText2ModelConverter(final Class<? extends AbstractEntity<?>> entityType, final Calculated calcAnnotation)
	    throws Exception {
	if (AnnotationReflector.isContextual(calcAnnotation)) {
	    return new ExpressionText2ModelConverter(getRootType(calcAnnotation), calcAnnotation.contextPath(), calcAnnotation.value());
	} else {
	    return new ExpressionText2ModelConverter(entityType, calcAnnotation.value());
	}
    }

    private Class<? extends AbstractEntity<?>> getRootType(final Calculated calcAnnotation) throws ClassNotFoundException {
	return (Class<? extends AbstractEntity<?>>) ClassLoader.getSystemClassLoader().loadClass(calcAnnotation.rootTypeName());
    }
}
