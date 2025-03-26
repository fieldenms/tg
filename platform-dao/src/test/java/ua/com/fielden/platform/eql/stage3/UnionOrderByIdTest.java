package ua.com.fielden.platform.eql.stage3;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.EqlStage3TestCase.*;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

public class UnionOrderByIdTest extends AbstractDaoTestCase {

    private final EqlQueryTransformer eqlQueryTransformer = getInstance(EqlQueryTransformer.class);

    @Before
    public void setUp() {
        resetSqlId();
    }

    @Test
    @WithDbVersion(MSSQL)
    public void MSSQL_id_is_implicitly_ordered_by_in_union_query_with_explicit_ordering_and_yielded_id() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(ID, querySource, ID, LONG_PROP_TYPE),
                                       yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));
        final var queryOrdering = orders(new OrderBy3(queryYields.yieldsMap().get(KEY), false),
                                         new OrderBy3(queryYields.yieldsMap().get(ID), false));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    queryOrdering,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Test
    @WithDbVersion(POSTGRESQL)
    public void POSTGRESQL_id_is_NOT_implicitly_ordered_by_in_union_query_with_explicit_ordering_and_yielded_id() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(ID, querySource, ID, LONG_PROP_TYPE),
                                       yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));
        final var queryOrdering = orders(new OrderBy3(queryYields.yieldsMap().get(KEY), false));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    queryOrdering,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_without_explicit_ordering() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(ID, querySource, ID, LONG_PROP_TYPE),
                                       yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    null,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_if_id_is_not_yielded() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));
        final var queryOrdering = orders(new OrderBy3(queryYields.yieldsMap().get(KEY), false));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    queryOrdering,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_if_yield_id_is_ordered_by_explicitly() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .orderBy().yield(ID).asc().yield(KEY).desc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(ID, querySource, ID, LONG_PROP_TYPE),
                                       yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));
        final var queryOrdering = orders(new OrderBy3(queryYields.yieldsMap().get(ID), false),
                                         new OrderBy3(queryYields.yieldsMap().get(KEY), true));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    queryOrdering,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_if_prop_id_is_ordered_by_explicitly() {
        final var queryModel = select(select(TeVehicle.class)
                                              .yield().prop(KEY).as(KEY)
                                              .yield().prop(ID).as(ID)
                                              .modelAsEntity(TeVehicle.class))
                .orderBy().prop(ID).asc().yield(KEY).desc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(queryModel);

        final var vehicleSource = source(TeVehicle.class, 1);
        final var sourceQueryYields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE),
                                             yieldProp(KEY, vehicleSource, KEY, STRING_PROP_TYPE));

        final var querySource = source(2, srcqry(sources(vehicleSource), sourceQueryYields, TeVehicle.class));
        final var queryYields = yields(yieldProp(ID, querySource, ID, LONG_PROP_TYPE),
                                       yieldProp(KEY, querySource, KEY, STRING_PROP_TYPE));
        final var queryOrdering = orders(new OrderBy3(prop(ID, querySource, LONG_PROP_TYPE), false),
                                         new OrderBy3(queryYields.yieldsMap().get(KEY), true));

        final var expectedQry = qry(sources(querySource),
                                    queryYields,
                                    queryOrdering,
                                    TeVehicle.class);

        assertQueryEquals(expectedQry, actualQuery);
    }

    @Override
    protected void populateDomain() {}

    private <T extends AbstractEntity<?>> ResultQuery3 transform(final EntityResultQueryModel<T> qry) {
        return eqlQueryTransformer
               .transform(new QueryProcessingModel<>(qry, null, null, emptyMap(), true), empty())
               .item;
    }

}
