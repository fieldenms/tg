package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.stage3.EqlQueryTransformer.transform;

import java.util.Collections;
import java.util.Map;

import org.hibernate.Query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.operands.queries.ResultQuery3;

public class EntityBatchDeleteByQueryModelOperation {
    private final QueryExecutionContext executionContext;

    public EntityBatchDeleteByQueryModelOperation(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final EntityResultQueryModel<E> model, final Map<String, Object> paramValues) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        final DeletionModel deletionModel = getModelSql(model, paramValues, domainMetadataAnalyser);
        final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionModel.sql, deletionModel.sqlParamValues);
        final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(executionContext.getSession());
        return sqlQuery.executeUpdate();
    }

    private <T extends AbstractEntity<?>> DeletionModel getModelSql(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues, final DomainMetadataAnalyser domainMetadataAnalyser) {
        final AggregatedResultQueryModel finalModel = select(model.getResultType()).where().prop(ID).in().model(model).yield().prop(ID).as(ID).modelAsAggregate();
        final String tableName = ((PersistedEntityMetadata<AbstractEntity<?>>) domainMetadataAnalyser.getEntityMetadata(model.getResultType())).getTable();
        final var eqlMetaData = executionContext.getDomainMetadata().eqlDomainMetadata;
        final TransformationResult2<ResultQuery3> s2tr = transform(new QueryProcessingModel(finalModel, null, null, paramValues, true), null, null, executionContext.dates(), eqlMetaData); 
        final ResultQuery3 entQuery3 = s2tr.item;
        final String selectionSql = entQuery3.sql(domainMetadataAnalyser.getDbVersion());
        final String deletionSql = produceDeletionSql(selectionSql, tableName, eqlMetaData.dbVersion);
        return new DeletionModel(deletionSql, s2tr.updatedContext.getParamValues());
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