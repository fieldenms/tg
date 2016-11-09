package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Contains tests for original / prev values handling during collectional property mutation.
 * <p>
 * Please, note that {@link MetaProperty#isChangedFromOriginal()} and {@link MetaProperty#isChangedFromPrevious()} is still based on 
 * collectional value count rather than on value itself.
 * 
 * @author TG Team
 *
 */
public class MetaPropertyCollectionalTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private TgCollectionalSerialisationParent entity;

    @Before
    public void setUp() {
        entity = factory.newEntity(TgCollectionalSerialisationParent.class, "key", "description");
    }
    
    private static MetaProperty<Set<TgCollectionalSerialisationChild>> prop(final TgCollectionalSerialisationParent entity) {
        return entity.getProperty("collProp");
    }
    
    @Test
    public void original_value_is_not_set_for_new_entity() {
        assertNull(prop(entity).getOriginalValue());
    }
    
    @Test
    public void prev_value_is_not_set_for_new_entity() {
        assertNull(prop(entity).getPrevValue());
    }
    
    @Test
    public void original_value_is_set_after_property_state_was_reset() {
        prop(entity).resetState();
        
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        assertEquals(expected, prop(entity).getOriginalValue());
        
        assertFalse(prop(entity).getValue() == prop(entity).getOriginalValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getValue(), prop(entity).getOriginalValue())); // values should be equal
    }
    
    @Test
    public void prev_value_is_set_after_property_state_was_reset() {
        prop(entity).resetState();
        
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        assertEquals(expected, prop(entity).getPrevValue());
        
        assertFalse(prop(entity).getValue() == prop(entity).getPrevValue()); // references should be different
        assertFalse(prop(entity).getOriginalValue() == prop(entity).getPrevValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getValue(), prop(entity).getPrevValue())); // values should be equal
    }
    
    @Test
    public void original_value_remains_the_same_after_property_value_was_set() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value once
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        
        assertEquals(origVal, prop(entity).getOriginalValue());
        assertFalse(origVal == prop(entity).getOriginalValue()); // references should be different
    }
    
    @Test
    public void prev_value_remains_the_same_after_property_value_was_set() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value once
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        
        assertEquals(origVal, prop(entity).getPrevValue());
        assertFalse(origVal == prop(entity).getPrevValue()); // references should be different
        assertFalse(prop(entity).getOriginalValue() == prop(entity).getPrevValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getOriginalValue(), prop(entity).getPrevValue())); // values should be equal
    }
    
    @Test
    public void original_value_remains_the_same_after_property_value_was_set_twice() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value twice
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        final Set<TgCollectionalSerialisationChild> secondVal = new HashSet<>();
        secondVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "2"));
        prop(entity).setValue(secondVal);
        
        assertEquals(origVal, prop(entity).getOriginalValue());
        assertFalse(origVal == prop(entity).getOriginalValue()); // references should be different
    }
    
    @Test
    public void prev_value_is_updated_after_property_value_was_set_twice() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value twice
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        final Set<TgCollectionalSerialisationChild> secondVal = new HashSet<>();
        secondVal.add(factory.newByKey(TgCollectionalSerialisationChild.class, entity, "2"));
        prop(entity).setValue(secondVal);
        
        assertEquals(val, prop(entity).getPrevValue());
        assertFalse(val == prop(entity).getPrevValue()); // references should be different
    }
}
