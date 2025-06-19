package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ExpandUnionTypedPropYield1Test extends EqlStage2TestCase {

    @Test
    public void union_typed_prop_yield_is_transformed_into_yields_for_all_union_members() {
        final var query1 = select(UnionEntityDetails.class)
                .yield().prop("union").as("union")
                .modelAsEntity(UnionEntityDetails.class);

        final var query2 = select(UnionEntityDetails.class)
                .yield().prop("union.propertyOne").as("union.propertyOne")
                .yield().prop("union.propertyTwo").as("union.propertyTwo")
                .modelAsEntity(UnionEntityDetails.class);

        assertEquals(qry(query2), qry(query1));
    }

}
