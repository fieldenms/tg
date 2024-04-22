package ua.com.fielden.platform.entity.query;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.dao.exceptions.EntityDeletionException;
import ua.com.fielden.platform.eql.meta.EqlTable;

import java.util.Collection;
import java.util.Collections;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES;
import static ua.com.fielden.platform.companion.DeleteOperations.ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS;

public class EntityBatchDeleteByIdsOperation<T extends AbstractEntity<?>> {
    private static final Logger LOGGER = getLogger(EntityBatchDeleteByIdsOperation.class);

    private final Session session;
    private final EqlTable entityTable;

    public EntityBatchDeleteByIdsOperation(final Session session, final EqlTable entityTable) {
        this.session = session;
        this.entityTable = entityTable;
    }

    public int deleteEntities(final String propName, final Collection<Long> ids) {
        try {
            final String deletionSql = composeDeletionSql(ids, propName);
            final EntityHibernateDeletionQueryProducer entityHibernateDeletionQueryProducer = new EntityHibernateDeletionQueryProducer(deletionSql, Collections.<String, Object> emptyMap());
            final Query sqlQuery = entityHibernateDeletionQueryProducer.produceHibernateQuery(session);
            return sqlQuery.executeUpdate();
        } catch (final javax.persistence.PersistenceException ex) {
            final var msg = ex.getCause() instanceof ConstraintViolationException
                            ? ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_EXISTING_DEPENDENCIES
                            : ERR_DELETION_WAS_UNSUCCESSFUL_DUE_TO_OTHER_REASONS.formatted(ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityDeletionException(msg, ex.getCause());
        }
    }

    private static final String DELETE_FROM = "DELETE FROM %s WHERE %s IN (%s)";
    private String composeDeletionSql(final Collection<Long> ids, final String propName) {
        final String propColumn = entityTable.columns().get(propName);
        final String idsCommaSeparated = StringUtils.join(ids, ",");
        return DELETE_FROM.formatted(entityTable.name(), propColumn, idsCommaSeparated);
    }

}