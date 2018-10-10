package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.EXISTS_BUT_NOT_ACTIVE_ERR;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class ActivatableEntitiesWithSkipeActivatableTrackingTest extends AbstractDaoTestCase {

    private static final String entityTitle = getEntityTitleAndDesc(TgCategory.class).getKey();
    
    @Test
    public void assignment_of_active_entity_to_property_with_SkipActivatableTracking_does_not_change_refCount_for_assigned_value() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        assertEquals(Integer.valueOf(0), cat1.getRefCount());
        
        
        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
        save(new_(TgSystem.class, "Sys2").setActive(true).setForthCat(cat1));
        save(new_(TgSystem.class, "Sys3").setActive(true).setCategory(cat1).setForthCat(cat1));
        assertEquals(Integer.valueOf(2), co(TgCategory.class).findByKey("Cat1").getRefCount());
    }
    
    @Test
    public void assignment_of_inactive_entity_to_property_with_SkipActivatableTracking_but_no_SkipEntityExistsValidation_with_at_least_skipActiveOnly_is_not_allowed() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(false));
        assertEquals(Integer.valueOf(0), cat1.getRefCount());
        
        final TgSystem sys = new_(TgSystem.class, "Sys").setActive(true).setForthCat(cat1);
        final MetaProperty<?> propFirthCat = sys.getProperty("forthCat");
        assertFalse(propFirthCat.isValid());
        assertEquals(propFirthCat.getFirstFailure().getMessage(), format(EXISTS_BUT_NOT_ACTIVE_ERR, entityTitle, cat1));
    }
    
    @Test
    public void deactivating_entity_referenced_by_some_SkipActivatableTracking_properties_in_active_entities_is_allowed() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgSystem.class, "Sys1").setActive(true).setForthCat(cat1));
        
        try {
            save(cat1.setActive(false));
        } catch (Exception ex) {
            fail("Saving of deactivated category should have been successful.");
        }
    }

    @Test
    public void deleting_entities_with_SkipActivatableTracking_properties_does_not_affect_refCount_of_referenced_by_them_active_entities() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        assertEquals(Integer.valueOf(0), cat1.getRefCount());
        
        final TgSystem sys1 = save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
        final TgSystem sys2 = save(new_(TgSystem.class, "Sys2").setActive(true).setForthCat(cat1));
        final TgSystem sys3 = save(new_(TgSystem.class, "Sys3").setActive(true).setCategory(cat1).setForthCat(cat1));
        
        co(TgSystem.class).delete(sys1);
        assertEquals(Integer.valueOf(1), co(TgCategory.class).findByKey("Cat1").getRefCount());
        
        co(TgSystem.class).delete(sys2);
        assertEquals(Integer.valueOf(1), co(TgCategory.class).findByKey("Cat1").getRefCount());
        
        co(TgSystem.class).delete(sys3);
        assertEquals(Integer.valueOf(0), co(TgCategory.class).findByKey("Cat1").getRefCount());
    }
 

    @Test
    public void activating_entity_referencing_inactive_values_in_properties_with_SkipActivatableTracking_is_permitted() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(false));
        assertEquals(Integer.valueOf(0), cat1.getRefCount());
        
        final TgSystem sys2 = save(new_(TgSystem.class, "Sys2").setActive(false).setForthCat(cat1));
        
        final TgSystem activatedSys2 = sys2.setActive(true);
        assertNull(activatedSys2.getProperty(ACTIVE).getFirstFailure());
        save(activatedSys2);
    }

}
