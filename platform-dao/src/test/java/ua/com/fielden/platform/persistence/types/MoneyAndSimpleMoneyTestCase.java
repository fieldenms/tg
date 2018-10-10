package ua.com.fielden.platform.persistence.types;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithSimpleMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityWithMoney;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Test Hibernate interaction with {@link Money} type and instrumented entities. Also test correctness of instrumented entities being handled by XStream.
 *
 * @author TG Team
 */
public class MoneyAndSimpleMoneyTestCase extends AbstractDaoTestCase {

    @Test
    public void can_save_and_retrieve_entity_with_money() {
        final EntityWithMoney instance =new_(EntityWithMoney.class, "name", "desc");
        instance.setMoney(new Money(new BigDecimal(100000d), Currency.getInstance("USD")));
        // saving instance of MoneyClass
        final IEntityDao<EntityWithMoney> dao = co$(EntityWithMoney.class);
        dao.save(instance);
        // retrieve saved instance
        final EntityWithMoney instance2 = dao.findByKey("name");
        assertEquals(instance, instance2);
    }

    @Test
    public void retrieved_entity_with_money_is_observable() {
        final IEntityWithMoney coEntityWithMoney = co$(EntityWithMoney.class);
        final EntityWithMoney instance =  coEntityWithMoney.findByKey("aname1");
        instance.setMoney(new Money(new BigDecimal(100d), Currency.getInstance("USD")));
    }

    @Test
    public void can_save_and_retrieve_entity_with_simple_money() {
        final EntityWithSimpleMoney instance = new_(EntityWithSimpleMoney.class, "name", "desc");
        instance.setMoney(new Money(new BigDecimal(100000d), Currency.getInstance(Locale.getDefault())));
        // saving instance of MoneyClass
        final EntityWithSimpleMoneyDao dao = co$(EntityWithSimpleMoney.class);
        dao.save(instance);
        // retrieve saved instance
        final EntityWithSimpleMoney instance2 = dao.findByKey("name");

        assertEquals(instance, instance2);
        assertEquals("Incorrect number of entities with simple money.", 2, dao.getPage(0, 25).data().size());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-02-19 02:47:00"));

        save(new_ (EntityWithMoney.class, "aname1", "desc").setMoney(Money.of("20.00")));
        save(new_ (EntityWithMoney.class, "aname2", "desc").setMoney(Money.of("20.00")));
        save(new_ (EntityWithMoney.class, "bname1", "desc").setMoney(Money.of("20.00")));
        save(new_(EntityWithSimpleMoney.class, "aname1", "desc").setMoney(Money.of("20.00")));
    }

}