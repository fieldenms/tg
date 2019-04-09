package ua.com.fielden.platform.entity.query.metadata;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.NON_STRING;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.STRING;
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
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
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
    
    public static KeyMemberInfo getKeyMemberInfo(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType, final Field keyMemberField) {
        final boolean optional = getPropertyAnnotation(Optional.class, entityType, keyMemberField.getName()) != null;
        final TypeInfo typeInfo = Integer.class.equals(keyMemberField.getType()) ? NON_STRING : 
            (!PropertyDescriptor.class.equals(keyMemberField.getType()) && isEntityType(keyMemberField.getType()) ? ENTITY : STRING);
        
        return new KeyMemberInfo(keyMemberField.getName(), typeInfo, optional);
    }
    
    protected static ExpressionModel getVirtualKeyPropForEntityWithCompositeKey(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        if (keyMembers.size() == 1) {
            return processSingleKeyMember(keyMembers.get(0).name, keyMembers.get(0).typeInfo);
        } else {
            final Iterator<ExpressionModel> kmIter = getVirtualKeyPropForEntityWithCompositeKeyList(keyMemberSeparator, keyMembers).iterator();
            final ExpressionModel firstMemberExpr = kmIter.next();

            IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> concatStart = expr().concat().expr(firstMemberExpr);
            
            while (kmIter.hasNext()) {
                final ExpressionModel nextKeyMember = kmIter.next();
                concatStart = concatStart.with().expr(nextKeyMember);
            }
            
            return concatStart.end().model();
        }
    }
    
    protected static List<ExpressionModel> getVirtualKeyPropForEntityWithCompositeKeyList(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        boolean foundFirstNonOptional = false;
        final List<ExpressionModel> result = new ArrayList<>();
        for (KeyMemberInfo keyMemberInfo : keyMembers) {
            if (keyMemberInfo.optional) {
                result.add(foundFirstNonOptional ? 
                        processOptionalKeyMemberAfter(keyMemberInfo.name, keyMemberInfo.typeInfo, keyMemberSeparator) 
                        :
                    processOptionalKeyMemberBefore(keyMemberInfo.name, keyMemberInfo.typeInfo, keyMemberSeparator));
            } else if (foundFirstNonOptional){
                result.add(expr().val(keyMemberSeparator).model());
                result.add(getKeyMemberConcatenationPropName(keyMemberInfo.name, keyMemberInfo.typeInfo));
            } else {
                foundFirstNonOptional = true;
                result.add(getKeyMemberConcatenationPropName(keyMemberInfo.name, keyMemberInfo.typeInfo));
            }
        }
        
        if (!foundFirstNonOptional) {
            throw new EqlException("Composite key should consist of at least one not-optional member.");
        }
        
        return result;
    }
    
    private static ExpressionModel getKeyMemberConcatenationPropName(final String keyMemberName, final TypeInfo keyMemberType) {
        return expr().prop(keyMemberType == ENTITY ? keyMemberName + "." + KEY : keyMemberName).model();
    }

    private static ExpressionModel processSingleKeyMember(final String keyMemberName, final TypeInfo keyMemberType) {
        return keyMemberType == NON_STRING ? expr().concat().prop(keyMemberName).with().val(EMPTY_STRING).end().model()
                : getKeyMemberConcatenationPropName(keyMemberName, keyMemberType);
    }

    private static ExpressionModel processOptionalKeyMemberAfter(final String keyMemberName, final TypeInfo keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().val(separator).with().expr(getKeyMemberConcatenationPropName(keyMemberName, keyMemberType)).end().otherwise().val(EMPTY_STRING).end()/*.endAsStr(256)*/.model();
    }

    private static ExpressionModel processOptionalKeyMemberBefore(final String keyMemberName, final TypeInfo keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().expr(getKeyMemberConcatenationPropName(keyMemberName, keyMemberType)).with().val(separator).end().otherwise().val(EMPTY_STRING).end()/*.endAsStr(256)*/.model();
    }

    
    public static class KeyMemberInfo {
        public final String name;
        public final TypeInfo typeInfo;
        public final boolean optional;
        
        
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