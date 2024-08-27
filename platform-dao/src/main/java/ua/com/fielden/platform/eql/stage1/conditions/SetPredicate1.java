package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.SetPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

/**
 * A predicate for SQL's IN / NOT IN statement.
 *
 * @author TG Team
 */
public record SetPredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> leftOperand,
                             boolean negated,
                             ISetOperand1<? extends ISetOperand2<?>> rightOperand)
        implements ICondition1<SetPredicate2>
{

    @Override
    public SetPredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new SetPredicate2(leftOperand.transform(context), negated, rightOperand.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, leftOperand.collectEntityTypes(), rightOperand.collectEntityTypes());
    }

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
                .add("left", leftOperand)
                .add("right", rightOperand)
                .add("negated", negated)
                .$();
    }

}
