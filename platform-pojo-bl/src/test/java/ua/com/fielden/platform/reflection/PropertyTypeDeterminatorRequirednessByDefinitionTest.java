package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;

/**
 * Test case for determining definition-based property requiredness.
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminatorRequirednessByDefinitionTest {

    @Test
    public void composite_property_key_is_required_by_definition() {
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition(AbstractEntity.KEY, FirstLevelEntity.class));
    }

    @Test
    public void non_composite_property_key_is_required_by_definition() {
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition(AbstractEntity.KEY, Entity.class));
    }

    @Test
    public void property_with_AtRequired_annotation_is_required_by_definition() {
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition("firstProperty", Entity.class));
    }

    @Test
    public void non_key_related_property_without_AtRequired_annotation_is_not_required_by_definition() {
        assertFalse(PropertyTypeDeterminator.isRequiredByDefinition("monitoring", Entity.class));
    }

    @Test
    public void property_desc_in_type_with_DescRequired_annotation_is_required_by_definition() {
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition(AbstractEntity.DESC, Entity.class));
    }

    @Test
    public void property_desc_in_type_without_DescRequired_annotation_is_not_required_by_definition() {
        assertFalse(PropertyTypeDeterminator.isRequiredByDefinition(AbstractEntity.DESC, FirstLevelEntity.class));
    }

    @Test
    public void property_that_is_non_optional_composite_key_member_is_required_by_definition() {
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition("property", SecondLevelEntity.class));
        assertTrue(PropertyTypeDeterminator.isRequiredByDefinition("propertyTwo", SecondLevelEntity.class));
    }

    @Test
    public void property_that_is_optional_composite_key_member_is_not_required_by_definition() {
        assertFalse(PropertyTypeDeterminator.isRequiredByDefinition("anotherProperty", SecondLevelEntity.class));
    }
}
