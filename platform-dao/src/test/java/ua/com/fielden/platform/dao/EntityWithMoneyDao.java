package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.stream.Stream;

import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.utils.Pair;

/**
 * A DAO for {@link EntityWithMoney} used for testing.
 * 
 * @author 01es
 * 
 */
@EntityType(EntityWithMoney.class)
public class EntityWithMoneyDao extends CommonEntityDao<EntityWithMoney> implements IEntityWithMoney {

    @Inject
    protected EntityWithMoneyDao(final IFilter filter) {
        super(filter);
    }

    @SessionRequired
    public EntityWithMoney saveWithException(final EntityWithMoney entity) {
        super.save(entity);
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
    
    @SessionRequired
    public long streamProcessingWithinTransaction(final EntityResultQueryModel<EntityWithMoney> query) {
        long result = 0;
        try(final Stream<EntityWithMoney> stream = stream(from(query).model())) {
            result = result + stream.count();
        }
        result = result + count(query);
        return result;
    }

}
