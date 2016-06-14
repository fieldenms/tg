package ua.com.fielden.platform.test.transactional;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.types.Money;

/**
 * A helper class for testing transactional support.
 *
 * @author TG Team
 *
 */
class LogicThatNeedsTransaction implements ISessionEnabled {
    private final IEntityDao<EntityWithMoney> dao;
    private Session session;
    private String transactionGuid;

    private final EntityFactory factory;

    @Override
    public Session getSession() {
        if (session == null) {
            throw new IllegalStateException("Someone forgot to annotate some method with SessionRequired!");
        }
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }

    @Override
    public String getTransactionGuid() {
        if (StringUtils.isEmpty(transactionGuid)) {
            throw new IllegalStateException("Transaction GUID is missing.");
        }
        return transactionGuid;
    }
    
    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

    @Inject
    public LogicThatNeedsTransaction(final IEntityDao<EntityWithMoney> dao, final EntityFactory factory) {
        this.dao = dao;
        this.factory = factory;
    }

    @SessionRequired
    public void singleTransactionInvocaion(final String amountOne, final String amountTwo) {
        final EntityWithMoney oneAmount = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money(amountOne));
        final EntityWithMoney twoAmount = factory.newEntity(EntityWithMoney.class, "two", "second").setMoney(new Money(amountTwo));
        dao.save(oneAmount);
        dao.save(twoAmount);
    }

    @SessionRequired
    public void nestedTransactionInvocaion(final String amountOne, final String amountTwo) {
        singleTransactionInvocaion(amountOne, amountTwo);
    }

    @SessionRequired
    public void transactionalInvocaionWithException(final String amountOne, final String amountTwo) {
        singleTransactionInvocaion(amountOne, amountTwo);
        final EntityWithMoney threeAmount = factory.newEntity(EntityWithMoney.class, "three", "third").setMoney(new Money("90.00"));
        dao.save(threeAmount);
        throw new RuntimeException("Purposeful exception.");
    }

    @SessionRequired
    public void nestedTransactionalInvocaionWithException(final String amountOne, final String amountTwo) {
        nestedTransactionInvocaion(amountOne, amountTwo);
        throw new RuntimeException("Purposeful exception.");
    }

    public void singleTransactionInvocaionWithExceptionInDao(final String amountOne) {
        final EntityWithMoney oneAmount = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money(amountOne)); 
                //new EntityWithMoney("one", "first", new Money(amountOne));

        ((EntityWithMoneyDao) dao).saveWithException(oneAmount);
    }

    public void singleTransactionInvocaionWithExceptionInDao2() {
        final EntityWithMoney one = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money("0.00")); 
                //new EntityWithMoney("one", "first", new Money("0.00"));
        final EntityWithMoney two = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money("0.00")); 
                //new EntityWithMoney("one", "first", new Money("0.00"));

        ((EntityWithMoneyDao) dao).saveTwoWithException(one, two);
    }
}
