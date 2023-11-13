package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.transform;

import java.util.Collections;
import java.util.Map;

import org.hibernate.Query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;

public class EntityBatchDeleteByQueryModelOperation {
    private final QueryExecutionContext executionContext;

    public EntityBatchDeleteByQueryModelOperation(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final EntityResultQueryModel<E> model, final Map<String, Object> paramValues) {
        final DeletionModel deletionModel = getModelSql(model, paramValues);
        final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionModel.sql, deletionModel.sqlParamValues);
        final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(executionContext.getSession());
        return sqlQuery.executeUpdate();
    }

    private <T extends AbstractEntity<?>> DeletionModel getModelSql(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        final EqlDomainMetadata eqlDomainMetadata = executionContext.getEqlDomainMetadata();
        final AggregatedResultQueryModel finalModel = select(model.getResultType()).where().prop(ID).in().model(model).yield().prop(ID).as(ID).modelAsAggregate();
        final String tableName = eqlDomainMetadata.entityMetadataHolder.getTableForEntityType(model.getResultType()).name;
        final TransformationResultFromStage2To3<ResultQuery3> s2tr = transform(new QueryProcessingModel(finalModel, null, null, paramValues, true), null, null, executionContext.dates(), eqlDomainMetadata); 
        final ResultQuery3 entQuery3 = s2tr.item;
        final String selectionSql = entQuery3.sql(eqlDomainMetadata.dbVersion);
        final String deletionSql = produceDeletionSql(selectionSql, tableName, eqlDomainMetadata.dbVersion);
        return new DeletionModel(deletionSql, s2tr.updatedContext.getSqlParamValues());
    }

    private String produceDeletionSql(final String selectionSql, final String tableName, final DbVersion dbVersion) {
        final int markerStart = selectionSql.indexOf(" IN ");
        return "DELETE FROM %s WHERE %s %s".formatted(tableName, dbVersion.idColumnName(), selectionSql.substring(markerStart));
    }

    private static class DeletionModel {
        private final String sql;
        private final Map<String, Object> sqlParamValues;

        DeletionModel(final String sql, final Map<String, Object> sqlParamValues) {
            this.sql = sql;
            this.sqlParamValues = Collections.unmodifiableMap(sqlParamValues);
        }
    }
}