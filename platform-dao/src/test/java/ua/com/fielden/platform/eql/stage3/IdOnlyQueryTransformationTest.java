package ua.com.fielden.platform.eql.stage3;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.IRetrievalModel.createRetrievalModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.EqlStage3TestCase.*;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;

/// Covers transformations of id-only queries.
///
public class IdOnlyQueryTransformationTest extends AbstractDaoTestCase {

    private final EqlQueryTransformer eqlQueryTransformer = getInstance(EqlQueryTransformer.class);

    @Before
    public void setUp() {
        resetSqlId();
    }

    @Test
    public void local_id_only_query_remains_a_top_level_query() {
        final var inQuery = select(TgVehicle.class).model();
        final var compiledQuery = transform(inQuery, fetchIdOnly(TgVehicle.class));

        final ResultQuery3 expectedCompiledQuery;
        {
            final var vehicleSource = source(TgVehicle.class, 1);
            final var yields = yields(yieldProp(ID, vehicleSource, ID, LONG_PROP_TYPE));
            expectedCompiledQuery = qry(sources(vehicleSource),
                                        yields,
                                        TgVehicle.class);
        }

        assertQueryEquals(expectedCompiledQuery, compiledQuery);
    }

    @Test
    public void foreign_id_only_query_becomes_nested() {
        final var inQuery = select(TgVehicle.class).yield().prop("model").modelAsEntity(TgVehicleModel.class);
        final var compiledQuery = transform(inQuery);

        final var expectedInQuery = select(TgVehicleModel.class).where().prop(ID).in().model(inQuery).model();
        final var expectedCompiledQuery = transform(expectedInQuery);

        assertQueryEquals(expectedCompiledQuery, compiledQuery);
    }

    @Override
    protected void populateDomain() {}

    private <T extends AbstractEntity<?>> ResultQuery3 transform(final EntityResultQueryModel<T> qry) {
        return eqlQueryTransformer
               .transform(new QueryProcessingModel<>(qry, null, null, emptyMap(), true), empty())
               .item;
    }

    private <T extends AbstractEntity<?>> ResultQuery3 transform(final EntityResultQueryModel<T> qry, final fetch<T> fetchModel) {
        final var retrievalModel = createRetrievalModel(fetchModel, getInstance(IDomainMetadata.class), getInstance(QuerySourceInfoProvider.class));
        return eqlQueryTransformer
                .transform(new QueryProcessingModel<>(qry, null, retrievalModel, emptyMap(), true), empty())
                .item;
    }

}
