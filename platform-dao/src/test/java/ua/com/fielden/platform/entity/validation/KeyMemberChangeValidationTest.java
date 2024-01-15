package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgAuthoriser;
import ua.com.fielden.platform.sample.domain.TgOriginator;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestModule;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KeyMemberChangeValidationTest extends AbstractDaoTestCase {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

    @Test
    public void warning_is_issued_upon_changing_property_key_in_persisted_entity_that_is_referenced() {
        final TgPerson person1 = co$(TgPerson.class).findByKey("Joe");

        person1.setKey("Donald");
        assertNotNull(person1.getProperty("key").getFirstWarning());
        assertEquals("Donald", person1.getKey());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final TgPerson person1 = save(new_(TgPerson.class, "Joe").setActive(true));
        save(new_(TgAuthoriser.class).setActive(true).setPerson(person1));
        save(new_(TgOriginator.class).setActive(true).setPerson(person1));
    }

}
