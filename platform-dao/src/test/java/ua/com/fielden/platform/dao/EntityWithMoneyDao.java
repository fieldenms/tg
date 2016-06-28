package ua.com.fielden.platform.dao;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.utils.Pair;

/**
 * A DAO for {@link EntityWithMoney} used for testing.
 * 
 * @author 01es
 * 
 */
@EntityType(EntityWithMoney.class)
public class EntityWithMoneyDao extends CommonEntityDao<EntityWithMoney> {

    @Inject
    protected EntityWithMoneyDao(final IFilter filter) {
        super(filter);
    }

    @SessionRequired
    public EntityWithMoney saveWithException(final EntityWithMoney entity) {
        final EntityWithMoney savedEntity = super.save(entity);
        throw new RuntimeException("Purposeful exception.");
    }

    @SessionRequired
    public EntityWithMoney saveTwoWithException(final EntityWithMoney one, final EntityWithMoney two) {
        super.save(one);
        super.save(two);
        throw new RuntimeException("Purposeful exception.");
    }

    @SessionRequired
    public Pair<Session, Session> getSessionWithDelay(final long sleep) throws Exception {
        final Session ses = getSession();
        Thread.sleep(sleep);
        return new Pair<Session, Session>(ses, getSession());
    }

}
