package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.APPENDIX;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModified;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.types.Money;

/**
 * A test case to ensure correct dynamic property modification for existing entity types with the modification and enhancement applied multiple times.
 * 
 * @author TG Team
 * 
 */
public class DynamicEntityTypeMixedAndRepetitiveModificationTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY = "newProperty";
    private DynamicEntityClassLoader cl;

    private final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();

    private final NewProperty<Money> np1 = NewProperty.create(NEW_PROPERTY, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            calculated);
    private final NewProperty<Money> np2 = NewProperty.create(NEW_PROPERTY + "1", Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            calculated);

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_complex_class_loading_with_multiple_repetative_enhancements() throws Exception {
        // 1. enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> mod1EntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(np1)
                .endModification();

        // 2. enhance(mod1EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> mod2EntityBeingEnhanced = cl.startModification(mod1EntityBeingEnhanced)
                .addProperties(np2)
                .endModification();

        assertTrue("Incorrect name.", mod1EntityBeingEnhanced.getName().startsWith(EntityBeingEnhanced.class.getName() + APPENDIX + "_"));
        assertEquals("Incorrect parent.", EntityBeingEnhanced.class, mod1EntityBeingEnhanced.getSuperclass());

        assertTrue("Incorrect name.", mod2EntityBeingEnhanced.getName().startsWith(EntityBeingEnhanced.class.getName() + APPENDIX + "_"));
        assertEquals("Incorrect parent.", mod1EntityBeingEnhanced, mod2EntityBeingEnhanced.getSuperclass());

        final Field mod2Np1Field = Finder.findFieldByName(mod2EntityBeingEnhanced, np1.getName());
        assertNotNull("Property should exist.", mod2Np1Field);
        final Field mod2Np2Field = Finder.findFieldByName(mod2EntityBeingEnhanced, np2.getName());
        assertNotNull("Property should exist.", mod2Np2Field);
    }

    @Test
    public void test_sequential_multiple_enhancements_and_modification() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(np1)
                .addProperties(np2)
                .endModification();

        assertTrue("Incorrect property type.", modEntityBeingEnhanced.getName().startsWith(
                EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final Class<? extends EntityBeingModified> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .addProperties(np1)
                .modifyProperties(NewProperty.changeType("prop1", modEntityBeingEnhanced))
                .endModification();

        assertTrue("Incorrect property type.", modEntityBeingModified.getName().startsWith(
                EntityBeingModified.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final Class<? extends TopLevelEntity> modTopLevelEntity = cl.startModification(TopLevelEntity.class)
                .addProperties(np1)
                .addProperties(np2)
                .modifyProperties(NewProperty.changeType("prop1", modEntityBeingModified))
                .modifyProperties(NewProperty.changeType("prop2", modEntityBeingModified))
                .endModification();

        assertTrue("Incorrect property type.", modTopLevelEntity.getName().startsWith(
                TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        // instantiate modTopLevelEntity
        final TopLevelEntity topLevelEntity = modTopLevelEntity.getConstructor().newInstance();
        assertNotNull("Should not be null.", topLevelEntity);


        // let's ensure that property types are compatible 
        // original prop2 and prop1 are of the same type - EntityBeingModified
        final Field prop1 = Finder.getFieldByName(modTopLevelEntity, "prop1");
        final Field prop2 = Finder.getFieldByName(modTopLevelEntity, "prop2");
        assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());

        // now take one of the modified properties from the enhanced TopLevelEntity and ensure that its type is indeed modified 
        // prop1.prop1: modEntityBeingEnhanced
        final Field enhancedProp = prop1.getType().getDeclaredField("prop1");
        assertTrue("Incorrect property type.", enhancedProp.getType().getName().startsWith(
                EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        // prop1.prop2: EntityBeingEnhanced
        final Field unenhancedProp = Finder.getFieldByName(prop1.getType(), "prop2");
        assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName(), unenhancedProp.getType().getName());

        assertTrue("Original type should be assignable FROM the enhanced type.",
                unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
        assertFalse("Enhanced type should be assignable TO the original type",
                // in other words, enhanced type should NOT be assignable FROM the original type
                enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));
    }

}
