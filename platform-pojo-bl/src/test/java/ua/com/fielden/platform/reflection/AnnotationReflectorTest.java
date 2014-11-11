package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.union.UnionEntity;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;

/**
 * Test case for {@link AnnotationReflector}.
 *
 * @author TG Team
 *
 */
public class AnnotationReflectorTest {
    @Test
    public void testGetValidationAnnotations() throws Exception {
        final Method mutatorForProperty = Reflector.getMethod(SimpleEntity.class, "setProperty", String.class);
        assertNotNull("Could not find mutator for property 'property'", mutatorForProperty);
        assertEquals("Incorrect number for validation annotations.", 1, AnnotationReflector.getValidationAnnotations(mutatorForProperty).size());

        final Method mutatorForPropertyTwo = Reflector.getMethod(SimpleEntity.class, "setPropertyTwo", String.class);
        assertNotNull("Could not find mutator for property 'propertyTwo'", mutatorForProperty);
        assertEquals("Incorrect number for validation annotations.", 0, AnnotationReflector.getValidationAnnotations(mutatorForPropertyTwo).size());
    }

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
    public void testHierarchicalPropertyAnnotationDetermination() {
        // Property "propertyTwo" in FirstLevelEntity is annotated with @CritOnly; SecondLevelEntity is not, but it is derived from FirstLevelEntity
        // Expect CritOnly from FirstLevelEntity to be picked up for SecondLevel
        assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo"));
        assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo"));
        assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty"));
        assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty"));
        assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
        assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
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
    public void test_isClassHasMethodAnnotatedWith() {
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(UnionEntity.class, Observable.class));
        // 3 -- for common properties, 2 -- for union entity, 2 -- AbstractUnionEntity, 2 -- AbstractEntity
        assertEquals("", 8, AnnotationReflector.getMethodsAnnotatedWith(UnionEntity.class, Observable.class).size());
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(SecondLevelEntity.class, Observable.class));
        // 4 -- for SecondLevelEntity, 2 -- for FirstLevelEntity 2 - for AbstractEntity
        assertEquals("", 9, AnnotationReflector.getMethodsAnnotatedWith(SecondLevelEntity.class, Observable.class).size());
    }
}
