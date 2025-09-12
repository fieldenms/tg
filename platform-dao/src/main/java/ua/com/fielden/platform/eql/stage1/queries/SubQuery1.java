package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.eql.stage1.sundries.Yield1.ABSENT_ALIAS;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/**
 * Represents a query used in the role of an operand.
 * <p>
 * Examples:
 * {@snippet :
 * prop("id").eq().allOfModels(subQuery1, subQuery2)
 * val(1).in().model(subQuery)
 * where().model(subQuery).isNotNull();
 * }
 * <p>
 * The only exception is the EXISTS statement, which is represented by {@link SubQueryForExists1}.
 * <p>
 * The specificity of this structure pertains to the processing of yields.
 * In case of no explicit yields, it is expected that the query result is an entity (i.e., contains ID), which can be auto-yielded.
 * Otherwise, an exception is thrown.
 *
 * @author TG Team
 */
public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public static final String ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID = "Auto-yield is not possible when the main source of the query doesn't contain property [id].";

    public SubQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public SubQuery2 transform(final TransformationContextFromStage1To2 context) {
        return maybeJoinRoot.map(joinRoot -> {
            final var queryComponents2 = transformQueryComponents(context, joinRoot);
            return new SubQuery2(queryComponents2, enhance(resultType, queryComponents2.yields()), isRefetchOnlyQuery());
        }).orElseGet(() -> {
            final var queryComponents2 = transformSourceless(context);
            return new SubQuery2(queryComponents2, enhance(null, queryComponents2.yields()), false);
        });
    }

    private static PropType enhance(final Class<?> resultType, final Yields2 yields) {
        return resultType == null
               ? yields.getYields().iterator().next().operand().type() // the case of modelAsPrimitive() no ResultType provided
               : propType(resultType, H_ENTITY); // the case of modelAsEntity(..)
    }

    private boolean isRefetchOnlyQuery() {
        return whereConditions.isIdEqualsExtId();
    }

    @Override
    protected EnhancedYields enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        final Yields2 result;

        if (!yields.isEmpty()) {
            result = yields;
        } else if (mainSource.querySourceInfo().getProps().containsKey(ID)) {
            result = new Yields2(listOf(new Yield2(new Prop2(mainSource, listOf(mainSource.querySourceInfo().getProps().get(ID))), ABSENT_ALIAS, false)));
        } else {
            throw new EqlStage1ProcessingException(ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID);
        }

        return new EnhancedYields(result);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SubQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SubQuery1;
    }
}
