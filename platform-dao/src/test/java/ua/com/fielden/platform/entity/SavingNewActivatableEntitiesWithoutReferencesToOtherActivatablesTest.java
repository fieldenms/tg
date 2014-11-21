package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class SavingNewActivatableEntitiesWithoutReferencesToOtherActivatablesTest extends AbstractDomainDrivenTestCase {

    @Test
    public void entity_exists_validator_should_be_assigned_automatically() {
        final TgCategory newCat = new_(TgCategory.class, "NEW");
        assertNotNull(newCat);
        assertNotNull(newCat.getProperty("parent").getValidators().get(ValidationAnnotation.ENTITY_EXISTS));
    }

    @Test
    public void new_activatable_entity_should_be_successfully_persisted() {
        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(true));
        assertEquals(Integer.valueOf(0), newCat.getRefCount());
    }

    @Override
    protected void populateDomain() {

    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
