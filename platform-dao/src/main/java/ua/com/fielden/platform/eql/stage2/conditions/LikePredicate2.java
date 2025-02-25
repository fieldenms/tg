package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.LikePredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public record LikePredicate2 (ISingleOperand2<? extends ISingleOperand3> matchOperand,
                              ISingleOperand2<? extends ISingleOperand3> patternOperand,
                              LikeOptions options)
        implements ICondition2<LikePredicate3>, ToString.IFormattable
{

    @Override
    public boolean ignore() {
        return matchOperand.ignore() || patternOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<LikePredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> matchOperandTr = matchOperand.transform(
                context);
        final TransformationResultFromStage2To3<? extends ISingleOperand3> patternOperandTr = patternOperand.transform(
                matchOperandTr.updatedContext);
        return new TransformationResultFromStage2To3<>(
                new LikePredicate3(matchOperandTr.item, patternOperandTr.item, options), patternOperandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(matchOperand.collectProps());
        result.addAll(patternOperand.collectProps());
        return result;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, matchOperand.collectEntityTypes(), patternOperand.collectEntityTypes());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .addIf("options", options, opts -> opts != LikeOptions.DEFAULT_OPTIONS)
                .add("match", matchOperand)
                .add("pattern", patternOperand)
                .$();
    }

}
