package ua.com.fielden.platform.persistence.types;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.types.Money;
import static java.math.BigDecimal.valueOf;


/**
 * Test Hibernate interaction with tax sensitive {@link Money} instances.
 *
 * @author 01es
 */
public class TaxSensitiveMoneyDbOperationsTestCase extends DbDrivenTestCase {

    @SuppressWarnings("unchecked")
    public void testThatCanSaveAndRetrieveEntityWithTaxMoney() {
	final EntityWithTaxMoney instance = entityFactory.newEntity(EntityWithTaxMoney.class, "name", "desc");
	instance.setMoney(new Money(new BigDecimal("1000"), 20, Currency.getInstance("AUD")));
	// saving instance of MoneyClass
	final IEntityDao dao = injector.getInstance(DaoFactory.class).newDao(EntityWithTaxMoney.class);
	dao.save(instance);

	hibernateUtil.getSessionFactory().getCurrentSession().flush();
	hibernateUtil.getSessionFactory().getCurrentSession().clear();

	// retrieve saved instance
	final EntityWithTaxMoney instance2 = (EntityWithTaxMoney) dao.findByKey("name");
	assertEquals(instance, instance2);
	assertEquals("Incorrect amount.", new BigDecimal("1000.0000"), instance2.getMoney().getAmount());
	assertEquals("Incorrect tax amount.", new BigDecimal("166.6667"), instance2.getMoney().getTaxAmount());
	assertEquals("Incorrect ex-tax amount.", new BigDecimal("833.3333"), instance2.getMoney().getExTaxAmount());
	assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());

	final EntityWithTaxMoney instance3 = (EntityWithTaxMoney) dao.findByKey("aname1");
	assertEquals("Incorrect amount.", new BigDecimal("100.0000"), instance3.getMoney().getAmount());
	assertEquals("Incorrect tax amount.", new BigDecimal("9.0909"), instance3.getMoney().getTaxAmount());
	assertEquals("Incorrect ex-tax amount.", new BigDecimal("90.9091"), instance3.getMoney().getExTaxAmount());
	assertEquals("Incorrect tax percent.", new Integer("10"), instance3.getMoney().getTaxPercent());
    }

    @SuppressWarnings("unchecked")
    public void testThatCanSaveAndRetrieveEntityWithSimpleTaxMoney() {
	final EntityWithSimpleTaxMoney instance = entityFactory.newEntity(EntityWithSimpleTaxMoney.class, "name", "desc");
	instance.setMoney(new Money(new BigDecimal("2222.0000"), 20, Currency.getInstance("USD"))); // USD deliberately to be different to the default currency
	// saving instance of MoneyClass
	final IEntityDao dao = injector.getInstance(DaoFactory.class).newDao(EntityWithSimpleTaxMoney.class);
	dao.save(instance);

	hibernateUtil.getSessionFactory().getCurrentSession().flush();
	hibernateUtil.getSessionFactory().getCurrentSession().clear();

	// retrieve saved instance
	final EntityWithSimpleTaxMoney instance2 = (EntityWithSimpleTaxMoney) dao.findByKey("name");
	assertEquals(instance, instance2);
	assertEquals("Currency should not have been persisted.", Currency.getInstance(Locale.getDefault()), instance2.getMoney().getCurrency());
	assertEquals("Incorrect amount.", new BigDecimal("2222.0000"), instance2.getMoney().getAmount());
	assertEquals("Incorrect tax amount.", new BigDecimal("370.3333"), instance2.getMoney().getTaxAmount());
	assertEquals("Incorrect ex-tax amount.", new BigDecimal("1851.6667"), instance2.getMoney().getExTaxAmount());
	assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());
    }

    @SuppressWarnings("unchecked")
    public void testThatCanSaveAndRetrieveEntityWithExTaxAndTaxMoney() {
	final EntityWithExTaxAndTaxMoney instance = entityFactory.newByKey(EntityWithExTaxAndTaxMoney.class, "name");
	instance.setDesc("desc");
	instance.setMoney(new Money(valueOf(600000d), 20, Currency.getInstance("USD")));

	final IEntityDao dao = injector.getInstance(DaoFactory.class).newDao(EntityWithExTaxAndTaxMoney.class);
	dao.save(instance);

	hibernateUtil.getSessionFactory().getCurrentSession().flush();
	hibernateUtil.getSessionFactory().getCurrentSession().clear();

	final EntityWithExTaxAndTaxMoney instance2 = (EntityWithExTaxAndTaxMoney) dao.findByKey("name");
	assertEquals(instance, instance2);
	assertEquals("Currency should not have been persisted.", Currency.getInstance(Locale.getDefault()), instance2.getMoney().getCurrency());
	assertEquals("Incorrect amount.", new BigDecimal("600000.0000"), instance2.getMoney().getAmount());
	assertEquals("Incorrect tax amount.", new BigDecimal("100000.0000"), instance2.getMoney().getTaxAmount());
	assertEquals("Incorrect ex-tax amount.", new BigDecimal("500000.0000"), instance2.getMoney().getExTaxAmount());
	assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/money-with-tax-amount-user-type-test-case.flat.xml" };
    }
}