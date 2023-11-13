package ua.com.fielden.platform.eql.stage1.queries;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.H_ENTITY;
import static ua.com.fielden.platform.eql.stage0.YieldBuilder.ABSENT_ALIAS;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * A structure used for representing queries in WHERE/ON conditions, yielding, grouping, and ordering.
 * The only exception is the EXISTS statement, which is represented by {@link SubQueryForExists1}.
 * <p>
 * The specificity of this structure pertains to the processing of yields.
 * In case of no explicit yields, it is expected that query result is entity (i.e., contains ID), which can be auto-yielded.
 * Otherwise the exception is thrown.
 *
 */
public class SubQuery1 extends AbstractQuery1 implements ISingleOperand1<SubQuery2> {

    public static final String ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID = "Auto-yield is not possible when the main source of the query doesn't contain ID property.";

    public SubQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public SubQuery2 transform(final TransformationContextFromStage1To2 context) {

        if (joinRoot == null) {
            final QueryComponents2 qb = transformSourceless(context);
            return new SubQuery2(qb, enhance(null, qb.yields()), false);
        }

        final QueryComponents2 queryComponents2 = transformQueryComponents(context);
        return new SubQuery2(queryComponents2, enhance(resultType, queryComponents2.yields()), isRefetchOnlyQuery());
    }

    private static PropType enhance(final Class<?> resultType, final Yields2 yields) {
        return resultType == null
               ? yields.getYields().iterator().next().operand.type() // the case of modelAsPrimitive() no ResultType provided
               : new PropType(resultType, H_ENTITY); // the case of modelAsEntity(..)
    }

    private boolean isRefetchOnlyQuery() {
        return whereConditions.isIdEqualsExtId();
    }

    @Override
    protected Yields2 enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (!yields.getYields().isEmpty()) {
            return yields;
        } else if (mainSource.querySourceInfo().getProps().containsKey(ID)) {
            return new Yields2(listOf(new Yield2(new Prop2(mainSource, listOf(mainSource.querySourceInfo().getProps().get(ID))), ABSENT_ALIAS, false)));
        } else {
            throw new EqlStage1ProcessingException(ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID);
        }
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