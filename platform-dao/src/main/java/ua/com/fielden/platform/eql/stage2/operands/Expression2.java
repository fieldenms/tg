package ua.com.fielden.platform.eql.stage2.operands;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.operands.CompoundSingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.utils.StreamUtils.concat;

public class Expression2 extends AbstractSingleOperand2 implements ISingleOperand2<Expression3> {

    public final ISingleOperand2<? extends ISingleOperand3> first;
    private final List<CompoundSingleOperand2> items;
    
    public Expression2(final ISingleOperand2<? extends ISingleOperand3> first, final List<CompoundSingleOperand2> items) {
        super(extractTypes(first, items));
        this.first = first;
        this.items = ImmutableList.copyOf(items);
    }
    
    @Override
    public TransformationResultFromStage2To3<Expression3> transform(final TransformationContextFromStage2To3 context) {
        final List<CompoundSingleOperand3> transformed = new ArrayList<>();
        final TransformationResultFromStage2To3<? extends ISingleOperand3> firstTr = first.transform(context);
        TransformationContextFromStage2To3 currentContext = firstTr.updatedContext;
        for (final CompoundSingleOperand2 item : items) {
            final TransformationResultFromStage2To3<? extends ISingleOperand3> itemTr = item.operand().transform(currentContext);
            transformed.add(new CompoundSingleOperand3(itemTr.item, item.operator()));
            currentContext = itemTr.updatedContext;
        }
        return new TransformationResultFromStage2To3<>(new Expression3(firstTr.item, transformed, type), currentContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(first.collectProps());
        for (final CompoundSingleOperand2 item : items) {
            result.addAll(item.operand().collectProps());
        }
        
        return result;
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(
                items.stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream),
                first.collectEntityTypes().stream())
                .collect(toSet());
    }
    
    @Override
    public boolean ignore() {
        return false;
    }
    
    @Override
    public boolean isNonnullableEntity() {
        return items.isEmpty() ? first.isNonnullableEntity() : false;
    }

    private static Set<PropType> extractTypes(final ISingleOperand2<? extends ISingleOperand3> first, final List<CompoundSingleOperand2> items) {
        final Set<PropType> types = new HashSet<>();
        types.add(first.type());
        for (final CompoundSingleOperand2 item : items) {
            types.add(item.operand().type());
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
        return this == obj
               || obj instanceof Expression2 that
                  && Objects.equals(first, that.first)
                  && Objects.equals(items, that.items);
    }

}
