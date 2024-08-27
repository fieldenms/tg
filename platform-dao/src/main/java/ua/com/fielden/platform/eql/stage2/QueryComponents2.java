package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.utils.ToString;

/**
 * Represents all structural query parts (components) obtained as the result of stage 1 to stage 2 transformation.
 * This class is used as a convenience to keep all the query components together.
 *
 * @param joinRoot -- if not null represents a FROM part of SQL query; otherwise it means that query yields are derived either from values or from props of outer query sources.
 */
public record QueryComponents2(
        IJoinNode2<? extends IJoinNode3> joinRoot,
        Conditions2 whereConditions,
        Yields2 yields,
        GroupBys2 groups,
        OrderBys2 orderings)
{

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
                .addIfNotNull("join", joinRoot)
                .addIfNot("where", whereConditions, Conditions2::isEmpty)
                .addIfNot("yields", yields, Yields2::isEmpty)
                .addIfNot("groups", groups, GroupBys2::isEmpty)
                .addIfNot("orderings", orderings, OrderBys2::isEmpty)
                .$();
    }

}
