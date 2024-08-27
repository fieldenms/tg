package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

/**
 * Represents all structural query parts (components) obtained as the result of stage 2 to stage 3 transformation.
 * This class is used as a convenience to keep all the query components together.
 *
 */
public record QueryComponents3(
        IJoinNode3 joinRoot,
        Conditions3 whereConditions,
        Yields3 yields,
        GroupBys3 groups,
        OrderBys3 orderings)
{

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
                .addIfNotNull("join", joinRoot)
                .addIfNot("where", whereConditions, Conditions3::isEmpty)
                .addIfNot("yields", yields, Yields3::isEmpty)
                .addIfNot("groups", groups, GroupBys3::isEmpty)
                .addIfNot("orderings", orderings, OrderBys3::isEmpty)
                .$();
    }

}
