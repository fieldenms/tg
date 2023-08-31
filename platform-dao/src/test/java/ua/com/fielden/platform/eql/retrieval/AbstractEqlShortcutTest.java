package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlTestCase;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.QueryModelResult;

public abstract class AbstractEqlShortcutTest extends EqlTestCase {

    protected static <T extends AbstractEntity<?>> void assertModelResultsEquals(final EntityResultQueryModel<T> exp, final EntityResultQueryModel<T> act) {
        assertModelResultsAreEqual(transformToModelResult(exp), transformToModelResult(act));
    }
    
    protected static void assertModelResultsEquals(final AggregatedResultQueryModel exp, final AggregatedResultQueryModel act) {
        assertModelResultsAreEqual(transformToModelResult(exp), transformToModelResult(act));
    }
    
    private static <T extends AbstractEntity<?>> void assertModelResultsAreEqual(final QueryModelResult<T> expQmr, final QueryModelResult<T> actQmr) {
        assertEquals("Qry model results (SQL) are different!", expQmr.sql(), actQmr.sql());
        assertEquals("Qry model results (Result Type) are different!", expQmr.resultType(), actQmr.resultType());
        assertEquals("Qry model results (Fetch Model) are different!", expQmr.fetchModel(), actQmr.fetchModel());
        assertEquals("Qry model results (Param values) are different!", expQmr.paramValues(), actQmr.paramValues());
        assertEquals("Qry model results (Yielded props infos) are different!", expQmr.yieldedColumns(), actQmr.yieldedColumns());
    }
    
    private static final <T extends AbstractEntity<?>> QueryModelResult<T> transformToModelResult(final QueryProcessingModel<T, ?> qem) {
        return EntityContainerFetcher.getModelResult(qem, H2, filter, null, dates, metadata());
    }
    
    protected static final <T extends AbstractEntity<?>> QueryModelResult<T> transformToModelResult(final EntityResultQueryModel<T> queryModel) {
        return transformToModelResult(new QueryProcessingModel<T, EntityResultQueryModel<T>>(queryModel, null, null, emptyMap(), true));
    }
    
    protected static final QueryModelResult<EntityAggregates> transformToModelResult(final AggregatedResultQueryModel queryModel) {
        return transformToModelResult(new QueryProcessingModel<EntityAggregates, AggregatedResultQueryModel>(queryModel, null, null, emptyMap(), true));
    }
    
    protected static <T extends AbstractEntity<?>> EntityTree<T> buildResultTree(final EntityResultQueryModel<T> act) {
        final QueryModelResult<T> modelResult = transformToModelResult(act);
        return EntityResultTreeBuilder.build(modelResult.resultType(), modelResult.yieldedColumns(), querySourceInfoProvider());
    }
    
    protected static EntityTree<EntityAggregates> buildResultTree(final AggregatedResultQueryModel act) {
        final QueryModelResult<EntityAggregates> modelResult = transformToModelResult(act);
        return EntityResultTreeBuilder.build(modelResult.resultType(), modelResult.yieldedColumns(), querySourceInfoProvider());
    }
}