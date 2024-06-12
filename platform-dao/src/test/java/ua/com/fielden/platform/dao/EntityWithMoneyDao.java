package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.stream.Stream;

import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.utils.Pair;

/**
 * A DAO for {@link EntityWithMoney} used for testing.
 *
 * @author TG Team
 */
@EntityType(EntityWithMoney.class)
public class EntityWithMoneyDao extends CommonEntityDao<EntityWithMoney> implements IEntityWithMoney {
    public static final String ERR_PURPOSEFUL_EXCEPTION = "Purposeful exception.";

    @SessionRequired
    public EntityWithMoney saveWithException(final EntityWithMoney entity) {
        super.save(entity);
        throw new RuntimeException(ERR_PURPOSEFUL_EXCEPTION);
    }

    @SessionRequired
    public EntityWithMoney saveTwoWithException(final EntityWithMoney one, final EntityWithMoney two) {
        super.save(one);
        super.save(two);
        throw new RuntimeException("Purposeful exception.");
    }

    @SessionRequired
    public Pair<Session, Session> getSessionWithDelay(final long sleep) throws Exception {
        final Session ses = getSessionUnsafe();
        Thread.sleep(sleep);
        return new Pair<Session, Session>(ses, getSessionUnsafe());
    }

    @SessionRequired
    public long streamProcessingWithinTransaction(final EntityResultQueryModel<EntityWithMoney> query) {
        long result = 0;
        try (final Stream<EntityWithMoney> stream = stream(from(query).model())) {
            result = result + stream.count();
        }
        result = result + count(query);
        return result;
    }

    // @SessionRequired -- deliberately not annotated
    public EntityWithMoney superSave(final EntityWithMoney entity) {
        return super.save(entity);
    }

}
