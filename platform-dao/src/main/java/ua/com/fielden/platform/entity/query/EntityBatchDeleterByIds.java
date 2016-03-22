package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityBatchDeleterByIds {
    private final QueryExecutionContext executionContext;
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityBatchDeleterByIds(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final Collection<Long> ids, Class<E> entityType) {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(executionContext.getDomainMetadata());
        String deletionSql = composeDeletionSql(ids, entityType, domainMetadataAnalyser);
        EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionSql, Collections.<String, Object> emptyMap());
        Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(executionContext.getSession());
        return sqlQuery.executeUpdate();
    }

    private <E extends AbstractEntity<?>> String composeDeletionSql(final Collection<Long> ids, Class<E> entityType, final DomainMetadataAnalyser domainMetadataAnalyser) {
        PersistedEntityMetadata<E> entityMetadata = ((PersistedEntityMetadata<E>) domainMetadataAnalyser.getEntityMetadata(entityType));
        String tableName = entityMetadata.getTable();
        String idColumn = entityMetadata.getProps().get("id").getColumn().getName();
        String idsCommaSeparated = StringUtils.join(ids, ",");
        return "DELETE FROM " + tableName + " WHERE " + idColumn + " IN (" + idsCommaSeparated + ")";
    }
}