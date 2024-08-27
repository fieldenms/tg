package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Optional;

/**
 * Represents all structural query parts (components) obtained as the result of stage 2 to stage 3 transformation.
 * This class is used as a convenience to keep all the query components together.
 *
 */
public record QueryComponents3(
        Optional<IJoinNode3> maybeJoinRoot,
        Conditions3 whereConditions,
        Yields3 yields,
        GroupBys3 groups,
        OrderBys3 orderings)
{

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
                .addIfPresent("join", maybeJoinRoot)
                .addIfNot("where", whereConditions, Conditions3::isEmpty)
                .addIfNot("yields", yields, Yields3::isEmpty)
                .addIfNot("groups", groups, GroupBys3::isEmpty)
                .addIfNot("orderings", orderings, OrderBys3::isEmpty)
                .$();
    }

}
