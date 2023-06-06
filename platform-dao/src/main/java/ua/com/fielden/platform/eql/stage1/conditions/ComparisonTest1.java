package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class ComparisonTest1 implements ICondition1<ComparisonTest2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand;
    private final ComparisonOperator operator;

    public ComparisonTest1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final ComparisonOperator operator, final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public ComparisonTest2 transform(final TransformationContext1 context) {
        return new ComparisonTest2(leftOperand.transform(context), operator, rightOperand.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        result.addAll(leftOperand.collectEntityTypes());
        result.addAll(rightOperand.collectEntityTypes());
        return result;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + operator.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ComparisonTest1)) {
            return false;
        }

        final ComparisonTest1 other = (ComparisonTest1) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
    }
}