package ua.com.fielden.platform.web.utils;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMetaProperty;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyAssignmentErrorHandler;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities.INVISIBLE_VALUE;

/// Values that a client supplies for invisible properties are not applied to an entity by [EntityResourceUtils#apply].
///
public class EntityResourceUtilsInvisiblePropertyTest {

    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final FactoryForTestingEntities factory = new FactoryForTestingEntities(injector.getInstance(EntityFactory.class), new Date());

    @Test
    public void value_for_a_touched_invisible_property_is_not_applied() {
        final EntityWithMetaProperty entity = factory.createEntityWithInvisibleProps(1L);

        final Map<String, Object> holder = new LinkedHashMap<>();
        holder.put("version", 0);
        holder.put("invisibleProp", modified("Supplied", INVISIBLE_VALUE));
        holder.put("invisibleInCentresProp", modified("Changed", "Ok"));
        holder.put("@@touchedProps", List.of("invisibleProp", "invisibleInCentresProp"));

        EntityResourceUtils.apply(holder, entity, PropertyAssignmentErrorHandler.standard, null);

        assertEquals(INVISIBLE_VALUE, entity.getInvisibleProp());
        assertFalse(entity.getProperty("invisibleProp").isDirty());
        assertEquals("Changed", entity.getInvisibleInCentresProp());
    }

    @Test
    public void value_for_an_untouched_invisible_property_is_not_applied() {
        final EntityWithMetaProperty entity = factory.createEntityWithInvisibleProps(1L);

        final Map<String, Object> holder = new LinkedHashMap<>();
        holder.put("version", 0);
        holder.put("invisibleProp", modified("Supplied", INVISIBLE_VALUE));
        holder.put("invisibleInCentresProp", modified("Changed", "Ok"));
        holder.put("@@touchedProps", List.of());

        EntityResourceUtils.apply(holder, entity, PropertyAssignmentErrorHandler.standard, null);

        assertEquals(INVISIBLE_VALUE, entity.getInvisibleProp());
        assertFalse(entity.getProperty("invisibleProp").isDirty());
        assertEquals("Changed", entity.getInvisibleInCentresProp());
    }

    @Test
    public void unmodified_value_for_a_touched_invisible_property_is_not_applied() {
        final EntityWithMetaProperty entity = factory.createEntityWithInvisibleProps(1L);

        final Map<String, Object> holder = new LinkedHashMap<>();
        holder.put("version", 0);
        holder.put("invisibleProp", unmodified("Supplied"));
        holder.put("@@touchedProps", List.of("invisibleProp"));

        EntityResourceUtils.apply(holder, entity, PropertyAssignmentErrorHandler.standard, null);

        assertEquals(INVISIBLE_VALUE, entity.getInvisibleProp());
        assertFalse(entity.getProperty("invisibleProp").isDirty());
    }

    private static Map<String, Object> modified(final Object val, final Object origVal) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("val", val);
        map.put("origVal", origVal);
        return map;
    }

    private static Map<String, Object> unmodified(final Object origVal) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("origVal", origVal);
        return map;
    }

}
