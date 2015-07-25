package ua.com.fielden.platform.dao.eql;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class DomainMetadataExpressionsGenerator {

    ExpressionModel generateUnionEntityPropertyExpression(final Class<? extends AbstractUnionEntity> entityType, final String commonPropName) {
        final List<Field> props = AbstractUnionEntity.unionProperties(entityType);
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

    ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType) {
        final List<Field> keyMembers = Finder.getKeyMembers(entityType);
        final Iterator<Field> iterator = keyMembers.iterator();
        IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> expressionModelInProgress = expr().concat().prop(getKeyMemberConcatenationExpression(iterator.next()));
        for (; iterator.hasNext();) {
            expressionModelInProgress = expressionModelInProgress.with().val(Reflector.getKeyMemberSeparator(entityType));
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

    private PrimitiveResultQueryModel getReferenceCountForSingleProp(final Class<? extends AbstractEntity<?>> entityType, final String propName) {
        return select(entityType).where().prop(propName).eq().extProp("id").yield().countAll().modelAsPrimitive();
    }

    ExpressionModel getReferencesCountPropForEntity(final Set<Pair<Class<? extends AbstractEntity<?>>, String>> references) {
        if (references.size() == 0) {
            return expr().val(0).model();
        }

        final Iterator<Pair<Class<? extends AbstractEntity<?>>, String>> iterator = references.iterator();
        final Pair<Class<? extends AbstractEntity<?>>, String> firstEntry = iterator.next();
        IStandAloneExprOperationAndClose expressionModelInProgress = expr().model(getReferenceCountForSingleProp(firstEntry.getKey(), firstEntry.getValue()));
        for (; iterator.hasNext();) {
            final Pair<Class<? extends AbstractEntity<?>>, String> entry = iterator.next();
            expressionModelInProgress = expressionModelInProgress.add().model(getReferenceCountForSingleProp(entry.getKey(), entry.getValue()));
        }
        return expressionModelInProgress.model();
    }
}