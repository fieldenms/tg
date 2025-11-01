package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes;

import static java.lang.String.join;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.union;
import static ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes.Property.unionEntityDetails;

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

    @Test
    public void id_is_appended_to_union_typed_property_path() {
        final var query1 = select(TgEntityWithManyPropTypes.class)
                .where()
                .prop(unionEntityDetails + "." + union).eq().val(123)
                .yield().prop(ID).as(ID)
                .modelAsAggregate();

        final var query2 = select(TgEntityWithManyPropTypes.class)
                .where()
                .prop(unionEntityDetails + "." + union + "." + ID).eq().val(123)
                .yield().prop(ID).as(ID)
                .modelAsAggregate();

        assertEquals(qry(query2), qry(query1));
    }

}
