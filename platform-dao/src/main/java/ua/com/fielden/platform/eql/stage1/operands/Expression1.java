package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.utils.StreamUtils.concat;

public record Expression1 (ISingleOperand1<? extends ISingleOperand2<?>> first,
                           List<CompoundSingleOperand1> items)
        implements ISingleOperand1<Expression2>, ToString.IFormattable
{

    @Override
    public Expression2 transform(final TransformationContextFromStage1To2 context) {
        return new Expression2(first.transform(context),
                               items.stream().map(el -> el.transform(context)).collect(toImmutableList()));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(
                items.stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream),
                first.collectEntityTypes().stream())
                .collect(toSet());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("first", first)
                .addIfNotEmpty("rest", items)
                .$();
    }


}
