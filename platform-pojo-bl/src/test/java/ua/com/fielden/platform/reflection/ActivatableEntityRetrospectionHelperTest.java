package ua.com.fielden.platform.reflection;

import org.junit.Test;
import ua.com.fielden.platform.entity.activatable.test_entities.UnionOwner;
import ua.com.fielden.platform.entity.activatable.test_entities.UnionWithoutActivatableOwner;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.reflection.test_entities.ActionEntity;
import ua.com.fielden.platform.reflection.test_entities.EntityWithPropertiesOfActivatableTypes;
import ua.com.fielden.platform.reflection.test_entities.UnionWithoutActivatableActionOwner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableProperty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyPersistent;
import static ua.com.fielden.platform.utils.EntityUtils.*;

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

    /// This holds for generative entities, which are persistent and use crit-only properties to capture additional execution parameters in Entity Centres.
    ///
    @Test
    public void isActivatableProperty_is_true_for_critOnly_property_with_activatable_type_for_persistent_entities() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit")));
        assertTrue(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
    }

    @Test
    public void isActivatableProperty_is_false_for_critOnly_property_with_non_activatable_type() {
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "authorCrit"));
        assertFalse(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "authorCrit")));
        assertFalse(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "authorCrit"));
    }

    @Test
    public void isActivatableProperty_is_true_for_plain_property_with_activatable_type_for_persistent_entities() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertFalse(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertTrue(isActivatableEntityType(determinePropertyType(EntityWithPropertiesOfActivatableTypes.class, "plainCategory")));
        assertTrue(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
    }

    /// Properties of activatable entity types in action entities still require the same validation as in persistent entities.
    ///
    @Test
    public void isActivatableProperty_is_true_for_plain_property_with_activatable_type_for_action_entities() {
        assertFalse(isPersistentEntityType(ActionEntity.class));
        assertFalse(isSyntheticEntityType(ActionEntity.class));
        assertTrue(isActivatableEntityType(determinePropertyType(ActionEntity.class, "plainCategory")));
        assertTrue(isActivatableProperty(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
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

    @Test
    public void isActivatableProperty_is_true_for_persistent_union_typed_properties_in_persistent_entities() {
        assertTrue(isPersistentEntityType(UnionOwner.class));
        assertTrue(isUnionEntityType(determinePropertyType(UnionOwner.class, "union")));
        // One out of 5 union members is not activatable here.
        // At least one member should be activatable for union to be considered activatable.
        assertTrue(isActivatableProperty(UnionOwner.class, "union"));
    }

    @Test
    public void isActivatableProperty_is_true_for_persistent_union_typed_properties_in_action_entities() {
        assertFalse(isPersistentEntityType(ActionEntity.class));
        assertFalse(isSyntheticEntityType(ActionEntity.class));
        assertTrue(isUnionEntityType(determinePropertyType(ActionEntity.class, "union")));
        // One out of 5 union members is not activatable here.
        // At least one member should be activatable for union to be considered activatable.
        assertTrue(isActivatableProperty(ActionEntity.class, "union"));
    }

    @Test
    public void isActivatableProperty_is_false_for_persistent_union_typed_properties_in_persistent_entities_if_union_has_no_activatable_property() {
        assertTrue(isPersistentEntityType(UnionWithoutActivatableOwner.class));
        assertTrue(isUnionEntityType(determinePropertyType(UnionWithoutActivatableOwner.class, "union")));
        assertFalse(isActivatableProperty(UnionWithoutActivatableOwner.class, "union"));
    }

    @Test
    public void isActivatableProperty_is_false_for_persistent_union_typed_properties_in_action_entities_if_union_has_no_activatable_property() {
        assertFalse(isPersistentEntityType(UnionWithoutActivatableActionOwner.class));
        assertFalse(isSyntheticEntityType(UnionWithoutActivatableActionOwner.class));
        assertTrue(isUnionEntityType(determinePropertyType(UnionWithoutActivatableActionOwner.class, "union")));
        assertFalse(isActivatableProperty(UnionWithoutActivatableActionOwner.class, "union"));
    }

}
