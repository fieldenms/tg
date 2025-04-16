package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertFieldDeclared;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertFieldExists;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyCorrectness;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertInstantiation;

import java.lang.reflect.Field;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.CircularChild;
import ua.com.fielden.platform.reflection.asm.impl.entities.CircularParent;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
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

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void generated_types_can_have_both_added_and_modified_properties() throws Exception {
        // a modified property
        final NewProperty<Double> np = NewProperty.fromField(EntityBeingEnhanced.class, "prop1").changeType(Double.class);

        // first modify, then add
        final Class<? extends EntityBeingEnhanced> newType1 = startModification(EntityBeingEnhanced.class)
                .modifyTypeName(EntityBeingEnhanced.class.getName() + "_modify_then_add")
                .modifyProperties(np)
                .addProperties(np1)
                .endModification();
        assertGeneratedPropertyCorrectness(np, newType1);
        assertGeneratedPropertyCorrectness(np1, newType1);
        
        // first add, then modify
        final Class<? extends EntityBeingEnhanced> newType2 = startModification(EntityBeingEnhanced.class)
                .modifyTypeName(EntityBeingEnhanced.class.getName() + "_add_then_modify")
                .addProperties(np1)
                .modifyProperties(np)
                .endModification();
        assertGeneratedPropertyCorrectness(np, newType2);
        assertGeneratedPropertyCorrectness(np1, newType2);
    }

    /**
     *        +------------prop1------------+
     *        v                             |
     * +--------------+             +---------------+
     * | CircularChild | --prop1--> | CircularParent|
     * +--------------+             +---------------+
     *        |                      ^      |
     *  add newProperty              |      |
     *        |          +--prop1----+      |
     *        |          |                  |
     *        |          |            modify prop1
     *        |          |                  |
     *        v          |                  v
     * +-------------------+            +-------------------+
     * | mod1CircularChild | <--prop1-- | modCircularParent |
     * +-------------------+            +-------------------+
     *          |                                ^
     *    modify prop1                           |
     *          v                                |
     * +-------------------+                     |
     * | mod2CircularChild | ------prop1---------+
     * +-------------------+
     * 
     * @throws Exception
     */
    @Test
    public void modification_of_entities_that_form_a_graph_cycle_succeeds() throws Exception {
        final CircularChild newCircularChild = factory.newEntity(CircularChild.class);
        final CircularParent newCircularParent = factory.newEntity(CircularParent.class);
        newCircularChild.set("prop1", newCircularParent);
        newCircularParent.set("prop1", newCircularChild);

        final Class<? extends CircularChild> mod1CircularChild = startModification(CircularChild.class)
                .addProperties(np1)
                .endModification();
        final CircularChild newMod1CircularChild = assertInstantiation(mod1CircularChild, factory);
        newMod1CircularChild.set("prop1", newCircularParent);

        // enhance(CircularParent)
        //   prop1: CircularChild -> modCircularChild
        final Class<? extends CircularParent> modCircularParent = startModification(CircularParent.class)
                .modifyProperties(NewProperty.fromField(CircularParent.class, "prop1").changeType(mod1CircularChild))
                .endModification();
        final CircularParent newModCircularParent = assertInstantiation(modCircularParent, factory);
        newModCircularParent.set("prop1", newMod1CircularChild);
        
        // enhance(mod1CircularChild)
        //   prop1: CircularParent -> modCircularParent
        final Class<? extends CircularChild> mod2CircularChild = startModification(mod1CircularChild)
                .modifyProperties(NewProperty.fromField(mod1CircularChild, "prop1").changeType(modCircularParent))
                .endModification();
        final CircularChild newMod2CircularChild = assertInstantiation(mod2CircularChild, factory);
        newMod2CircularChild.set("prop1", newModCircularParent);

        // begin assertions following the graph from the last modified type
        final Field mod2CircularChildProp1 = assertFieldDeclared(mod2CircularChild, "prop1");
        assertEquals("Incorrect type of modified property %s.%s.".formatted(mod2CircularChild.getName(), "prop1"), 
                modCircularParent, mod2CircularChildProp1.getType());
        
        final Field modCircularParentProp1 = assertFieldDeclared(modCircularParent, "prop1");
        assertEquals("Incorrect type of modified property %s.%s.".formatted(modCircularParent.getName(), "prop1"), 
                mod1CircularChild, modCircularParentProp1.getType());

        final Field mod1CircularChildProp1 = assertFieldExists(mod1CircularChild, "prop1");
        assertEquals("Incorrect type of modified property %s.%s.".formatted(mod1CircularChild.getName(), "prop1"), 
                CircularParent.class, mod1CircularChildProp1.getType());
        
        // fetch instances
        assertSame("Incorrect value of newMod2CircularChild.%s.".formatted("prop1"),
                newModCircularParent, newMod2CircularChild.get("prop1"));

        assertSame("Incorrect value of newMod2CircularChild.%s.".formatted("prop1.prop1"),
                newMod1CircularChild, newMod2CircularChild.get("prop1.prop1"));

        assertSame("Incorrect value of newMod2CircularChild.%s.".formatted("prop1.prop1.prop1"),
                newCircularParent, newMod2CircularChild.get("prop1.prop1.prop1"));

        assertSame("Incorrect value of newMod2CircularChild.%s.".formatted("prop1.prop1.prop1.prop1"),
                newCircularChild, newMod2CircularChild.get("prop1.prop1.prop1.prop1"));
    }

}
