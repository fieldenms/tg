package ua.com.fielden.platform.test.transactional;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Inject;

/**
 * A helper class for testing transactional support.
 * 
 * @author 01es
 * 
 */
class LogicThatNeedsTransaction {
    private final IEntityDao<EntityWithMoney> dao;

    @Inject
    public LogicThatNeedsTransaction(final IEntityDao<EntityWithMoney> dao) {
	this.dao = dao;
    }

    @Transactional
    public void singleTransactionInvocaion(final String amountOne, final String amountTwo) {
	final EntityWithMoney oneAmount = new EntityWithMoney("one", "first", new Money(amountOne));
	final EntityWithMoney twoAmount = new EntityWithMoney("two", "second", new Money(amountTwo));
	dao.save(oneAmount);
	dao.save(twoAmount);
    }

    @Transactional
    public void nestedTransactionInvocaion(final String amountOne, final String amountTwo) {
	singleTransactionInvocaion(amountOne, amountTwo);
    }

    @Transactional
    public void transactionalInvocaionWithException(final String amountOne, final String amountTwo) {
	singleTransactionInvocaion(amountOne, amountTwo);
	final EntityWithMoney threeAmount = new EntityWithMoney("three", "third", new Money("90.00"));
	dao.save(threeAmount);
	throw new RuntimeException("Purposeful exception.");
    }

    @Transactional
    public void nestedTransactionalInvocaionWithException(final String amountOne, final String amountTwo) {
	nestedTransactionInvocaion(amountOne, amountTwo);
	throw new RuntimeException("Purposeful exception.");
    }

    public void singleTransactionInvocaionWithExceptionInDao(final String amountOne) {
	final EntityWithMoney oneAmount = new EntityWithMoney("one", "first", new Money(amountOne));

	((EntityWithMoneyDao) dao).saveWithException(oneAmount);
    }

    public void singleTransactionInvocaionWithExceptionInDao2() {
	final EntityWithMoney one = new EntityWithMoney("one", "first", new Money("0.00"));
	final EntityWithMoney two = new EntityWithMoney("one", "first", new Money("0.00"));

	((EntityWithMoneyDao) dao).saveTwoWithException(one, two);
    }
}
