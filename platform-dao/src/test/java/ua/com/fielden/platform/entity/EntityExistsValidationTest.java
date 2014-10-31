package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.utils.Validators;

public class EntityExistsValidationTest extends AbstractDomainDrivenTestCase {

    private final IEntityAggregatesDao coAggregates = getInstance(IEntityAggregatesDao.class);
    private final IApplicationDomainProvider domainProvider = getInstance(IApplicationDomainProvider.class);

    @Test
    @Ignore
    public void only_existing_non_activatable_entity_should_be_acceptable() {
        final TgCategory cat1 = new_(TgCategory.class, "NEW");
        assertNotNull(cat1);

        final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat1, coAggregates);
        assertEquals(0, count);
    }

    @Override
    protected void populateDomain() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(cat1.setParent(cat1));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true));
        save(cat2.setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(false));

        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
        save(new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat2));
        save(new_(TgSystem.class, "Sys3").setActive(false).setCategory(cat3));
        save(new_(TgSystem.class, "Sys4").setActive(false).setCategory(cat3));

        save(new_(TgSubSystem.class, "SubSys1").setActive(true).setCategory(cat1));
        save(new_(TgSubSystem.class, "SubSys2").setActive(false).setCategory(cat1));
        save(new_(TgSubSystem.class, "SubSys3").setActive(true).setCategory(cat2));
        save(new_(TgSubSystem.class, "SubSys4").setActive(false).setCategory(cat3));
        save(new_(TgSubSystem.class, "SubSys5").setActive(false).setCategory(cat3));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
