package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case for mutation rules of properties that are defined as <code>@Final(persistentOnly = true)</code>.
 * 
 * @author TG Team
 *
 */
public class FinalPersistentOnlyPropsValidationTest extends AbstractDaoTestCase {

    @Test
    public void assigned_and_persisted_final_properties_cannot_be_changed() {
        final TgCategory catBeforChanged = co$(TgCategory.class).findByKey("Cat1");
        assertTrue(catBeforChanged.getProperty("finalProp").isEditable());
        
        catBeforChanged.setFinalProp(42);
        assertTrue(catBeforChanged.getProperty("finalProp").isEditable());
        assertEquals(Integer.valueOf(42), catBeforChanged.getFinalProp());
        
        final TgCategory cat = save(catBeforChanged);
        assertTrue(cat.getProperty("finalProp").isValid());
        assertFalse(cat.getProperty("finalProp").isEditable());
        assertEquals(Integer.valueOf(42), cat.getFinalProp());

        cat.setFinalProp(43);
        assertFalse(cat.getProperty("finalProp").isValid());
        assertEquals(Integer.valueOf(42), cat.getFinalProp());
    }

    @Test
    public void unassigned_final_properties_of_persisted_entities_can_be_assigned_and_changed_multiple_times_before_saving() {
        final TgCategory cat = co$(TgCategory.class).findByKey("Cat1");
        assertNull(cat.getFinalProp());
        assertTrue(cat.getProperty("finalProp").isEditable());
        assertTrue(cat.getProperty("finalProp").isValid());
        
        cat.setFinalProp(43);
        assertTrue(cat.getProperty("finalProp").isValid());
        assertTrue(cat.getProperty("finalProp").isEditable());
        assertEquals(Integer.valueOf(43), cat.getFinalProp());

        cat.setFinalProp(42);
        assertTrue(cat.getProperty("finalProp").isValid());
        assertEquals(Integer.valueOf(42), cat.getFinalProp());
        assertTrue(cat.getProperty("finalProp").isEditable());
    }

    @Test
    public void final_properties_for_not_persisted_entities_can_be_changed_multiple_times() {
        final TgCategory cat = new_(TgCategory.class, "Cat2");
        
        assertTrue(cat.getProperty("finalProp").isValid());
        assertTrue(cat.getProperty("finalProp").isEditable());
        cat.setFinalProp(43);
        assertTrue(cat.getProperty("finalProp").isValid());
        assertEquals(Integer.valueOf(43), cat.getFinalProp());
        assertTrue(cat.getProperty("finalProp").isEditable());

        cat.setFinalProp(42);
        assertTrue(cat.getProperty("finalProp").isValid());
        assertEquals(Integer.valueOf(42), cat.getFinalProp());
        assertTrue(cat.getProperty("finalProp").isEditable());
    }

    @Test
    public void immediately_final_prooperty_becomes_non_editable_immediately_upon_assignment_for_a_persisted_entity() {
        final TgCategory catBeforChanged = co$(TgCategory.class).findByKey("Cat1");
        assertTrue(catBeforChanged.getProperty("immediatelyFinalProp").isEditable());
        assertNull(catBeforChanged.getImmediatelyFinalProp());
        
        catBeforChanged.setImmediatelyFinalProp(42);
        assertFalse(catBeforChanged.getProperty("immediatelyFinalProp").isEditable());
        assertEquals(Integer.valueOf(42), catBeforChanged.getImmediatelyFinalProp());
        
        catBeforChanged.setImmediatelyFinalProp(43);
        assertFalse(catBeforChanged.getProperty("immediatelyFinalProp").isValid());
        assertEquals(Integer.valueOf(42), catBeforChanged.getImmediatelyFinalProp());
    }
    
    @Test
    public void immediately_final_prooperty_becomes_non_editable_immediately_upon_assignment_for_a_new_entity() {
        final TgCategory newCat = new_(TgCategory.class, "Cat1");
        assertTrue(newCat.getProperty("immediatelyFinalProp").isEditable());
        assertNull(newCat.getImmediatelyFinalProp());
        
        newCat.setImmediatelyFinalProp(42);
        assertFalse(newCat.getProperty("immediatelyFinalProp").isEditable());
        assertEquals(Integer.valueOf(42), newCat.getImmediatelyFinalProp());
        
        newCat.setImmediatelyFinalProp(43);
        assertFalse(newCat.getProperty("immediatelyFinalProp").isValid());
        assertEquals(Integer.valueOf(42), newCat.getImmediatelyFinalProp());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(TgCategory.class, "Cat1").setActive(true));
    }

}
