package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.LikePredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

/**
 * A predicate for SQL's LIKE / NOT LIKE statement.
 * Additionally holds information about case sensitivity.
 *
 * @author TG Team
 */
public record LikePredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> leftOperand,
                              ISingleOperand1<? extends ISingleOperand2<?>> rightOperand,
                              LikeOptions options)
        implements ICondition1<LikePredicate2>
{


    @Override
    public LikePredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new LikePredicate2(leftOperand.transform(context), rightOperand.transform(context), options);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, leftOperand.collectEntityTypes(), rightOperand.collectEntityTypes());
    }

}
