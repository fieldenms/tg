package ua.com.fielden.platform.entity.query.metadata;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.NON_STRING;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.STRING;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class CompositeKeyEqlExpressionGenerator {
    public static final String EMPTY_STRING = "";

    /** Private default constructor to prevent instantiation. */
    private CompositeKeyEqlExpressionGenerator() {
    }

    public static ExpressionModel generateCompositeKeyEqlExpression(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType) {
        final List<KeyMemberInfo> keyMembersInfo = getKeyMembers(entityType).stream().map(keyMemberInfo -> getKeyMemberInfo(entityType, keyMemberInfo)).collect(toList()); 
        return generateCompositeKeyEqlExpression(getKeyMemberSeparator(entityType), keyMembersInfo);
    }

    protected static ExpressionModel generateCompositeKeyEqlExpression(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        boolean hasAtLeastOneNotOptionalMember = false;
        for (KeyMemberInfo keyMemberInfo : keyMembers) {
            hasAtLeastOneNotOptionalMember = hasAtLeastOneNotOptionalMember || !keyMemberInfo.optional;
        }

        if (keyMembers.size() == 1) {
            return generateExpressionForCompositeKeyWithSingleMember(keyMembers.get(0));
        } else if (hasAtLeastOneNotOptionalMember) {
            return generateExpressionForCompositeKeyWithAtLeastOneNotOptionalMember(keyMemberSeparator, keyMembers);
        } else {
            return generateExpressionForCompositeKeyWithOnlyOptionalMembers(keyMemberSeparator, keyMembers);
        }
    }
    
    private static ExpressionModel generateExpressionForCompositeKeyWithSingleMember(final KeyMemberInfo keyMember) {
        final ExpressionModel resultExpression = keyMember.typeInfo == NON_STRING ? expr().concat().prop(keyMember.name).with().val(EMPTY_STRING).end().model()
                : adoptPropName(keyMember.name, keyMember.typeInfo);
        return !keyMember.optional ? resultExpression : expr().caseWhen().prop(keyMember.name).isNotNull().then().expr(resultExpression).end().model();
    }

    private static ExpressionModel generateExpressionForCompositeKeyWithOnlyOptionalMembers(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        ExpressionModel currExp = null;

        for (final KeyMemberInfo keyMember : keyMembers) {
            currExp = concatenateOptionalExpressionWithOptionalKeyMember(currExp, keyMember, keyMemberSeparator);
        }

        return currExp;
    }

    private static ExpressionModel generateExpressionForCompositeKeyWithAtLeastOneNotOptionalMember(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        return concatenate(getCompositeKeyExpressionsSequenceForConcatenation(keyMemberSeparator, keyMembers));
    }

    
    private static ExpressionModel concatenateOptionalExpressionWithOptionalKeyMember(final ExpressionModel firstExpr, final KeyMemberInfo second, final String separator) {
        final ExpressionModel secondExpr = adoptPropName(second.name, second.typeInfo);

        if (firstExpr == null) {
            return secondExpr;
        } else {
            return expr().caseWhen().condition(cond().expr(firstExpr).isNotNull().and().expr(secondExpr).isNotNull().model()).then().expr(concatenate(listOf(firstExpr, expr().val(separator).model(), secondExpr))). // 
                    when().condition(cond().expr(firstExpr).isNotNull().and().expr(secondExpr).isNull().model()).then().expr(firstExpr). //
                    when().expr(secondExpr).isNotNull().then().expr(secondExpr). //
                    otherwise().val(null).end().model();
        }
    }

    private static KeyMemberInfo getKeyMemberInfo(final Class<? extends AbstractEntity<DynamicEntityKey>> entityType, final Field keyMemberField) {
        final boolean optional = getPropertyAnnotation(Optional.class, entityType, keyMemberField.getName()) != null;
        final TypeInfo typeInfo = Integer.class.equals(keyMemberField.getType()) ? NON_STRING
                : (!PropertyDescriptor.class.equals(keyMemberField.getType()) && isEntityType(keyMemberField.getType()) ? ENTITY : STRING);

        return new KeyMemberInfo(keyMemberField.getName(), typeInfo, optional);
    }

    private static ExpressionModel concatenate(final List<ExpressionModel> expressions) {
        final Iterator<ExpressionModel> kmIter = expressions.iterator();
        final ExpressionModel firstMemberExpr = kmIter.next();

        IConcatFunctionWith<IStandAloneExprOperationAndClose, AbstractEntity<?>> concatInProgress = expr().concat().expr(firstMemberExpr);

        while (kmIter.hasNext()) {
            final ExpressionModel nextKeyMember = kmIter.next();
            concatInProgress = concatInProgress.with().expr(nextKeyMember);
        }

        return concatInProgress.end().model();
    }

    private static List<ExpressionModel> getCompositeKeyExpressionsSequenceForConcatenation(final String keyMemberSeparator, List<KeyMemberInfo> keyMembers) {
        boolean foundFirstNotOptional = false;
        final List<ExpressionModel> result = new ArrayList<>();
        for (KeyMemberInfo keyMemberInfo : keyMembers) {
            if (keyMemberInfo.optional) {
                result.add(foundFirstNotOptional ? 
                          concatenateDelimiterWithOptionalKeyMember(keyMemberInfo.name, keyMemberInfo.typeInfo, keyMemberSeparator)
                          : 
                          concatenateOptionalKeyMemberWithDelimiter(keyMemberInfo.name, keyMemberInfo.typeInfo, keyMemberSeparator));
            } else if (foundFirstNotOptional) {
                result.add(expr().val(keyMemberSeparator).model());
                result.add(adoptPropName(keyMemberInfo.name, keyMemberInfo.typeInfo));
            } else {
                foundFirstNotOptional = true;
                result.add(adoptPropName(keyMemberInfo.name, keyMemberInfo.typeInfo));
            }
        }

        return result;
    }

    private static ExpressionModel adoptPropName(final String keyMemberName, final TypeInfo keyMemberType) {
        return expr().prop(keyMemberType == ENTITY ? keyMemberName + "." + KEY : keyMemberName).model();
    }

    private static ExpressionModel concatenateDelimiterWithOptionalKeyMember(final String keyMemberName, final TypeInfo keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().val(separator).with().expr(adoptPropName(keyMemberName, keyMemberType)).end() //
                .otherwise().val(EMPTY_STRING).end().model();
    }

    private static ExpressionModel concatenateOptionalKeyMemberWithDelimiter(final String keyMemberName, final TypeInfo keyMemberType, final String separator) {
        return expr().caseWhen().prop(keyMemberName).isNotNull().then().concat().expr(adoptPropName(keyMemberName, keyMemberType)).with().val(separator).end() //
                .otherwise().val(EMPTY_STRING).end().model();
    }

    public static class KeyMemberInfo {
        public final String name;
        public final TypeInfo typeInfo;
        public final boolean optional;

        public KeyMemberInfo(final String name, final TypeInfo typeInfo, final boolean optional) {
            this.name = name;
            this.typeInfo = typeInfo;
            this.optional = optional;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + (optional ? 1231 : 1237);
            result = prime * result + ((typeInfo == null) ? 0 : typeInfo.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof KeyMemberInfo)) {
                return false;
            }

            KeyMemberInfo other = (KeyMemberInfo) obj;

            return Objects.equals(other.name, name) && Objects.equals(other.typeInfo, typeInfo) && optional == other.optional;
        }
    }

    public static enum TypeInfo {
        ENTITY,
        STRING,
        NON_STRING
    }
}