package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.serial;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.union;

public class GroupByPropEnhancementTest extends EqlStage2TestCase {

    @Test
    public void expanded_composite_key_contains_path_to_the_key_of_union_typed_key_member() {
        final var query = qry(select(UnionEntityDetails.class)
                                      .groupBy().prop(KEY)
                                      .yield().countAll()
                                      .modelAsEntity(UnionEntityDetails.class));

        final var expectedQuery = qry(select(UnionEntityDetails.class)
                                              .groupBy().prop(serial.toPath())
                                              .groupBy().prop(union + "." + KEY)
                                              .yield().countAll()
                                              .modelAsEntity(UnionEntityDetails.class));

        assertEquals(expectedQuery, query);
    }

}
