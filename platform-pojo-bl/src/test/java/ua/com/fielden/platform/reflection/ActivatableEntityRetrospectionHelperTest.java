package ua.com.fielden.platform.reflection;

import org.junit.Test;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.reflection.test_entities.EntityWithPropertiesOfActivatableTypes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableProperty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyPersistent;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

public class ActivatableEntityRetrospectionHelperTest {

    @Test
    public void isActivatableProperty_is_false_for_calculated_property_with_activatable_type() {
        assertTrue(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "calcCategory")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
    }

    @Test
    public void isActivatableProperty_is_false_for_calculated_property_with_non_activatable_type() {
        assertTrue(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "calcAuthor"));
        assertFalse(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "calcAuthor")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "calcAuthor"));
    }

    @Test
    public void isActivatableProperty_is_false_for_critOnly_property_with_activatable_type() {
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
    }

    @Test
    public void isActivatableProperty_is_false_for_critOnly_property_with_non_activatable_type() {
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "authorCrit"));
        assertFalse(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "authorCrit")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "authorCrit"));
    }

    @Test
    public void isActivatableProperty_is_false_for_plain_property_with_activatable_type() {
        assertFalse(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "plainCategory")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
    }

    @Test
    public void isActivatableProperty_is_false_for_plain_property_with_non_activatable_type() {
        assertFalse(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "plainAuthor"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "plainAuthor"));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "plainAuthor"));
        assertFalse(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "plainAuthor")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "plainAuthor"));
    }

    @Test
    public void isActivatableProperty_is_true_for_persistent_property_with_activatable_type() {
        assertTrue(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "category"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "category")));
        assertTrue(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "category"));
    }

    @Test
    public void isActivatableProperty_is_false_for_persistent_property_with_non_activatable_type() {
        assertTrue(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "author"));
        assertFalse(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "author")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "author"));
    }

}
