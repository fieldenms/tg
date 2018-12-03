package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collections;
import java.util.Map;

import org.hibernate.Query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityBatchDeleteByQueryModelOperation {
    private final QueryExecutionContext executionContext;
    private static final String TABLE_ALIAS = "TABLE-FOR-DELETION";

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
        final AggregatedResultQueryModel finalModel = select(model.getResultType()).as(TABLE_ALIAS).where().prop(ID).in().model(model).yield().prop(ID).as(ID).modelAsAggregate();
        final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, null, null, executionContext.getUniversalConstants());
        final EntQuery entQuery = gen.generateEntQueryAsResultQuery(finalModel, null, finalModel.getResultType(), null, paramValues);
        final String tableName = ((PersistedEntityMetadata<AbstractEntity<?>>) domainMetadataAnalyser.getEntityMetadata(model.getResultType())).getTable();
        final String selectionSql = entQuery.sql();
        final String deletionSql = produceDeletionSql(selectionSql, tableName);
        final Map<String, Object> sqlParamValues = entQuery.getValuesForSqlParams();
        return new DeletionModel(deletionSql, sqlParamValues);
    }
    
    private String produceDeletionSql(final String selectionSql, final String tableName) {
        final int markerStart = selectionSql.indexOf(TABLE_ALIAS);
        return "DELETE FROM " + tableName + " WHERE " + selectionSql.substring(markerStart + TABLE_ALIAS.length() + 2 + 10);
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