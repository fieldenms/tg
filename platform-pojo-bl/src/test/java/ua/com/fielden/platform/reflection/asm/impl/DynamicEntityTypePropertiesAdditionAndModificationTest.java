package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyCorrectness;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.types.Money;

/**
 * A test case to ensure correct dynamic entity type modification by both adding new properties and modifying existing ones.
 * 
 * @author TG Team
 * 
 */
public class DynamicEntityTypePropertiesAdditionAndModificationTest {
    private static final Calculated calculated = new CalculatedAnnotation().contextualExpression("2 * 3 - [integerProp]").newInstance();
    private static final NewProperty<Money> np1 = NewProperty.create("newTestProperty", Money.class, "New test property",
            "New test property description", calculated);

    private DynamicEntityClassLoader cl;

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void generated_types_can_have_both_added_and_modified_properties() throws Exception {
        // a modified property
        final NewProperty<Double> np = NewProperty.fromField(EntityBeingEnhanced.class, "prop1").changeType(Double.class);

        // first modify, then add
        final Class<? extends EntityBeingEnhanced> newType1 = cl.startModification(EntityBeingEnhanced.class)
                .modifyTypeName(EntityBeingEnhanced.class.getName() + "_modify_then_add")
                .modifyProperties(np)
                .addProperties(np1)
                .endModification();
        assertGeneratedPropertyCorrectness(np, newType1);
        assertGeneratedPropertyCorrectness(np1, newType1);
        
        // first add, then modify
        final Class<? extends EntityBeingEnhanced> newType2 = cl.startModification(EntityBeingEnhanced.class)
                .modifyTypeName(EntityBeingEnhanced.class.getName() + "_add_then_modify")
                .addProperties(np1)
                .modifyProperties(np)
                .endModification();
        assertGeneratedPropertyCorrectness(np, newType2);
        assertGeneratedPropertyCorrectness(np1, newType2);
    }

}
