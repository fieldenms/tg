package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

/**
 * Represents all structural query parts (components) obtained as the result of stage 1 to stage 2 transformation.
 * This class is used as a convenience to keep all the query components together.
 * 
 */
public class QueryComponents2 {
    public final IJoinNode2<? extends IJoinNode3> joinRoot;
    public final Conditions2 whereConditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;

    public QueryComponents2(final IJoinNode2<? extends IJoinNode3> joinRoot, final Conditions2 whereConditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        this.joinRoot = joinRoot;
        this.whereConditions = whereConditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}