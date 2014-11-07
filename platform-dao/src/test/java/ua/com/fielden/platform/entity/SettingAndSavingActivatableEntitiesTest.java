package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class SettingAndSavingActivatableEntitiesTest extends AbstractDomainDrivenTestCase {


    @Test
    @Ignore
    public void active_entity_with_active_references_should_not_becove_inactive() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        cat1.setActive(false);

        final MetaProperty<Boolean> activeProperty = cat1.getProperty("active");

        assertFalse(activeProperty.isValid());
        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey();
        assertEquals(format("Entity %s has active dependencies (%s).", entityTitle, 1), activeProperty.getFirstFailure().getMessage());
    }

    @Override
    protected void populateDomain() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(cat1.setParent(cat1));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true));
        save(cat2.setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(false));

        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
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
