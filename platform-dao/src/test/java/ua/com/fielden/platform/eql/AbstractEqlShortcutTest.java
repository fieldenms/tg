package ua.com.fielden.platform.eql;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.eql.retrieval.EntityContainerFetcher.getResultPropsInfos;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlTestCase;
import ua.com.fielden.platform.eql.retrieval.QueryModelResult;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage3.operands.queries.ResultQuery3;

public abstract class AbstractEqlShortcutTest extends EqlTestCase {

    protected static <T extends AbstractEntity<?>> void assertModelResultsEquals(final EntityResultQueryModel<T> exp, final EntityResultQueryModel<T> act) {
        final QueryModelResult<T> expQmr = transformToModelResult(new QueryProcessingModel<T, EntityResultQueryModel<T>>(exp, null, null, emptyMap(), true));
        final QueryModelResult<T> actQmr = transformToModelResult(new QueryProcessingModel<T, EntityResultQueryModel<T>>(act, null, null, emptyMap(), true));
        
        assertEquals("Qry model results (SQL) are different!", expQmr.sql, actQmr.sql);
        assertEquals("Qry model results (Result Type) are different!", expQmr.resultType, actQmr.resultType);
        assertEquals("Qry model results (Fetch Model) are different!", expQmr.fetchModel, actQmr.fetchModel);
        assertEquals("Qry model results (Param values) are different!", expQmr.getParamValues(), actQmr.getParamValues());
        assertEquals("Qry model results (Yielded props infos) are different!", expQmr.getYieldedPropsInfo(), actQmr.getYieldedPropsInfo());
    }
    
    private static final <E extends AbstractEntity<?>> QueryModelResult<E> transformToModelResult(final QueryProcessingModel<E, ?> qem) {
        final TransformationResult2<ResultQuery3> tr = EqlQueryTransformer.transform(qem, filter, null, dates, metadata());
        final ResultQuery3 entQuery3 = tr.item;
        final String sql = entQuery3.sql(H2);
        return new QueryModelResult<>((Class<E>) entQuery3.resultType, sql, getResultPropsInfos(entQuery3.yields), tr.updatedContext.getParamValues(), qem.fetchModel);
    }
}