package ua.com.fielden.platform.entity.validation;

import org.junit.Test;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.sample.domain.TgAuthoriser;
import ua.com.fielden.platform.sample.domain.TgOriginator;
import ua.com.fielden.platform.sample.domain.TgOriginatorDetails;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.validation.KeyMemberChangeValidator.KEY_MEMBER_CHANGE_MESSAGE;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

public class KeyMemberChangeValidationTest extends AbstractDaoTestCase {

    @Test
    public void warning_is_issued_upon_changing_property_key_in_persisted_entity_that_is_referenced() {
        final TgPerson person1 = co$(TgPerson.class).findByKey("Joe");

        person1.setKey("Donald");
        final MetaProperty<String> mpKey = person1.getProperty("key");
        assertTrue(mpKey.isValid());
        final Warning warning = mpKey.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(TgPerson.class).getKey())));
        assertEquals("Donald", person1.getKey());
    }

    @Test
    public void warning_is_issued_upon_changing_key_member_in_persisted_entity_that_is_referenced() {
        final TgPerson person1 = co$(TgPerson.class).findByKey("Joe");
        assertNotNull(person1);
        final TgOriginator originator1 = co$(TgOriginator.class).findByKey(person1);
        assertNotNull(originator1);

        final TgPerson person2 = save(new_(TgPerson.class, "Richard").setActive(true));
        originator1.setPerson(person2);
        MetaProperty<TgPerson> mpPerson = originator1.getProperty("person");
        assertTrue(mpPerson.isValid());
        final Warning warning = mpPerson.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(TgOriginator.class).getKey())));
        assertTrue(EntityUtils.areEqual(person2, originator1.getPerson()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final TgPerson person1 = save(new_(TgPerson.class, "Joe").setActive(true));
        save(new_(TgAuthoriser.class).setActive(false).setPerson(person1));
        final TgOriginator originator1 = save(new_(TgOriginator.class).setActive(true).setPerson(person1));
        save(new_(TgOriginatorDetails.class).setOriginator(originator1).setActive(true));
    }

}
