package ua.com.fielden.platform.eql.stage1.conditions;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class NullTest1 implements ICondition1<Conditions2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    private final boolean negated;

    public NullTest1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public Conditions2 transform(final PropsResolutionContext context) {
        final ISingleOperand2<?> transformedOperand = operand.transform(context);
        if (transformedOperand instanceof EntProp2 && isUnionEntityType(((EntProp2) transformedOperand).type)) {
            final EntProp2 prop = (EntProp2) transformedOperand;
            final UnionTypePropInfo<?> lastResolutionItem = (UnionTypePropInfo<?>)prop.getPath().get(prop.getPath().size() - 1);
            final List<ICondition2<?>> nullTests = new ArrayList<>();
            
            for (final AbstractPropInfo<?> el: lastResolutionItem.propEntityInfo.getProps().values()) {
                if (!el.hasExpression()) {
                    final List<AbstractPropInfo<?>> subPropPath = new ArrayList<>(prop.getPath());
                    subPropPath.add(el);
                    nullTests.add(new NullTest2(new EntProp2(prop.source, subPropPath), negated));
                }
            }
            
            if (negated) {
                final List<List<? extends ICondition2<?>>> negatedNullTests = new ArrayList<>();
                for (final ICondition2<?> nt : nullTests) {
                    negatedNullTests.add(asList(nt));
                }
                return new Conditions2(false, negatedNullTests);
            } else {
                return new Conditions2(false, asList(nullTests));
            }
        }
        
        return new Conditions2(false, asList(asList(new NullTest2(transformedOperand, negated))));
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