package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Contains tests for original / prev values handling during collectional property mutation.
 * 
 * @author TG Team
 *
 */
public class MetaPropertyCollectionalTest extends AbstractDaoTestCase {
    private TgCollectionalSerialisationParent entity;
    private TgCollectionalSerialisationParent savedEntity;

    @Before
    public void setUp() {
        entity = new_(TgCollectionalSerialisationParent.class, "key", "description");
        savedEntity = co$(TgCollectionalSerialisationParent.class).findByKeyAndFetch(fetch(TgCollectionalSerialisationParent.class).with("collProp"), "saved_key");
    }
    
    private static MetaProperty<Set<TgCollectionalSerialisationChild>> prop(final TgCollectionalSerialisationParent entity) {
        return entity.getProperty("collProp");
    }
    
    @Test
    public void original_value_is_not_set_for_new_entity() {
        assertNull(prop(entity).getOriginalValue());
        assertTrue(prop(entity).isChangedFromOriginal()); // null (originalValue) is not equal to default value in entity definition which is typically empty collection like '= new LinkedHashSet<Entity>();'
        assertTrue(prop(entity).isDirty()); // because of newly created entity
    }
    
    @Test
    public void prev_value_is_not_set_for_new_entity() {
        assertNull(prop(entity).getPrevValue());
        assertTrue(prop(entity).isChangedFromPrevious()); // null (prevValue) is not equal to default value in entity definition which is typically empty collection like '= new LinkedHashSet<Entity>();'
        assertTrue(prop(entity).isDirty()); // because of newly created entity
    }
    
    @Test
    public void original_value_is_set_after_property_state_was_reset() {
        prop(entity).resetState();
        
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        assertEquals(expected, prop(entity).getOriginalValue());
        assertFalse(prop(entity).isChangedFromOriginal());
        
        assertFalse(prop(entity).getValue() == prop(entity).getOriginalValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getValue(), prop(entity).getOriginalValue())); // values should be equal
        assertTrue(prop(entity).isDirty()); // because of newly created entity
    }
    
    @Test
    public void prev_value_is_set_after_property_state_was_reset() {
        prop(entity).resetState();
        
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        assertEquals(expected, prop(entity).getPrevValue());
        assertFalse(prop(entity).isChangedFromPrevious());
        
        assertFalse(prop(entity).getValue() == prop(entity).getPrevValue()); // references should be different
        assertFalse(prop(entity).getOriginalValue() == prop(entity).getPrevValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getValue(), prop(entity).getPrevValue())); // values should be equal
        assertTrue(prop(entity).isDirty()); // because of newly created entity
    }
    
    @Test
    public void original_value_remains_the_same_after_property_value_was_set() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value once
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(new_composite(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        
        assertEquals(origVal, prop(entity).getOriginalValue());
        assertFalse(origVal == prop(entity).getOriginalValue()); // references should be different
        assertTrue(prop(entity).isChangedFromOriginal());
        assertTrue(prop(entity).isDirty());
    }
    
    @Test
    public void prev_value_remains_the_same_after_property_value_was_set() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value once
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(new_composite(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        
        assertEquals(origVal, prop(entity).getPrevValue());
        assertFalse(origVal == prop(entity).getPrevValue()); // references should be different
        assertFalse(prop(entity).getOriginalValue() == prop(entity).getPrevValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(entity).getOriginalValue(), prop(entity).getPrevValue())); // values should be equal
        assertTrue(prop(entity).isChangedFromPrevious());
        assertTrue(prop(entity).isDirty());
    }
    
    @Test
    public void original_value_remains_the_same_after_property_value_was_set_twice() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value twice
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(new_composite(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        final Set<TgCollectionalSerialisationChild> secondVal = new HashSet<>();
        secondVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "2"));
        prop(entity).setValue(secondVal);
        
        assertEquals(origVal, prop(entity).getOriginalValue());
        assertFalse(origVal == prop(entity).getOriginalValue()); // references should be different
        assertTrue(prop(entity).isChangedFromOriginal());
        assertTrue(prop(entity).isDirty());
    }
    
    @Test
    public void prev_value_is_updated_after_property_value_was_set_twice() {
        final Set<TgCollectionalSerialisationChild> origVal = new HashSet<>();
        origVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "0"));
        prop(entity).setValue(origVal);
        prop(entity).resetState();
        
        // set value twice
        final Set<TgCollectionalSerialisationChild> val = new HashSet<>();
        val.add(new_composite(TgCollectionalSerialisationChild.class, entity, "1"));
        prop(entity).setValue(val);
        final Set<TgCollectionalSerialisationChild> secondVal = new HashSet<>();
        secondVal.add(new_composite(TgCollectionalSerialisationChild.class, entity, "2"));
        prop(entity).setValue(secondVal);
        
        assertEquals(val, prop(entity).getPrevValue());
        assertFalse(val == prop(entity).getPrevValue()); // references should be different
        assertTrue(prop(entity).isChangedFromPrevious());
        assertTrue(prop(entity).isDirty());
    }
    
    @Test
    public void original_value_is_set_for_retrieved_entity() {
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        expected.add(co$(TgCollectionalSerialisationChild.class).findByKeyAndFetch(fetchAll(TgCollectionalSerialisationChild.class), savedEntity, "0"));
        
        assertEquals(expected, prop(savedEntity).getOriginalValue());
        assertFalse(prop(savedEntity).isChangedFromOriginal());
        
        assertFalse(prop(savedEntity).getValue() == prop(savedEntity).getOriginalValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(savedEntity).getValue(), prop(savedEntity).getOriginalValue())); // values should be equal
        assertFalse(prop(savedEntity).isDirty());
    }
    
    @Test
    public void prev_value_is_set_for_retrieved_entity() {
        final Set<TgCollectionalSerialisationChild> expected = new HashSet<>();
        expected.add(co$(TgCollectionalSerialisationChild.class).findByKeyAndFetch(fetchAll(TgCollectionalSerialisationChild.class), savedEntity, "0"));
        
        assertEquals(expected, prop(savedEntity).getPrevValue());
        assertFalse(prop(savedEntity).isChangedFromPrevious());
        
        assertFalse(prop(savedEntity).getValue() == prop(savedEntity).getPrevValue()); // references should be different
        assertTrue(EntityUtils.equalsEx(prop(savedEntity).getValue(), prop(savedEntity).getPrevValue())); // values should be equal
        assertFalse(prop(savedEntity).isDirty());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCollectionalSerialisationParent savedParent = save(new_(TgCollectionalSerialisationParent.class, "saved_key").setDesc("desc"));
        save(new_composite(TgCollectionalSerialisationChild.class, savedParent, "0").setDesc("desc"));
    }
}
