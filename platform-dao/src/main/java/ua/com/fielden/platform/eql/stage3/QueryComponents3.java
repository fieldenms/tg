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
public class QueryComponents3 {
    public final IJoinNode3 joinRoot;
    public final Conditions3 whereConditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;

    public QueryComponents3(final IJoinNode3 joinRoot, final Conditions3 whereConditions, final Yields3 yields, final GroupBys3 groups, final OrderBys3 orderings) {
        this.joinRoot = joinRoot;
        this.whereConditions = whereConditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}