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
        final TgOriginator originator = save(new_(TgOriginator.class).setActive(true).setPerson(richard));

        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");
        originator.setPerson(joe);

        final MetaProperty<String> mpPerson = originator.getProperty("person");
        assertTrue(mpPerson.isValid());
        assertFalse(mpPerson.hasWarnings());
        assertEquals(joe, originator.getPerson());
    }

    @Test
    public void warning_is_issued_upon_changing_property_key_in_persisted_entity_that_is_referenced() {
        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");

        joe.setKey("Donald");
        final MetaProperty<String> mpKey = joe.getProperty("key");
        assertTrue(mpKey.isValid());
        final Warning warning = mpKey.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(joe).getKey())));
        assertEquals("Donald", joe.getKey());
    }

    @Test
    public void warning_is_cleared_after_changing_key_back_to_original_value() {
        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");

        final MetaProperty<String> mpKey = joe.getProperty("key");

        joe.setKey("Donald"); // assign a different key
        assertEquals("Donald", joe.getKey());
        assertTrue(mpKey.isValid());
        assertTrue(mpKey.hasWarnings());

        joe.setKey("Joe"); // back to the original key value
        assertEquals("Joe", joe.getKey());
        assertTrue(mpKey.isValid());
        assertFalse(mpKey.hasWarnings());
    }

    @Test
    public void warning_is_issued_upon_changing_key_member_in_persisted_entity_that_is_referenced() {
        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");
        assertNotNull(joe);
        final TgOriginator originator = co$(TgOriginator.class).findByKey(joe);
        assertNotNull(originator);

        final TgPerson richard = save(new_(TgPerson.class, "Richard").setActive(true));
        originator.setPerson(richard);
        final MetaProperty<TgPerson> mpPerson = originator.getProperty("person");
        assertTrue(mpPerson.isValid());
        final Warning warning = mpPerson.getFirstWarning();
        assertNotNull(warning);
        assertTrue(warning.getMessage().startsWith(KEY_MEMBER_CHANGE_MESSAGE.formatted(getEntityTitleAndDesc(originator).getKey())));
        assertTrue(EntityUtils.areEqual(richard, originator.getPerson()));
    }

    @Test
    public void warning_is_cleared_after_changing_key_member_back_to_original_value() {
        final TgPerson richard = save(new_(TgPerson.class, "Richard").setActive(true));
        final TgPerson joe = co$(TgPerson.class).findByKey("Joe");
        final TgOriginator originator = co$(TgOriginator.class).findByKey(joe);

        final MetaProperty<TgPerson> mpPerson = originator.getProperty("person");

        originator.setPerson(richard); // assign a different person
        assertEquals(richard, originator.getPerson());
        assertTrue(mpPerson.isValid());
        assertTrue(mpPerson.hasWarnings());

        originator.setPerson(joe); // back to the original value, which should remove the warning
        assertEquals(joe, originator.getPerson());
        assertTrue(mpPerson.isValid());
        assertFalse(mpPerson.hasWarnings());
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
