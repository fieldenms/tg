package ua.com.fielden.platform.eql.stage2.operands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage3.operands.CompoundSingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class Expression2 extends AbstractSingleOperand2 implements ISingleOperand2<Expression3> {

    public final ISingleOperand2<? extends ISingleOperand3> first;
    private final List<CompoundSingleOperand2> items;
    

    public Expression2(final ISingleOperand2<? extends ISingleOperand3> first, final List<CompoundSingleOperand2> items) {
        super(extractTypes(first, items));
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
        return new TransformationResult<Expression3>(new Expression3(firstTr.item, transformed, type, hibType), currentContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
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

    private static Set<Class<?>> extractTypes(final ISingleOperand2<? extends ISingleOperand3> first, final List<CompoundSingleOperand2> items) {
        final Set<Class<?>> types = new HashSet<>();
        types.add(first.type());
        for (final CompoundSingleOperand2 item : items) {
            types.add(item.operand.type());
        }
        
        return types;
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