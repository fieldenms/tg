package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.CircularChild;
import ua.com.fielden.platform.reflection.asm.impl.entities.CircularParent;
import ua.com.fielden.platform.types.Money;

/**
 * A test case to ensure correct dynamic property modification for entity types with circular dependencies.
 * 
 * @author TG Team
 * 
 */
public class DynamicModificationOfEntityTypesWithCircularDependencyTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY = "newProperty";
    private DynamicEntityClassLoader cl;

    private final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();

    private final NewProperty pd = new NewProperty(NEW_PROPERTY, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_correct_loading_of_modified_types_with_circular_dependencies() throws Exception {
        // get the enhanced CircularChild type
        final Class<?> circularChildEnhancedType = cl.startModification(CircularChild.class).addProperties(pd).endModification();

        // specify that what property prop1@CircularParent should have its type replaced with circularChildEnhancedType
        // get the modified CircularParent type
        final Class<?> circularParentModifiedType = cl.startModification(CircularParent.class).modifyProperties(NewProperty.changeType("prop1", circularChildEnhancedType)).endModification();

        // create a new instance of the modified TopLevelEntity type
        final Object circularParentEntity = circularParentModifiedType.newInstance();
        assertNotNull("Should not be null.", circularParentEntity);

        // let's correct property type modification
        final Field circularParentProp1 = circularParentModifiedType.getDeclaredField("prop1");
        assertEquals("Property prop1 in the modified class CircularParent should be if enhanced type", circularChildEnhancedType, circularParentProp1.getType());
        final Field circularChildProp1 = Finder.findFieldByName(circularChildEnhancedType, "prop1");
        assertEquals("Property prop1 in the enhanced class CircularChild should be of not modified type CircularParent", CircularParent.class, circularChildProp1.getType());
    }

}
