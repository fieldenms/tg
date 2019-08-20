package ua.com.fielden.platform.persistence.types;

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.fromString;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.junit.Test;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * Test Hibernate interaction with tax sensitive {@link Money} instances.
 *
 * @author TG Team
 */
public class TaxSensitiveMoneyDbOperationsTestCase extends AbstractDaoTestCase {

    @Test
    public void can_save_and_retrieve_entity_with_tax_money() {
        final EntityWithTaxMoney instance = new_(EntityWithTaxMoney.class, "name", "desc");
        instance.setMoney(new Money(new BigDecimal("1000"), 20, Currency.getInstance("AUD")));
        // saving instance of MoneyClass
        final IEntityDao<EntityWithTaxMoney> dao = getInstance(ICompanionObjectFinder.class).find(EntityWithTaxMoney.class);
        dao.save(instance);

        // retrieve saved instance
        final EntityWithTaxMoney instance2 = dao.findByKey("name");
        assertEquals(instance, instance2);
        assertEquals("Incorrect amount.", new BigDecimal("1000.0000"), instance2.getMoney().getAmount());
        assertEquals("Incorrect tax amount.", new BigDecimal("166.6700"), instance2.getMoney().getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("833.3300"), instance2.getMoney().getExTaxAmount());
        assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());

        final EntityWithTaxMoney instance3 = dao.findByKey("aname1");
        assertEquals("Incorrect amount.", new BigDecimal("100.0000"), instance3.getMoney().getAmount());
        assertEquals("Incorrect tax amount.", new BigDecimal("9.0900"), instance3.getMoney().getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("90.9100"), instance3.getMoney().getExTaxAmount());
        assertEquals("Incorrect tax percent.", new Integer("10"), instance3.getMoney().getTaxPercent());
    }

    @Test
    public void can_save_and_retrieve_entity_with_simple_tax_money() {
        final EntityWithSimpleTaxMoney instance = new_(EntityWithSimpleTaxMoney.class, "name", "desc");
        instance.setMoney(new Money(new BigDecimal("2222.0000"), 20, Currency.getInstance("USD"))); // USD deliberately to be different to the default currency
        // saving instance of MoneyClass
        final IEntityDao<EntityWithSimpleTaxMoney> dao = getInstance(ICompanionObjectFinder.class).find(EntityWithSimpleTaxMoney.class);
        dao.save(instance);

        // retrieve saved instance
        final EntityWithSimpleTaxMoney instance2 = dao.findByKey("name");
        assertEquals(instance, instance2);
        assertEquals("Currency should not have been persisted.", Currency.getInstance(Locale.getDefault()), instance2.getMoney().getCurrency());
        assertEquals("Incorrect amount.", new BigDecimal("2222.0000"), instance2.getMoney().getAmount());
        assertEquals("Incorrect tax amount.", new BigDecimal("370.3300"), instance2.getMoney().getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("1851.6700"), instance2.getMoney().getExTaxAmount());
        assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());
    }


    @Test
    public void can_save_and_retrieve_entity_with_ex_tax_and_tax_money() {
        final EntityWithExTaxAndTaxMoney instance = new_(EntityWithExTaxAndTaxMoney.class, "name");
        instance.setDesc("desc");
        instance.setMoney(new Money(valueOf(600000d), 20, Currency.getInstance("USD")));

        final IEntityDao<EntityWithExTaxAndTaxMoney> dao = getInstance(ICompanionObjectFinder.class).find(EntityWithExTaxAndTaxMoney.class);
        dao.save(instance);

        final EntityWithExTaxAndTaxMoney instance2 = dao.findByKey("name");
        assertEquals(instance, instance2);
        assertEquals("Currency should not have been persisted.", Currency.getInstance(Locale.getDefault()), instance2.getMoney().getCurrency());
        assertEquals("Incorrect amount.", new BigDecimal("600000.0000"), instance2.getMoney().getAmount());
        assertEquals("Incorrect tax amount.", new BigDecimal("100000.0000"), instance2.getMoney().getTaxAmount());
        assertEquals("Incorrect ex-tax amount.", new BigDecimal("500000.0000"), instance2.getMoney().getExTaxAmount());
        assertEquals("Incorrect tax percent.", new Integer("20"), instance2.getMoney().getTaxPercent());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_(EntityWithTaxMoney.class, "aname1").setMoney(new Money(new BigDecimal("100.00"), new BigDecimal("9.09"), Currency.getInstance("AUD"))));
    }

}