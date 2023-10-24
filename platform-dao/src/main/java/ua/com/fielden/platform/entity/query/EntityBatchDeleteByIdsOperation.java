package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.Table;

public class EntityBatchDeleteByIdsOperation<T extends AbstractEntity<?>> {
    private final Session session;
    private final Table entityTable;

    public EntityBatchDeleteByIdsOperation(final Session session, final Table entityTable) {
        this.session = session;
        this.entityTable = entityTable;
    }

    public int deleteEntities(final String propName, final Collection<Long> ids) {
        final String deletionSql = composeDeletionSql(ids, propName);
        final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionSql, Collections.<String, Object> emptyMap());
        final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(session);
        return sqlQuery.executeUpdate();
    }

    private String composeDeletionSql(final Collection<Long> ids, final String propName) {
        final String propColumn = entityTable.columns.get(propName);
        final String idsCommaSeparated = StringUtils.join(ids, ",");
        return "DELETE FROM " + entityTable.name + " WHERE " + propColumn + " IN (" + idsCommaSeparated + ")";
    }
}