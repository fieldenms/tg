package ua.com.fielden.platform.entity.validation;

import org.junit.Test;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.validation.KeyMemberChangeValidator.KEY_MEMBER_CHANGE_MESSAGE;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

public class KeyMemberChangeValidationTest extends AbstractDaoTestCase {

    @Test
    public void key_change_validation_is_skipped_for_entities_annotated_with_SkipKeyChangeValidation() {
        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "ORG1"));
        final TgOrgUnit2 orgUnit2 = save(new_(TgOrgUnit2.class).setParent(orgUnit1).setName("ORG2"));

        orgUnit1.setKey(orgUnit1.getKey() + " change");

        final MetaProperty<String> mpKey = orgUnit1.getProperty("key");
        assertTrue(mpKey.isValid());
        assertFalse(mpKey.hasWarnings());
    }

    @Test
    public void no_warning_is_issued_upon_changing_property_key_for_persisted_entity_without_references() {
        final TgPerson richard = save(new_(TgPerson.class, "Richard").setActive(true));
        richard.setKey("Roberto");
        final MetaProperty<String> mpKey = richard.getProperty("key");
        assertTrue(mpKey.isValid());
        assertFalse(mpKey.hasWarnings());
        assertEquals("Roberto", richard.getKey());
    }

    @Test
    public void no_warning_is_issued_upon_changing_key_member_for_persisted_entity_without_references() {
        final TgPerson richard = save(new_(TgPerson.class, "Richard").setActive(true));
        final TgOriginator richardAsOriginator = save(new_(TgOriginator.class).setActive(true).setPerson(richard));

        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");
        richardAsOriginator.setPerson(joe);

        final MetaProperty<String> mpPerson = richardAsOriginator.getProperty("person");
        assertTrue(mpPerson.isValid());
        assertFalse(mpPerson.hasWarnings());
        assertEquals(joe, richardAsOriginator.getPerson());
    }

    @Test
    public void warning_is_issued_upon_changing_property_key_in_persisted_entity_that_is_referenced() {
        final TgPerson person1 = co$(TgPerson.class).findByKey("Joe");

        person1.setKey("Donald");
        final MetaProperty<String> mpKey = person1.getProperty("key");
        assertTrue(mpKey.isValid());
        final Warning warning = mpKey.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(person1).getKey())));
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
        final MetaProperty<TgPerson> mpPerson = originator1.getProperty("person");
        assertTrue(mpPerson.isValid());
        final Warning warning = mpPerson.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(originator1).getKey())));
        assertTrue(EntityUtils.areEqual(person2, originator1.getPerson()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final TgPerson joe = save(new_(TgPerson.class, "Joe").setActive(true));
        save(new_(TgAuthoriser.class).setActive(false).setPerson(joe));
        final TgOriginator originator1 = save(new_(TgOriginator.class).setActive(true).setPerson(joe));
        save(new_(TgOriginatorDetails.class).setOriginator(originator1).setActive(true)); // a reference to originator
    }

}
