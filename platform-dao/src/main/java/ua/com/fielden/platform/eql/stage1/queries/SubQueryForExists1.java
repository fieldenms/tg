package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.queries.SubQueryForExists2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

import static ua.com.fielden.platform.eql.stage2.sundries.Yields2.nullYields;

/**
 * A structure used for representing queries in the EXISTS statement.
 * <p>
 * Its specificity lies in the fact that queries without explicit yields will yield NULL, which is the most suitable value for EXISTS as the actual values are unimportant.
 * What is important is the presence of records in the result.
 *
 */
public class SubQueryForExists1 extends AbstractQuery1 implements ITransformableFromStage1To2<SubQueryForExists2> {

    public SubQueryForExists1(final QueryComponents1 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public SubQueryForExists2 transform(final TransformationContextFromStage1To2 context) {
        final var queryComponents2 = maybeJoinRoot.map(joinRoot -> transformQueryComponents(context, joinRoot))
                .orElseGet(() -> transformSourceless(context));
        return new SubQueryForExists2(queryComponents2);
    }

    @Override
    protected EnhancedYields enhanceYields(final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        return new EnhancedYields(yields.isEmpty() ? nullYields : yields);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SubQueryForExists1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SubQueryForExists1;
    }
}
