package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.CompoundSingleOperand3;
import ua.com.fielden.platform.eql.stage3.elements.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Expression2 implements ISingleOperand2<Expression3> {

    public final ISingleOperand2<? extends ISingleOperand3> first;
    private final List<CompoundSingleOperand2> items;

    public Expression2(final ISingleOperand2<? extends ISingleOperand3> first, final List<CompoundSingleOperand2> items) {
        this.first = first;
        this.items = items;
    }
    
    @Override
    public TransformationResult<Expression3> transform(final TransformationContext context) {
        final List<CompoundSingleOperand3> transformed = new ArrayList<>();
        final TransformationResult<? extends ISingleOperand3> firstTr = first.transform(context);
        TransformationContext currentContext = firstTr.updatedContext;
        for (final CompoundSingleOperand2 item : items) {
            final TransformationResult<? extends ISingleOperand3> itemTr = item.operand.transform(currentContext);
            transformed.add(new CompoundSingleOperand3(itemTr.item, item.operator));
            currentContext = itemTr.updatedContext;
        }
        return new TransformationResult<Expression3>(new Expression3(firstTr.item, transformed), currentContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        result.addAll(first.collectProps());
        for (final CompoundSingleOperand2 item : items) {
            result.addAll(item.operand.collectProps());
        }
        
        return result;
    }
    
    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class<BigDecimal> type() {
        // TODO EQL Need to provide proper implementation.
        return BigDecimal.class;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + items.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Expression2)) {
            return false;
        }
        
        final Expression2 other = (Expression2) obj;
        
        return Objects.equals(first, other.first) &&
                Objects.equals(items, other.items);
    }
}