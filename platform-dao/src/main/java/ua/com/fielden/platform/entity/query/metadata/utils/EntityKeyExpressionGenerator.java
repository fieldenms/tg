package ua.com.fielden.platform.entity.query.metadata.utils;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.utils.EntityKeyExpressionGenerator.TypeInfo.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.utils.EntityKeyExpressionGenerator.TypeInfo.NON_STRING;
import static ua.com.fielden.platform.entity.query.metadata.utils.EntityKeyExpressionGenerator.TypeInfo.STRING;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class EntityKeyExpressionGenerator {
    public static final String EMPTY_STRING = "";
    
    /** Private default constructor to prevent instantiation. */
    private EntityKeyExpressionGenerator() {
    }
    
    public static ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType) {
        final List<KeyMemberInfo> keyMembersInfo = new ArrayList<>();
        for (Field keyMemberField : getKeyMembers(entityType)) {
            keyMembersInfo.add(getKeyMemberInfo(entityType, keyMemberField));
        }
        return getVirtualKeyPropForEntityWithCompositeKey(getKeyMemberSeparator(entityType), keyMembersInfo);
    }
    
    private static KeyMemberInfo getKeyMemberInfo(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType, final Field keyMemberField) {
        final boolean optional = getPropertyAnnotation(Optional.class, entityType, keyMemberField.getName()) != null;
        final TypeInfo typeInfo = Integer.class.equals(keyMemberField.getType()) ? NON_STRING : 
            (!PropertyDescriptor.class.equals(keyMemberField.getType()) && isEntityType(keyMemberField.getType()) ? ENTITY : STRING);
        
        return new KeyMemberInfo(keyMemberField.getName(), typeInfo, optional);
    }
    
    protected static ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        final Iterator<KeyMemberInfo> kmIter = keyMembers.iterator();
        final KeyMemberInfo firstKeyMember = kmIter.next();
        
        ExpressionModel firstMemberExpr = processFirstKeyMember(firstKeyMember.name, firstKeyMember.typeInfo);
        if (!kmIter.hasNext()) {
            return firstMemberExpr;
        } else {
            IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> concatStart = expr().concat().expr(firstMemberExpr);
            
            while (kmIter.hasNext()) {
                final KeyMemberInfo nextKeyMember = kmIter.next();
                concatStart = nextKeyMember.optional ? 
                        concatStart.with().expr(processOptionalKeyMember(nextKeyMember.name, nextKeyMember.typeInfo, keyMemberSeparator))
                        :
                            concatStart.with().val(keyMemberSeparator).with().expr(getKeyMemberConcatenationPropName(nextKeyMember.name, nextKeyMember.typeInfo));
            }
            
            return concatStart.end().model();
        }
    }
    
    private static ExpressionModel getKeyMemberConcatenationPropName(final String keyMemberName, final TypeInfo keyMemberType) {
        return expr().prop(keyMemberType == ENTITY ? keyMemberName + "." + KEY : keyMemberName).model();
    }

    private static ExpressionModel processFirstKeyMember(final String keyMemberName, final TypeInfo keyMemberType) {
        return keyMemberType == NON_STRING ? expr().concat().prop(keyMemberName).with().val(EMPTY_STRING).end().model()
                : getKeyMemberConcatenationPropName(keyMemberName, keyMemberType);
    }
    
    private static ExpressionModel processOptionalKeyMember(final String keyMemberName, final TypeInfo keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().val(separator).with().expr(getKeyMemberConcatenationPropName(keyMemberName, keyMemberType)).end().otherwise().val(EMPTY_STRING).end()/*.endAsStr(256)*/.model();
    }


    public static class KeyMemberInfo {
        final String name;
        final TypeInfo typeInfo;
        final boolean optional;
        
        
        public KeyMemberInfo(final String name, final TypeInfo  typeInfo, final boolean optional) {
            this.name = name;
            this.typeInfo = typeInfo;
            this.optional = optional;
        }
    }
    
    public static enum TypeInfo {
        ENTITY,
        STRING,
        NON_STRING
    }
}
