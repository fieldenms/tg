package ua.com.fielden.platform.eql.stage1.conditions;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

public class NullTest1 implements ICondition1<ICondition2<?>> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    private final boolean negated;

    public NullTest1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public ICondition2<?> transform(final TransformationContext1 context) {
        final ISingleOperand2<?> transformedOperand = operand.transform(context);
        if (transformedOperand instanceof Prop2 && isUnionEntityType(((Prop2) transformedOperand).type.javaType())) {
            final Prop2 prop = (Prop2) transformedOperand;
            final QuerySourceItemForUnionType<?> lastResolutionItem = (QuerySourceItemForUnionType<?>) prop.getPath().get(prop.getPath().size() - 1);
            final List<ICondition2<?>> nullTests = new ArrayList<>();

            for (final AbstractQuerySourceItem<?> querySourceInfoItem : lastResolutionItem.getProps().values()) {
                if (!querySourceInfoItem.hasExpression()) {
                    final List<AbstractQuerySourceItem<?>> subPropPath = new ArrayList<>(prop.getPath());
                    subPropPath.add(querySourceInfoItem);
                    nullTests.add(new NullTest2(new Prop2(prop.source, subPropPath), negated));
                }
            }

            if (negated) {
                final List<List<? extends ICondition2<?>>> negatedNullTests = nullTests.stream().map(nullTest -> asList(nullTest)).collect(toList());
                return new Conditions2(false, negatedNullTests);
            } else {
                return new Conditions2(false, asList(nullTests));
            }
        }

        return new NullTest2(transformedOperand, negated);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullTest1)) {
            return false;
        }

        final NullTest1 other = (NullTest1) obj;

        return Objects.equals(negated, other.negated) && Objects.equals(operand, other.operand);
    }
}