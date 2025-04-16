package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMap;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMappedOrCalculated;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isNumeric;

import java.lang.ref.Reference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.reflection.test_entities.ComplexKeyEntity;
import ua.com.fielden.platform.reflection.test_entities.EntityWithCollection;
import ua.com.fielden.platform.reflection.test_entities.EntityWithMap;
import ua.com.fielden.platform.reflection.test_entities.EntityWithNumericProps;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.KeyEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityForReflector;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityReference;
import ua.com.fielden.platform.utils.Pair;

/**
 * Test case for {@link PropertyTypeDeterminator}.
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminatorTest {

    @Test
    public void testDeterminePropertyType() {
        assertEquals(Comparable.class, PropertyTypeDeterminator.determineClass(SecondLevelEntity.class, "key", false, false));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "key"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "desc"));
        assertEquals(Long.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "anotherProperty"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "property"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyTwo"));
        assertEquals(SecondLevelEntity.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType"));

        // testing dot-notated property names
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.key"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.desc"));
        assertEquals(Long.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.anotherProperty"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.property"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.propertyTwo"));
        assertEquals(SecondLevelEntity.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType"));

        // testing returning of Object class on non-existent property
        try {
            PropertyTypeDeterminator.determinePropertyType(FirstLevelEntity.class, "propertyOfSelfType");
            fail("There is no propertyOfSelfType property in the FirstLevelEntity class");
            PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.nonExistingProperty.anotherProperty");
            fail("There is no propertyOfSelfType.nonExistingProperty.anotherProperty property in the SecondLevelEntity class");
        } catch (final ReflectionException e) {
        } catch (final Exception e) {
            fail("There shouldn't be any other exception but IllegalArgumentException.");
        }

        // testing determination of no-parameter function type
        assertEquals(int.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.property.length()"));
        assertEquals(int.class, PropertyTypeDeterminator.determinePropertyType(FirstLevelEntity.class, "property.length()"));
    }

    @Test
    public void test_that_determinePropertyType_works_for_union_entities() {
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyType(UnionEntityForReflector.class, "levelEntity.key"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyType(UnionEntityForReflector.class, "levelEntity.getKey()"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(UnionEntityForReflector.class, "key"));
        assertEquals(int.class, PropertyTypeDeterminator.determinePropertyType(UnionEntityForReflector.class, "levelEntity.propertyOfSelfType.property.length()"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(UnionEntityForReflector.class, "levelEntity.propertyOfSelfType.property"));
    }

    @Test
    public void testDeterminePropertyTypeWithCorrectTypeParameters() {
        Type type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "dummyReferenceProperty");
        assertEquals(Reference.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(SecondLevelEntity.class, ((ParameterizedType) type).getActualTypeArguments()[0]);

        // testing that "key"/"getKey()" returns correct type. No parameterization could be used for "key" property!
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "key"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "propertyOfSelfType.key"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.key"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "getKey()"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "propertyOfSelfType.getKey()"));
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.getKey()"));

        type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(Entity.class, "propertyDescriptor");
        assertEquals(PropertyDescriptor.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(Entity.class, ((ParameterizedType) type).getActualTypeArguments()[0]);

        type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(Entity.class, "getPropertyDescriptor()");
        assertEquals(PropertyDescriptor.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(Entity.class, ((ParameterizedType) type).getActualTypeArguments()[0]);
    }

    @Test
    public void test_that_determinePropertyTypeWithCorrectTypeParameters_works_for_union_entities() {
        Type type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(UnionEntityReference.class, "referenceProperty");
        assertEquals(Reference.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(String.class, ((ParameterizedType) type).getActualTypeArguments()[0]);
        type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(UnionEntityReference.class, "firstReference.firstProperty");
        assertEquals(Reference.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(String.class, ((ParameterizedType) type).getActualTypeArguments()[0]);
        type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(UnionEntityReference.class, "getReferenceProperty()");
        assertEquals(Reference.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(String.class, ((ParameterizedType) type).getActualTypeArguments()[0]);
        type = PropertyTypeDeterminator.determinePropertyTypeWithCorrectTypeParameters(UnionEntityReference.class, "firstReference.getFirstProperty()");
        assertEquals(Reference.class, ((ParameterizedType) type).getRawType());
        assertEquals(1, ((ParameterizedType) type).getActualTypeArguments().length);
        assertEquals(String.class, ((ParameterizedType) type).getActualTypeArguments()[0]);
    }

    @Test
    public void test_determinePropertyType_for_getKey_method() {
        assertEquals(DynamicEntityKey.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.getKey()"));
        assertEquals(KeyEntity.class, PropertyTypeDeterminator.determinePropertyType(ComplexKeyEntity.class, "getKey()"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(ComplexKeyEntity.class, "getKey().getKey()"));
        assertEquals(SimpleEntity.class, PropertyTypeDeterminator.determinePropertyType(ComplexKeyEntity.class, "getKey().getSimpleEntity()"));
        assertEquals(String.class, PropertyTypeDeterminator.determinePropertyType(ComplexKeyEntity.class, "getKey().getSimpleEntity().getKey()"));
    }

    @Test
    public void should_be_able_to_determine_type_for_property_specified_as_this_keyword() {
        assertEquals(SecondLevelEntity.class, PropertyTypeDeterminator.determinePropertyType(SecondLevelEntity.class, "this"));
    }

    @Test
    public void test_isDotExpression() {
        assertTrue("proerty1.property2.property3 should be dot-notation", PropertyTypeDeterminator.isDotExpression("proerty1.property2.property3"));
        assertTrue("getProperty1().property2.getProperty3() should also be a dot-notation", PropertyTypeDeterminator.isDotExpression("getProperty1().property2.getProperty3()"));
        assertFalse("getProperty1()property2getProperty3() shuldn't be a dot-notation", PropertyTypeDeterminator.isDotExpression("getProperty1()property2getProperty3()"));
        assertFalse("proerty1property2property3 shouldn't be a dot-notaton", PropertyTypeDeterminator.isDotExpression("proerty1property2property3"));
    }

    @Test
    public void test_penultAndLast() {
        Pair<String, String> penultAndLast = PropertyTypeDeterminator.penultAndLast("property1.property2.property3");
        assertEquals("property1.property2 should be a penult property of the property1.property2.property3", "property1.property2", penultAndLast.getKey());
        assertEquals("property3 should be last property of the property1.property2.property3", "property3", penultAndLast.getValue());
        penultAndLast = PropertyTypeDeterminator.penultAndLast("getProperty1().property2.getProperty3()");
        assertEquals("getProperty1().property2 should be a penult property of the getProperty1().property2.getProperty3()", "getProperty1().property2", penultAndLast.getKey());
        assertEquals("getProperty3() should be last property of the getProperty1().property2.getProperty3()", "getProperty3()", penultAndLast.getValue());
        try {
            penultAndLast = PropertyTypeDeterminator.penultAndLast("property3");
            fail("property3 is not dot-notation therefore RuntimeException should be thrown");
        } catch (final ReflectionException ex) {
            assertEquals("A dot-expression is expected.", ex.getMessage());
        }
        try {
            penultAndLast = PropertyTypeDeterminator.penultAndLast("getProperty3()");
            fail("getProperty3() is not dot-expression therefore RuntimeException should be thrown");
        } catch (final ReflectionException ex) {
            assertEquals("A dot-expression is expected.", ex.getMessage());
        }
    }

    @Test
    public void test_transform() {
        Pair<Class<?>, String> pair = PropertyTypeDeterminator.transform(SecondLevelEntity.class, "propertyOfSelfType.property");
        assertEquals("Incorrect type of the penult property", SecondLevelEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "property", pair.getValue());
        pair = PropertyTypeDeterminator.transform(ComplexKeyEntity.class, "key.key");
        assertEquals("Incorrect type of the penult property", KeyEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "key", pair.getValue());
        pair = PropertyTypeDeterminator.transform(ComplexKeyEntity.class, "key.simpleEntity");
        assertEquals("Incorrect type of the penult property", KeyEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "simpleEntity", pair.getValue());
        pair = PropertyTypeDeterminator.transform(ComplexKeyEntity.class, "key.simpleEntity.key");
        assertEquals("Incorrect type of the penult property", SimpleEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "key", pair.getValue());
        pair = PropertyTypeDeterminator.transform(UnionEntityForReflector.class, "levelEntity.propertyOfSelfType.anotherProperty");
        assertEquals("Incorrect type of the penult property", SecondLevelEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "anotherProperty", pair.getValue());
        try {
            PropertyTypeDeterminator.transform(SecondLevelEntity.class, "propertyOfSelfType.nonExistingProperty.property");
            fail("Should have thrown an exception.");
        } catch (final Exception ex) {
            // expected situation
        }

        // methods finding tests: (including method inheritance & nested dot-notation properties)
        String methodName = "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.methodSecondLevel()";
        pair = PropertyTypeDeterminator.transform(SecondLevelEntity.class, methodName);
        assertEquals("Incorrect type of the penult property", SecondLevelEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "methodSecondLevel()", pair.getValue());

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel()";
        pair = PropertyTypeDeterminator.transform(SecondLevelEntity.class, methodName);
        assertEquals("Incorrect type of the penult property", SecondLevelEntity.class, pair.getKey());
        assertEquals("Incorrect last property name", "methodFirstLevel()", pair.getValue());

        methodName = "propertyOfSelfType.methodFirstLevel1().test";
        try {
            PropertyTypeDeterminator.transform(SecondLevelEntity.class, methodName);
            fail("There is no such property");
        } catch (final ReflectionException e) {
            // expected situation
        }
    }

    @Test
    public void test_collectional_properties_and_methods_type_determination() {
        assertEquals(List.class, PropertyTypeDeterminator.determineClass(EntityWithCollection.class, "collection", true, false));
        assertEquals(Double.class, PropertyTypeDeterminator.determineClass(EntityWithCollection.class, "collection", true, true));
        assertEquals(List.class, PropertyTypeDeterminator.determineClass(EntityWithCollection.class, "getCollection()", true, false));
        assertEquals(Double.class, PropertyTypeDeterminator.determineClass(EntityWithCollection.class, "getCollection()", true, true));

        assertEquals(Double.class, PropertyTypeDeterminator.determinePropertyType(EntityWithCollection.class, "collection"));
        assertEquals(Double.class, PropertyTypeDeterminator.determinePropertyType(EntityWithCollection.class, "getCollection()"));

        assertEquals(Integer.class, PropertyTypeDeterminator.determinePropertyType(EntityWithCollection.class, "collection2.intCollectionalProperty"));
        assertEquals(Integer.class, PropertyTypeDeterminator.determinePropertyType(EntityWithCollection.class, "getCollection2().getIntCollectionalProperty()"));
    }

    protected class GenericsPropertiesTestClass<T extends Number> {
        private List<Integer> prop1;
        private List<T> prop2;
        private List<? extends Float> prop3;
        private List<BigInteger[]> prop4;

        public List<Integer> getProp1() {
            return prop1;
        }

        public List<T> getProp2() {
            return prop2;
        }

        public List<? extends Float> getProp3() {
            return prop3;
        }

        public List<BigInteger[]> getProp4() {
            return prop4;
        }
    }

    @Test
    public void test_type_class_conversion() {
        assertEquals(Integer.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "prop1"));
        assertEquals(Integer.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "getProp1()"));
        assertEquals(Number.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "prop2"));
        assertEquals(Number.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "getProp2()"));
        assertEquals(Float.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "prop3"));
        assertEquals(Float.class, PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "getProp3()"));
        // TODO following tests are not passed under Java 7, which might indicate a bug in Java 7...
        // TODO See java.lang.reflect.GenericArrayType interface and PropertyTypeDeterminator.classFrom() method for more details.
        assertEquals(new BigInteger[] {}.getClass(), PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "prop4"));
        assertEquals(new BigInteger[] {}.getClass(), PropertyTypeDeterminator.determinePropertyType(GenericsPropertiesTestClass.class, "getProp4()"));
    }

    @Test
    public void test_type_detrmination_for_property_descriptors() {
        assertEquals(PropertyDescriptor.class, PropertyTypeDeterminator.determinePropertyType(Entity.class, "propertyDescriptor"));
        assertEquals(PropertyDescriptor.class, PropertyTypeDeterminator.determinePropertyType(Entity.class, "getPropertyDescriptor()"));
    }

    @Test
    public void properties_of_type_Long_recognized_as_numeric() {
        assertTrue(isNumeric(EntityWithNumericProps.class, "numericLong"));
    }

    @Test
    public void properties_of_type_Integer_recognized_as_numeric() {
        assertTrue(isNumeric(EntityWithNumericProps.class, "numericInteger"));
    }

    @Test
    public void properties_of_type_BigDecimal_recognized_as_numeric() {
        assertTrue(isNumeric(EntityWithNumericProps.class, "numericBigDecimal"));
    }

    @Test
    public void properties_of_type_Money_recognized_as_numeric() {
        assertTrue(isNumeric(EntityWithNumericProps.class, "numericMoney"));
    }

    @Test
    public void properties_of_non_numeric_typs_are_not_recognized_as_numeric() {
        assertFalse(isNumeric(Entity.class, "propertyDescriptor"));
        assertFalse(isNumeric(Entity.class, "monitoring"));
        assertFalse(isNumeric(Entity.class, "bigDecimals"));
    }

    @Test
    public void map_properties_should_be_recognized_well() {
        assertTrue(isMap(EntityWithMap.class, "mapProperty"));
    }
    
    @Test
    public void isMappedOrCalculated_returns_true_for_mapped_or_calculated_properties() {
        assertTrue(isMappedOrCalculated(Entity.class, "observablePropertyInitialisedAsNull"));
        assertTrue(isMappedOrCalculated(Entity.class, "firstProperty"));
    }
    
    @Test
    public void isMappedOrCalculated_returns_false_for_invalid_propety_names() {
        assertFalse(isMappedOrCalculated(Entity.class, "invalid property name"));
        assertFalse(isMappedOrCalculated(Entity.class, ""));
    }
    
    @Test
    public void isMappedOrCalculated_returns_false_for_not_mappped_or_calculated_properties() {
        assertFalse(isMappedOrCalculated(Entity.class, "monitoring"));
        assertFalse(isMappedOrCalculated(Entity.class, "observableProperty"));
    }
    
    @Test
    public void determinePropertyType_returns_null_if_the_type_could_not_be_determined() {
        assertNull(determinePropertyType(AbstractPersistentEntity.class, AbstractEntity.KEY));
    }
    
    @Test
    public void determinePropertyType_requires_both_type_and_propety_to_be_specified() {
        try {
            determinePropertyType(null, AbstractEntity.KEY);
        } catch (final ReflectionException ex) {
            assertEquals(PropertyTypeDeterminator.ERR_TYPE_AND_PROP_REQUIRED, ex.getMessage());
        }

        try {
            determinePropertyType(AbstractPersistentEntity.class, null);
        } catch (final ReflectionException ex) {
            assertEquals(PropertyTypeDeterminator.ERR_TYPE_AND_PROP_REQUIRED, ex.getMessage());
        }
        
        try {
            determinePropertyType(AbstractPersistentEntity.class, "");
        } catch (final ReflectionException ex) {
            assertEquals(PropertyTypeDeterminator.ERR_TYPE_AND_PROP_REQUIRED, ex.getMessage());
        }
        
        try {
            determinePropertyType(null, null);
        } catch (final ReflectionException ex) {
            assertEquals(PropertyTypeDeterminator.ERR_TYPE_AND_PROP_REQUIRED, ex.getMessage());
        }
    }

}
