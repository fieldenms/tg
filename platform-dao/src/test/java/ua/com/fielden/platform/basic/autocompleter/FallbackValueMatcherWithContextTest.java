package ua.com.fielden.platform.basic.autocompleter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A set of basic unit tests for {@link FallbackValueMatcherWithContext}.
 * 
 * @author TG Team
 *
 */
public class FallbackValueMatcherWithContextTest extends AbstractDaoTestCase {

    @Test
    public void only_active_values_are_matched_when_activeOnly_argument_is_true() {
        final FallbackValueMatcherWithContext<TgCategory, TgCategory> matcher = new FallbackValueMatcherWithContext<>(co(TgCategory.class), true);
        
        final List<TgCategory> result = matcher.findMatches("CAT%");
        assertEquals(2, result.size());
        assertEquals("CAT1", result.get(0).getKey());
        assertEquals("CAT3", result.get(1).getKey());
    }

    @Test
    public void both_active_and_inactive_values_are_matched_when_activeOnly_argument_is_false() {
        final FallbackValueMatcherWithContext<TgCategory, TgCategory> matcher = new FallbackValueMatcherWithContext<>(co(TgCategory.class), false);
        
        final List<TgCategory> result = matcher.findMatches("CAT%");
        assertEquals(4, result.size());
        assertEquals("CAT1", result.get(0).getKey());
        assertEquals("CAT2", result.get(1).getKey());
        assertEquals("CAT3", result.get(2).getKey());
        assertEquals("CAT4", result.get(3).getKey());
    }

    @Test(expected = EntityException.class)
    public void matching_activeOnly_for_non_activatable_entities_is_not_permitted() {
        new FallbackValueMatcherWithContext<>(co(EntityWithMoney.class), true);
    }

    @Test
    public void matching_by_description_is_supported() {
        final FallbackValueMatcherWithContext<EntityWithMoney, EntityWithMoney> matcher = new FallbackValueMatcherWithContext<>(co(EntityWithMoney.class), false);
        
        final List<EntityWithMoney> result = matcher.findMatches("desc%");
        assertEquals(2, result.size());
        assertEquals("KEY1", result.get(0).getKey());
        assertEquals("KEY2", result.get(1).getKey());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));

        save(new_(User.class, "USER_1").setBase(true));
        
        save(new_(TgCategory.class, "CAT1").setActive(true));
        save(new_(TgCategory.class, "CAT2").setActive(false));
        save(new_(TgCategory.class, "CAT3").setActive(true));
        save(new_(TgCategory.class, "CAT4").setActive(false));
        save(new_(TgCategory.class, "BAT1").setActive(true));
        save(new_(TgCategory.class, "BAT2").setActive(false));
        
        save(new_(EntityWithMoney.class, "KEY1", "desc for KEY1").setMoney(new Money("20.00")).setDateTimeProperty(date("2017-06-01 11:00:55")));
        save(new_(EntityWithMoney.class, "KEY2", "desc for KEY2").setMoney(new Money("42.00")).setDateTimeProperty(date("2017-06-01 11:00:55")));
    }

}
