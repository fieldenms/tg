package ua.com.fielden.platform.entity.query;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import ua.com.fielden.platform.dao.exceptions.EntityDeletionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.IDates;

import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.transform;

public class EntityBatchDeleteByQueryModelOperation {
    private static final Logger LOGGER = getLogger(EntityBatchDeleteByQueryModelOperation.class);

    private final IDomainMetadata domainMetadata;
    private final IDates dates;
    private final Supplier<Session> session;

    public EntityBatchDeleteByQueryModelOperation(
            final IDomainMetadata domainMetadata,
            final IDates dates,
            final Supplier<Session> session)
    {
        this.domainMetadata = domainMetadata;
        this.dates = dates;
        this.session = session;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final EntityResultQueryModel<E> model, final Map<String, Object> paramValues) {
        try {
            final DeletionModel deletionModel = getModelSql(model, paramValues);
            final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionModel.sql, deletionModel.sqlParamValues);
            final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(session.get());
            return sqlQuery.executeUpdate();
        } catch (final PersistenceException ex) {
            final var msg = ex.getCause() instanceof ConstraintViolationException
                            ? ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES
                            : ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS.formatted(ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityDeletionException(msg, ex.getCause());
        }
    }

    private <T extends AbstractEntity<?>> DeletionModel getModelSql(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        final AggregatedResultQueryModel finalModel = select(model.getResultType()).where().prop(ID).in().model(model).yield().prop(ID).as(ID).modelAsAggregate();
        final String tableName = domainMetadata.getTableForEntityType(model.getResultType()).name();
        final TransformationResultFromStage2To3<ResultQuery3> s2tr = transform(new QueryProcessingModel(finalModel, null, null, paramValues, true), null, empty(), dates, domainMetadata);
        final ResultQuery3 entQuery3 = s2tr.item;
        final String selectionSql = entQuery3.sql(domainMetadata.dbVersion());
        final String deletionSql = produceDeletionSql(selectionSql, tableName, domainMetadata.dbVersion());
        return new DeletionModel(deletionSql, s2tr.updatedContext.getSqlParamValues());
    }

    private static final String DELETE_FROM = "DELETE FROM %s WHERE %s %s";
    private String produceDeletionSql(final String selectionSql, final String tableName, final DbVersion dbVersion) {
        final int markerStart = selectionSql.indexOf(" IN ");
        return DELETE_FROM.formatted(tableName, dbVersion.idColumnName(), selectionSql.substring(markerStart));
    }

    private record DeletionModel(String sql, Map<String, Object> sqlParamValues) {
        private DeletionModel(final String sql, final Map<String, Object> sqlParamValues) {
            this.sql = sql;
            this.sqlParamValues = Collections.unmodifiableMap(sqlParamValues);
        }
    }

}
