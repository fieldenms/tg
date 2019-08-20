package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationInHierarchy;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentInHierarchy;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;
import ua.com.fielden.platform.sample.domain.UnionEntity;

/**
 * Test case for {@link AnnotationReflector}.
 *
 * @author TG Team
 *
 */
public class AnnotationReflectorTest {

    @Test
    public void testGetKeyType() throws Exception {
        assertEquals("Incorrect ket type for SimpleEntity.", String.class, AnnotationReflector.getKeyType(SimpleEntity.class));
        assertEquals("Incorrect ket type for FirstLevelEntity.", DynamicEntityKey.class, AnnotationReflector.getKeyType(FirstLevelEntity.class));
        assertEquals("Incorrect ket type for SecondLevelEntity.", DynamicEntityKey.class, AnnotationReflector.getKeyType(SecondLevelEntity.class));
    }

    @Test
    public void testHierarchicalAnnotationDetermination() {
        // Class FirstLevelEntity is annotated with KeyTitle; SecondLevelEntity is not, but it is derived from First Level
        // Expect KeyTitle from FirstLevelEntity to be picked up for SecondLevel
        assertTrue("KeyTitle annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentForClass(KeyTitle.class, SecondLevelEntity.class));
        assertEquals("Incorrect title for key property in SecondLevelEntity.", "Leveled Entity No", AnnotationReflector.getAnnotation(SecondLevelEntity.class, KeyTitle.class).value());
    }

    @Test
    public void isAnnotationPresentInHierarchy_is_true_for_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty"));
    }

    @Test
    public void isAnnotationPresentInHierarchy_is_true_for_dotnotated_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_correct_annotation_for_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty").get().annotationType());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_empty_result_for_properties_without_annotations_at_different_levels_of_type_hierarchy() {
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty").isPresent());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_correct_annotation_for_dotnotated_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo").get().annotationType());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_empty_result_for_dotnotated_properties_without_annotations_at_different_levels_of_type_hierarchy() {
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty.property").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty.property").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo").isPresent());
    }

    @Test
    public void testPropertyAnnotationRetrieval() {
        // ordinary property tests
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, FirstLevelEntity.class, "property"));
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property"));
        assertEquals("Incorrect property annotation value", "Property", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property").value());
        // key property
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(KeyTitle.class, SecondLevelEntity.class, "key"));
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(KeyType.class, SecondLevelEntity.class, "key"));
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(IsProperty.class, SecondLevelEntity.class, "key"));
        assertEquals("Incorrect property annotation value", "Leveled Entity No", AnnotationReflector.getPropertyAnnotation(KeyTitle.class, SecondLevelEntity.class, "key").value());
        // dot-notated property
        // ordinary property tests
        assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.property"));
        assertEquals("Incorrect property annotation value", "Property", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.property").value());
    }

    @Test
    public void retrieval_of_annotations_for_non_existing_properties_succeeds_with_null_result() {
        assertNull(getPropertyAnnotation(Title.class, FirstLevelEntity.class, "property_that_does_not_exist"));
        assertNull(getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.sub_property_that_does_not_exist"));
        assertNull(getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property_that_does_not_exist.sub_property_that_does_not_exist"));
    }

    @Test
    public void test_isClassHasMethodAnnotatedWith() {
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(UnionEntity.class, Observable.class));
        // 3 -- for common properties, 2 -- for union entity, 2 -- AbstractUnionEntity, 2 -- AbstractEntity
        assertEquals("", 8, AnnotationReflector.getMethodsAnnotatedWith(UnionEntity.class, Optional.of(Observable.class)).size());
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(SecondLevelEntity.class, Observable.class));
        // 4 -- for SecondLevelEntity, 2 -- for FirstLevelEntity 2 - for AbstractEntity
        assertEquals("", 9, AnnotationReflector.getMethodsAnnotatedWith(SecondLevelEntity.class, Optional.of(Observable.class)).size());
    }
}
