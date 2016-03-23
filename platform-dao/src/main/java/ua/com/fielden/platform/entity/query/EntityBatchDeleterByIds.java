package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityBatchDeleterByIds<E extends AbstractEntity<?>> {
    private final Session session;
    private final PersistedEntityMetadata<E> entityMetadata;

    public EntityBatchDeleterByIds(final Session session, PersistedEntityMetadata<E> entityMetadata) {
        this.session = session;
        this.entityMetadata = entityMetadata;
    }

    public <E extends AbstractEntity<?>> int deleteEntities(final String propName, final Collection<Long> ids) {
        String deletionSql = composeDeletionSql(ids, propName);
        EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionSql, Collections.<String, Object> emptyMap());
        Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(session);
        return sqlQuery.executeUpdate();
    }

    private <E extends AbstractEntity<?>> String composeDeletionSql(final Collection<Long> ids, final String propName) {
        String tableName = entityMetadata.getTable();
        String propColumn = entityMetadata.getProps().get(propName).getColumn().getName();
        String idsCommaSeparated = StringUtils.join(ids, ",");
        return "DELETE FROM " + tableName + " WHERE " + propColumn + " IN (" + idsCommaSeparated + ")";
    }
}