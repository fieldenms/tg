package ua.com.fielden.platform.test.transactional;

import ua.com.fielden.platform.dao.EntityWithMoneyDao2;
import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Inject;

/**
 * A helper class for testing transactional support.
 *
 * @author 01es
 *
 */
class LogicThatNeedsTransaction2 {
    private final IEntityDao2<EntityWithMoney> dao;

    @Inject
    public LogicThatNeedsTransaction2(final IEntityDao2<EntityWithMoney> dao) {
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

	((EntityWithMoneyDao2) dao).saveWithException(oneAmount);
    }

    public void singleTransactionInvocaionWithExceptionInDao2() {
	final EntityWithMoney one = new EntityWithMoney("one", "first", new Money("0.00"));
	final EntityWithMoney two = new EntityWithMoney("one", "first", new Money("0.00"));

	((EntityWithMoneyDao2) dao).saveTwoWithException(one, two);
    }
}
