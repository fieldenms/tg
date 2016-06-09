package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class EntityBatchDeleterByQueryModel {
    private final QueryExecutionContext executionContext;
    private final String tableAlias = "TABLE-FOR-DELETION";
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityBatchDeleterByQueryModel(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final EntityResultQueryModel<E> model, final Map<String, Object> paramValues) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        DeletionModel deletionModel = getModelSql(model, paramValues, domainMetadataAnalyser);
        EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionModel.sql, deletionModel.sqlParamValues);
        Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(executionContext.getSession());
        return sqlQuery.executeUpdate();
    }

    private <T extends AbstractEntity<?>> DeletionModel getModelSql(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues, final DomainMetadataAnalyser domainMetadataAnalyser) {
        AggregatedResultQueryModel finalModel = select(model.getResultType()).as(tableAlias).where().prop(ID).in().model(model).yield().prop(ID).as(ID).modelAsAggregate();
        final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, null, null, executionContext.getUniversalConstants());
        final EntQuery entQuery = gen.generateEntQueryAsResultQuery(finalModel, null, finalModel.getResultType(), null, paramValues);
        String tableName = ((PersistedEntityMetadata<AbstractEntity<?>>) domainMetadataAnalyser.getEntityMetadata(model.getResultType())).getTable();
        String selectionSql = entQuery.sql();
        String deletionSql = produceDeletionSql(selectionSql, tableName);
        final Map<String, Object> sqlParamValues = entQuery.getValuesForSqlParams();
        return new DeletionModel(deletionSql, sqlParamValues);
    }
    
    private String produceDeletionSql(String selectionSql, String tableName) {
        int markerStart = selectionSql.indexOf(tableAlias);
        return "DELETE FROM " + tableName + " WHERE " + selectionSql.substring(markerStart + tableAlias.length() + 2 + 10);
    }

    private class DeletionModel {
        private final String sql;
        private final Map<String, Object> sqlParamValues;

        DeletionModel(String sql, Map<String, Object> sqlParamValues) {
            this.sql = sql;
            this.sqlParamValues = sqlParamValues;
        }
    }
}