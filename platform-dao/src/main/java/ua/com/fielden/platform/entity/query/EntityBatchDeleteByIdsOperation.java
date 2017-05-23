package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityBatchDeleteByIdsOperation<T extends AbstractEntity<?>> {
    private final Session session;
    private final PersistedEntityMetadata<T> entityMetadata;

    public EntityBatchDeleteByIdsOperation(final Session session, PersistedEntityMetadata<T> entityMetadata) {
        this.session = session;
        this.entityMetadata = entityMetadata;
    }

    public int deleteEntities(final String propName, final Collection<Long> ids) {
        final String deletionSql = composeDeletionSql(ids, propName);
        final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionSql, Collections.<String, Object> emptyMap());
        final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(session);
        return sqlQuery.executeUpdate();
    }

    private String composeDeletionSql(final Collection<Long> ids, final String propName) {
        final String tableName = entityMetadata.getTable();
        final String propColumn = entityMetadata.getProps().get(propName).getColumn().getName();
        final String idsCommaSeparated = StringUtils.join(ids, ",");
        return "DELETE FROM " + tableName + " WHERE " + propColumn + " IN (" + idsCommaSeparated + ")";
    }
}