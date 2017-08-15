package ua.com.fielden.platform.co_finder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import org.junit.Test;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CompanionFinderTest extends AbstractDaoTestCase {

    private final ICompanionObjectFinder coFinder = getInstance(ICompanionObjectFinder.class);
    
    @Test
    public void find_returns_a_fully_fledged_companion_that_reads_entities_as_instrumented() {
        final IEntityDao<EntityWithMoney> co =  coFinder.find(EntityWithMoney.class);
        assertNotNull(co);
        
        final EntityWithMoney entity = co.findByKeyAndFetch(fetchAll(EntityWithMoney.class), "key1");
        assertNotNull(entity);
        assertTrue(entity.isInstrumented());
    }
    
    @Test
    public void findAsReader_returns_a_companion_as_reader_that_reads_entities_as_instrumented_when_second_argument_is_false() {
        final IEntityReader<EntityWithMoney> co =  coFinder.findAsReader(EntityWithMoney.class, false);
        assertNotNull(co);
        
        final EntityWithMoney entity = co.findByKeyAndFetch(fetchAll(EntityWithMoney.class), "key1");
        assertNotNull(entity);
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void findAsReader_returns_a_companion_as_reader_that_reads_entities_as_uninstrumented_when_second_argument_is_true() {
        final IEntityReader<EntityWithMoney> co =  coFinder.findAsReader(EntityWithMoney.class, true);
        assertNotNull(co);
        
        final EntityWithMoney entity = co.findByKeyAndFetch(fetchAll(EntityWithMoney.class), "key1");
        assertNotNull(entity);
        assertFalse(entity.isInstrumented());
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));
        
        save(new_(User.class, "USER_1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));
        
        save(new_(EntityWithMoney.class, "key1").setMoney(Money.of("20.00")).setDateTimeProperty(date("2009-03-01 11:00:55")).setDesc("desc"));
        save(new_(EntityWithMoney.class, "key2").setMoney(Money.of("30.00")).setDateTimeProperty(date("2009-03-01 00:00:00")).setDesc("desc"));
        save(new_(EntityWithMoney.class, "key3").setMoney(Money.of("40.00")).setDesc("desc"));
        save(new_(EntityWithMoney.class, "key4").setMoney(Money.of("50.00")).setDateTimeProperty(date("2009-03-01 10:00:00")).setDesc("desc"));
    }
}
