package ua.com.fielden.platform.eql.stage3;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;

/// Covers transformations of id-only queries.
///
public class IdOnlyQueryTransformationTest extends EqlStage3TestCase {

    @Test
    public void local_id_only_query_remains_a_top_level_query() {
        final var actualEql = select(TgVehicle.class).model();
        final var actualQuery = qry(actualEql, fetchIdOnly(TgVehicle.class));

        final ResultQuery3 expectedQuery;
        {
            final var vehicleSource = source(TgVehicle.class, 1);
            final var yields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE));
            expectedQuery = qry(sources(vehicleSource), yields, TgVehicle.class);
        }

        assertAlphaEq(expectedQuery, actualQuery);
    }

    @Test
    public void foreign_id_only_query_becomes_nested() {
        final var actualEql = select(TgVehicle.class).yield().prop("model").modelAsEntity(TgVehicleModel.class);
        final var expectedEql = select(TgVehicleModel.class).where().prop(ID).in().model(actualEql).model();
        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

}
