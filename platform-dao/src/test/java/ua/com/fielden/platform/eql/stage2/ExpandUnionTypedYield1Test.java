package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes;

import static java.lang.String.join;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.union;
import static ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes.Property.unionEntityDetails;

public class ExpandUnionTypedYield1Test extends EqlStage2TestCase {

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

    @Test
    public void union_typed_prop_path_yield_is_transformed_into_yields_for_all_union_members() {
        final var query1 = select(TgEntityWithManyPropTypes.class)
                .yield().prop(unionEntityDetails + "." + union).as(unionEntityDetails + "." + union)
                .modelAsEntity(TgEntityWithManyPropTypes.class);

        final var query2 = select(TgEntityWithManyPropTypes.class)
                .yield().prop(unionEntityDetails  + "." + union  + "." +  "propertyOne").as(unionEntityDetails + "." + union + "." +  "propertyOne")
                .yield().prop(unionEntityDetails + "." + union + "." + "propertyTwo").as(unionEntityDetails  + "." +  union + "." + "propertyTwo")
                .modelAsEntity(TgEntityWithManyPropTypes.class);

        assertEquals(qry(query2), qry(query1));
    }

}
