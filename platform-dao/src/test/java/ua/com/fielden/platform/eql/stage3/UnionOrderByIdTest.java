package ua.com.fielden.platform.eql.stage3;

import org.junit.After;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage2.queries.UnionOrderById;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.EqlStage3TestCase.assertQueryEquals;

/// Tests for the [UnionOrderById] transformation.
///
/// Each test compiles the "actual" query with the transformation enabled and the "expected" query with it disabled (via [UnionOrderById#enabled]),
/// then compares the two for alpha-equivalence.
/// Compiling the expected query with the transformation disabled produces a transformation-independent baseline,
/// which keeps the comparison meaningful even when the expected and actual query models are identical:
/// were the transformation to wrongly add (or omit) an `id` ordering, the actual query would diverge from the baseline.
///
public class UnionOrderByIdTest extends AbstractDaoTestCase {

    // TODO Refactor to extend EqlStage3TestCase.
    // @WithDbVersion should be dropped and a runtime DbVersion mocked.

    private final EqlQueryTransformer eqlQueryTransformer = getInstance(EqlQueryTransformer.class);

    @After
    public void restoreUnionOrderById() {
        UnionOrderById.enabled = true;
    }

    @Test
    @WithDbVersion(MSSQL)
    public void MSSQL_id_is_implicitly_ordered_by_in_union_query_with_explicit_ordering_and_yielded_id() {
        final var actualEql = select(select(TeVehicle.class)
                                             .yield().prop(KEY).as(KEY)
                                             .yield().prop(ID).as(ID)
                                             .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        // The actual query relies on the transformation to add `id` to the ordering implicitly.
        // The expected query writes the ordering out in full and is compiled with the transformation disabled.
        final var expectedEql = select(select(TeVehicle.class)
                                               .yield().prop(KEY).as(KEY)
                                               .yield().prop(ID).as(ID)
                                               .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                          .yield(ID).asc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(actualEql);
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(expectedEql);

        assertQueryEquals(expectedQuery, actualQuery);
    }

    @Test
    @WithDbVersion(POSTGRESQL)
    public void POSTGRESQL_id_is_NOT_implicitly_ordered_by_in_union_query_with_explicit_ordering_and_yielded_id() {
        final var eql = select(select(TeVehicle.class)
                                       .yield().prop(KEY).as(KEY)
                                       .yield().prop(ID).as(ID)
                                       .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(eql);
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(eql);

        assertQueryEquals(expectedQuery, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_without_explicit_ordering() {
        final var eql = select(select(TeVehicle.class)
                                       .yield().prop(KEY).as(KEY)
                                       .yield().prop(ID).as(ID)
                                       .modelAsEntity(TeVehicle.class))
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(eql);
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(eql);

        assertQueryEquals(expectedQuery, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_if_id_is_not_yielded() {
        final var eql = select(select(TeVehicle.class)
                                       .yield().prop(KEY).as(KEY)
                                       .yield().prop(ID).as(ID)
                                       .modelAsEntity(TeVehicle.class))
                .orderBy().yield(KEY).asc()
                .yield().prop(KEY).as(KEY)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(eql);
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(eql);

        assertQueryEquals(expectedQuery, actualQuery);
    }

    @Test
    public void id_is_NOT_implicitly_ordered_by_in_union_query_if_yield_id_is_ordered_by_explicitly() {
        final var eql = select(select(TeVehicle.class)
                                       .yield().prop(KEY).as(KEY)
                                       .yield().prop(ID).as(ID)
                                       .modelAsEntity(TeVehicle.class))
                .orderBy().yield(ID).asc().yield(KEY).desc()
                .yield().prop(KEY).as(KEY)
                .yield().prop(ID).as(ID)
                .modelAsEntity(TeVehicle.class);

        final var actualQuery = transform(eql);
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(eql);

        assertQueryEquals(expectedQuery, actualQuery);
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
        UnionOrderById.enabled = false;
        final var expectedQuery = transform(queryModel);

        assertQueryEquals(expectedQuery, actualQuery);
    }

    @Override
    protected void populateDomain() {}

    private <T extends AbstractEntity<?>> ResultQuery3 transform(final EntityResultQueryModel<T> qry) {
        return eqlQueryTransformer
               .transform(new QueryProcessingModel<>(qry, null, null, emptyMap(), true), empty())
               .item;
    }

}
