package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

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
        OrderBys3 orderings) {
}