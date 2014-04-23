package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.utils.Validators;

public class CountActiveDependenciesTest extends AbstractDomainDrivenTestCase {

    private final IEntityAggregatesDao coAggregates = getInstance(IEntityAggregatesDao.class);
    private final IApplicationDomainProvider domainProvider = getInstance(IApplicationDomainProvider.class);

    @Test
    public void incorrect_number_of_active_dependencies_for_non_persisted() {

        final TgCategory cat1 = new_(TgCategory.class, "Cat1");
        assertNotNull(cat1);

        final int count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat1, coAggregates);
        assertEquals(0, count);
    }


    @Test
    public void incorrect_number_of_active_dependencies_for_cat1() {

        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        assertNotNull(cat1);

        final int count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat1, coAggregates);
        assertEquals(2, count);
    }

    @Test
    public void incorrect_number_of_active_dependencies_for_cat2() {

        final TgCategory cat2 = ao(TgCategory.class).findByKey("Cat2");
        assertNotNull(cat2);

        final int count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat2, coAggregates);
        assertEquals(1, count);
    }

    @Test
    public void incorrect_number_of_active_dependencies_for_cat3() {

        final TgCategory cat3 = ao(TgCategory.class).findByKey("Cat3");
        assertNotNull(cat3);

        final int count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat3, coAggregates);
        assertEquals(0, count);
    }

    @Override
    protected void populateDomain() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(true));

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
