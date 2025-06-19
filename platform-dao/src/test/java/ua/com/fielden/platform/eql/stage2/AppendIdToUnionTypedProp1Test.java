package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.union;

public class AppendIdToUnionTypedProp1Test extends EqlStage2TestCase {

    @Test
    public void id_is_appended_to_union_typed_property() {
        final var query1 = select(UnionEntityDetails.class)
                .where()
                .prop(union.toPath()).eq().val(123)
                .yield().prop(ID).as(ID)
                .modelAsEntity(UnionEntityDetails.class);

        final var query2 = select(UnionEntityDetails.class)
                .where()
                .prop(union + "." + ID).eq().val(123)
                .yield().prop(ID).as(ID)
                .modelAsEntity(UnionEntityDetails.class);

        assertEquals(qry(query2), qry(query1));
    }

}
