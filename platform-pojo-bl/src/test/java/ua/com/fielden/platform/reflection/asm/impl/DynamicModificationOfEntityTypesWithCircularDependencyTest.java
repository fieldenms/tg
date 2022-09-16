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

    private final NewProperty<Money> pd = NewProperty.create(NEW_PROPERTY, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_correct_loading_of_modified_types_with_circular_dependencies() throws Exception {
        // get the enhanced CircularChild type
        final Class<? extends CircularChild> modCircularChild = cl.startModification(CircularChild.class)
                .addProperties(pd)
                .endModification();

        // enhance(CircularParent)
        //      prop1: CircularChild -> modCircularChild
        final Class<? extends CircularParent> modCircularParent = cl.startModification(CircularParent.class)
                .modifyProperties(NewProperty.fromField(CircularParent.class, "prop1").changeType(modCircularChild))
                .endModification();

        // instantiate the modified CircularParent
        final CircularParent newModCircularParent = modCircularParent.getConstructor().newInstance();
        assertNotNull("Should not be null.", newModCircularParent);

        // let's correct property type modification
        final Field modCircularParentProp1 = modCircularParent.getDeclaredField("prop1");
        assertEquals("Property prop1 in the modified class CircularParent should be of enhanced type.", 
                modCircularChild, modCircularParentProp1.getType());
        final Field modCircularChildProp1 = Finder.findFieldByName(modCircularChild, "prop1");
        assertEquals("Property prop1 in the modified class CircularChild should be of original CircularParent type.",
                CircularParent.class, modCircularChildProp1.getType());
    }

}
