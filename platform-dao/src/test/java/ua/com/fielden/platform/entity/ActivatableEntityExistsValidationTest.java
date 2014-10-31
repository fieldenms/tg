package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class ActivatableEntityExistsValidationTest extends AbstractDomainDrivenTestCase {

    @Test
    public void entity_exists_validator_should_be_assigned_automatically() {
        final TgCategory newCat = new_(TgCategory.class, "NEW");
        assertNotNull(newCat);
        assertNotNull(newCat.getProperty("parent").getValidators().get(ValidationAnnotation.ENTITY_EXISTS));
    }

    @Override
    protected void populateDomain() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(cat1.setParent(cat1));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true));
        save(cat2.setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(false));

//        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
//        save(new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat2));
//        save(new_(TgSystem.class, "Sys3").setActive(false).setCategory(cat3));
//        save(new_(TgSystem.class, "Sys4").setActive(false).setCategory(cat3));
//
//        save(new_(TgSubSystem.class, "SubSys1").setActive(true).setCategory(cat1));
//        save(new_(TgSubSystem.class, "SubSys2").setActive(false).setCategory(cat1));
//        save(new_(TgSubSystem.class, "SubSys3").setActive(true).setCategory(cat2));
//        save(new_(TgSubSystem.class, "SubSys4").setActive(false).setCategory(cat3));
//        save(new_(TgSubSystem.class, "SubSys5").setActive(false).setCategory(cat3));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
