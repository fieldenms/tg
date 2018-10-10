package ua.com.fielden.platform.entity.meta;

import static ua.com.fielden.platform.entity.meta.entities.EntityForReadOnlyTesting.NOT_EDITABLE_REASON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.entities.EntityForReadOnlyTesting;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;

/**
 *
 * This test case to validate the correct behaviour of read-only meta-data.
 *
 * @author TG Team
 *
 */
public class MetaPropertyReadonlyTestCase {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private EntityForReadOnlyTesting entity;

    @Before
    public void setUp() {
        entity = factory.newEntity(EntityForReadOnlyTesting.class, "key", "description");
    }

    @Test
    public void property_defined_as_readonly_is_recognised_as_such_by_meta_property() {
        assertFalse(entity.getProperty("readonlyIntProp").isEditable());
    }

    @Test
    public void readonly_property_can_be_changed_via_setter_for_editable_entity() {
        assertFalse(entity.getProperty("readonlyIntProp").isEditable());
        assertNull(entity.getReadonlyIntProp());
        entity.setReadonlyIntProp(42);
        assertEquals(Integer.valueOf(42), entity.getReadonlyIntProp());
    }

    @Test
    public void property_defined_not_as_readonly_becomes_readonly_if_entity_is_not_editable() {
        assertTrue(entity.getProperty("intProp").isEditable());
        entity.setEditable(false);
        assertFalse(entity.getProperty("intProp").isEditable());
    }

    @Test
    public void neither_readonly_nor_editable_property_can_be_changed_for_not_editable_entity() {
        entity.setEditable(false);
        
        assertNull(entity.getReadonlyIntProp());
        final Either<Exception, EntityForReadOnlyTesting> res1 = Try(() -> entity.setReadonlyIntProp(42));
        assertTrue(res1 instanceof Left);
        assertEquals(NOT_EDITABLE_REASON, ((Left<Exception, EntityForReadOnlyTesting>) res1).value.getMessage());
        assertNull(entity.getReadonlyIntProp());
        
        assertNull(entity.getIntProp());
        final Either<Exception, EntityForReadOnlyTesting> res2 = Try(() -> entity.setIntProp(42));
        assertTrue(res2 instanceof Left);
        assertEquals(NOT_EDITABLE_REASON, ((Left<Exception, EntityForReadOnlyTesting>) res2).value.getMessage());
        assertNull(entity.getIntProp());
    }

}
