package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

public class CountActiveDependenciesTest extends AbstractDaoTestCase {

    private final IEntityAggregatesDao coAggregates = getInstance(IEntityAggregatesDao.class);
    private final IApplicationDomainProvider domainProvider = getInstance(IApplicationDomainProvider.class);

    @Test
    public void there_should_be_no_dependencies_on_not_persisted_entity() {
        final TgCategory cat1 = new_(TgCategory.class, "NEW");
        assertNotNull(cat1);

        final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat1, coAggregates);
        assertEquals(0, count);
    }

    @Test
    public void incorrect_number_of_active_dependencies_for_cat1() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        assertNotNull(cat1);

        final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat1, coAggregates);
        assertEquals(3, count);
    }

    @Test
    public void incorrect_number_of_active_dependencies_for_cat2() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        assertNotNull(cat2);

        final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat2, coAggregates);
        assertEquals(1, count);
    }

    @Test
    public void cat3_is_referenced_twice_by_the_same_active_entity_which_should_be_counted_as_one_dependency() {
        final TgCategory cat3 = co(TgCategory.class).findByKey("Cat3");
        assertNotNull(cat3);

        final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), cat3, coAggregates);
        assertEquals(1, count);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        cat1 = save(cat1.setParent(cat1));
        TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true));
        cat2 = save(cat2.setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(true));

        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
        save(new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat2));
        save(new_(TgSystem.class, "Sys3").setActive(false).setCategory(cat3));
        save(new_(TgSystem.class, "Sys4").setActive(false).setCategory(cat3));

        save(new_(TgSystem.class, "Sys5").setActive(true).setCategory(cat1).setFirstCategory(cat3).setSecondCategory(cat3));
        save(new_(TgSystem.class, "Sys6").setActive(false).setCategory(cat1));
        save(new_(TgSystem.class, "Sys7").setActive(true).setCategory(cat2));
        save(new_(TgSystem.class, "Sys8").setActive(false).setCategory(cat3));
        save(new_(TgSystem.class, "Sys9").setActive(false).setCategory(cat3));
    }

}
