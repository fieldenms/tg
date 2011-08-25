package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.AnnotationDescriptor;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModified;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;

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
    private static final String NEW_PROPERTY_ORIGINATION = "integerProp";
    private static final String NEW_PROPERTY = "newProperty";
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private DynamicEntityClassLoader cl;

    private final NewProperty pd1 = new NewProperty(NEW_PROPERTY, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }} ));
    private final NewProperty pd2 = new NewProperty(NEW_PROPERTY + 1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }} ));

    @Before
    public void setUp() {
	cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_complex_class_loading_with_multiple_repetative_enhancements() throws Exception {
	// first enhancement
	// get the enhanced EntityBeingEnhanced type
	final Class<?> oneTimeEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd1).endModification();
	// second enhancement
	// get the enhanced EntityBeingEnhanced type
	final Class<?> twoTimesEnhancedType = cl.startModification(oneTimeEnhancedType.getName()).addProperties(pd2).endModification();

	assertEquals("Incorrect name.", EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "1", oneTimeEnhancedType.getName());
	assertEquals("Incorrect parent.", AbstractEntity.class, oneTimeEnhancedType.getSuperclass());

	assertEquals("Incorrect name.", EntityBeingEnhanced.class.getName() + "$$TgEntity2", twoTimesEnhancedType.getName());
	assertEquals("Incorrect parent.", AbstractEntity.class, twoTimesEnhancedType.getSuperclass());

	final Field field1 = Finder.findFieldByName(twoTimesEnhancedType, NEW_PROPERTY);
	assertNotNull("Property should exist.", field1);
	final Field field2 = Finder.findFieldByName(twoTimesEnhancedType, NEW_PROPERTY + 1);
	assertNotNull("Property should exist.", field2);
    }

    @Test
    public void test_sequential_multiple_enhancements_and_modification() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = //
	cl.startModification(EntityBeingEnhanced.class.getName()).//
	addProperties(pd1).//
	addProperties(pd2).//
	endModification();

	assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "2", entityBeingEnhancedEnhancedType.getName());

	// get the modified and enhanced EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = //
	    cl.startModification(EntityBeingModified.class.getName()).//
	    addProperties(pd1).//
	    modifyProperties(NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType)).//
	    endModification();

	assertEquals("Incorrect property type.", EntityBeingModified.class.getName() + DynamicTypeNamingService.APPENDIX + "4", entityBeingModifiedModifiedType.getName());

	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType = //
	    cl.startModification(TopLevelEntity.class.getName()).//
	    addProperties(pd1).//
	    addProperties(pd2).//
	    modifyProperties(NewProperty.changeType("prop1", entityBeingModifiedModifiedType)).//
	    modifyProperties(NewProperty.changeType("prop2", entityBeingModifiedModifiedType)).//
	    endModification();

	assertEquals("Incorrect property type.", TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "8", topLevelEntityModifiedType.getName());

	// create a new instance of the modified TopLevelEntity type
	final Object topLevelEntity = topLevelEntityModifiedType.newInstance();
	assertNotNull("Should not be null.", topLevelEntity);

	// let's ensure that property types are compatible -- prop2 should be compatible with prop1 as its type is a super class for type of prop1
	final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
	final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
	assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());

	// now take one of the properties from top level entity and ensure that it's type is property modified
	final Field enhancedProp = prop1.getType().getDeclaredField("prop1");
	final Field unenhancedProp = prop1.getType().getDeclaredField("prop2");
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "2", enhancedProp.getType().getName());
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName(), unenhancedProp.getType().getName());
	assertFalse("Original type should not be assignable to the enhanced type", unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
	assertFalse("Enhanced type should not be assignable to the original type", enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));
    }

}
