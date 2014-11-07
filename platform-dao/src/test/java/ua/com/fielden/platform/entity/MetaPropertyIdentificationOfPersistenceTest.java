package ua.com.fielden.platform.entity;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.*;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

/**
 * A test case for testing essential {@link MetaProperty} functionality from perspective of a persisted entity.
 *
 * @author TG Team
 *
 */
public class MetaPropertyIdentificationOfPersistenceTest extends AbstractDomainDrivenTestCase {

    @Test
    public void identification_of_retrievable_properties_for_non_composite_entity_with_non_persistent_props_other_than_desc() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        assertNull( cat1.getProperty(ID));
        assertNull( cat1.getProperty(VERSION));


        final List<MetaProperty<?>> retrievableProps = cat1.getProperties().values().stream().
                filter(p -> p.isRetrievable()).collect(Collectors.toList());

        assertEquals(5, retrievableProps.size());

        final Set<String> names = retrievableProps.stream().map(p -> p.getName()).collect(Collectors.toSet());
        assertTrue(names.contains(KEY));
        assertTrue(names.contains(DESC));
        assertTrue(names.contains(ACTIVE));
        assertTrue(names.contains(REF_COUNT));
        assertTrue(names.contains("parent"));
    }

    @Test
    public void identification_of_retrievable_properties_for_composite_entity_with_non_persistent_desc() {
        final TgOrgUnit2 cat1 = ao(TgOrgUnit2.class).findByKey(ao(TgOrgUnit1.class).findByKey("Org1"), "Org1_1");
        assertNull( cat1.getProperty(ID));
        assertNull( cat1.getProperty(VERSION));


        final List<MetaProperty<?>> retrievableProps = cat1.getProperties().values().stream().
                filter(p -> p.isRetrievable()).collect(Collectors.toList());

        assertEquals(2, retrievableProps.size());

        final Set<String> names = retrievableProps.stream().map(p -> p.getName()).collect(Collectors.toSet());
        assertTrue(names.contains("name"));
        assertTrue(names.contains("parent"));
    }

    @Override
    protected void populateDomain() {
        save(new_(TgCategory.class, "Cat1").setActive(true));
        final TgOrgUnit1 org1 = save(new_(TgOrgUnit1.class, "Org1"));
        save(new_composite(TgOrgUnit2.class, org1, "Org1_1"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}