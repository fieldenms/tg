package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.LikePredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

/**
 * A predicate for SQL's LIKE / NOT LIKE statement.
 * Additionally holds information about case sensitivity.
 *
 * @author TG Team
 */
public record LikePredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> matchOperand,
                              ISingleOperand1<? extends ISingleOperand2<?>> patternOperand,
                              LikeOptions options)
        implements ICondition1<LikePredicate2>, ToString.IFormattable
{

    @Override
    public LikePredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new LikePredicate2(matchOperand.transform(context), patternOperand.transform(context), options);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, matchOperand.collectEntityTypes(), patternOperand.collectEntityTypes());
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
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
