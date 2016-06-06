package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity;
import ua.com.fielden.platform.domaintree.testing.EnhancingSlaveEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.EntityTypeAnnotation;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for {@link DomainTreeEnhancer}.
 *
 * @author TG Team
 *
 */
public class DomainTreeEnhancerTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IDomainTreeEnhancer dtm() {
        return (IDomainTreeEnhancer) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        /////////////////////////////////////////////////////////////////
        //////////////////////// IMPORTANT //////////////////////////////
        /////////////////////////////////////////////////////////////////
        // Testing enhancer will be serialised in "static" byte array.
        // In every particular test it will be deserialised (which includes enhanced types loading).
        // For clean experiment old classLoader should be disposed with all enhanced classes.
        // But in this case this behaviour is slightly UNEXPECTED due to non-immediate
        // Garbage Collection of the classLoader.
        /////////////////////////////////////////////////////////////////
        //////////////////////// IMPORTANT //////////////////////////////
        /////////////////////////////////////////////////////////////////
        initialiseDomainTreeTest(DomainTreeEnhancerTest.class);
    }

    protected static Object createDtm_for_DomainTreeEnhancerTest() {
        final DomainTreeEnhancer dte = new DomainTreeEnhancer(serialiser(), createRootTypes_for_DomainTreeEnhancerTest());
        assertEquals("Incorrect count of enhanced types byte arrays.", 1, dte.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        final DomainTreeEnhancer similarlyCreatedDte = new DomainTreeEnhancer(serialiser(), createRootTypes_for_DomainTreeEnhancerTest());
        assertTrue("Similarly created instance should be equal to the original instance.", EntityUtils.equalsEx(dte, similarlyCreatedDte));

        similarlyCreatedDte.apply();
        assertTrue("Similarly created instance after apply() should be equal to the original instance.", EntityUtils.equalsEx(dte, similarlyCreatedDte));

        final DomainTreeEnhancer dtmCopy0 = EntityUtils.deepCopy(dte, serialiser());
        final DomainTreeEnhancer dtmCopy1 = EntityUtils.deepCopy(dtmCopy0, serialiser());
        assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(dtmCopy1, dtmCopy0));
        assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(dtmCopy0, dte));

        dte.addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "1 * integerProp", "Old single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dte.addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "2 * integerProp", "Old double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dte.addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "3 * integerProp", "Old triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dte.addCalculatedProperty(EnhancingMasterEntity.class, "", "4 * integerProp", "Old quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dte.apply();

        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dte.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        return dte;
    }

    protected static Object createIrrelevantDtm_for_DomainTreeEnhancerTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_DomainTreeEnhancerTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
        rootTypes.add(EnhancingMasterEntity.class);
        return rootTypes;
    }

    protected static void manageTestingDTM_for_DomainTreeEnhancerTest(final Object obj) {
    }

    protected static void performAfterDeserialisationProcess_for_DomainTreeEnhancerTest(final Object obj) {
    }

    protected static void assertInnerCrossReferences_for_DomainTreeEnhancerTest(final Object obj) {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static void fieldDoesNotExist(final Class<?> type, final String prop) {
        try {
            final Field field = Finder.findFieldByName(type, prop);
            assertNull("The property [" + prop + "] should not exist in type [" + type + "].", field);
        } catch (final IllegalArgumentException e) {
        }
    }

    private static void fieldDoesNotExistInAnyPlaceExcept(final Class<?> type, final String prop) {
        // ensure that there are no fields with the same name on the hierarchy.
        final String atomicName = PropertyTypeDeterminator.transform(type, prop).getValue();
        final List<String> names = Arrays.asList("" + atomicName, //
                "masterEntityProp." + atomicName,

                "masterEntityProp.masterEntityProp." + atomicName, "masterEntityProp.masterEntityProp.masterEntityProp." + atomicName, "masterEntityProp.masterEntityProp.slaveEntityProp."
                        + atomicName, "masterEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

                "masterEntityProp.slaveEntityProp." + atomicName, "masterEntityProp.slaveEntityProp.masterEntityProp." + atomicName, "masterEntityProp.slaveEntityProp.slaveEntityProp."
                        + atomicName, "masterEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

                "masterEntityProp.evenSlaverEntityProp." + atomicName, "masterEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName, "masterEntityProp.evenSlaverEntityProp.slaveEntityProp."
                        + atomicName, "masterEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName,
                ////////////////////////////////////////////////////////////////////////////
                "slaveEntityProp." + atomicName,

                "slaveEntityProp.masterEntityProp." + atomicName, "slaveEntityProp.masterEntityProp.masterEntityProp." + atomicName, "slaveEntityProp.masterEntityProp.slaveEntityProp."
                        + atomicName, "slaveEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

                "slaveEntityProp.slaveEntityProp." + atomicName, "slaveEntityProp.slaveEntityProp.masterEntityProp." + atomicName, "slaveEntityProp.slaveEntityProp.slaveEntityProp."
                        + atomicName, "slaveEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

                "slaveEntityProp.evenSlaverEntityProp." + atomicName, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName, "slaveEntityProp.evenSlaverEntityProp.slaveEntityProp."
                        + atomicName, "slaveEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName,
                ////////////////////////////////////////////////////////////////////////////
                "evenSlaverEntityProp." + atomicName,

                "evenSlaverEntityProp.masterEntityProp." + atomicName, "evenSlaverEntityProp.masterEntityProp.masterEntityProp." + atomicName, "evenSlaverEntityProp.masterEntityProp.slaveEntityProp."
                        + atomicName, "evenSlaverEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

                "evenSlaverEntityProp.slaveEntityProp." + atomicName, "evenSlaverEntityProp.slaveEntityProp.masterEntityProp." + atomicName, "evenSlaverEntityProp.slaveEntityProp.slaveEntityProp."
                        + atomicName, "evenSlaverEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

                "evenSlaverEntityProp.evenSlaverEntityProp." + atomicName, "evenSlaverEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName, "evenSlaverEntityProp.evenSlaverEntityProp.slaveEntityProp."
                        + atomicName, "evenSlaverEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName);
        for (final String name : names) {
            if (!name.equals(prop)) {
                fieldDoesNotExist(type, name);
            }
        }
    }

    /**
     * Ensures that specified 'custom' property field exists with specified parameters and that no field with specified "atomic" name exists on the hierarchy.
     *
     * @param type
     * @param prop
     * @param customPropType
     * @param title
     * @param desc
     */
    protected static void customFieldExistsInSinglePlaceAndItWORKS(final Class<?> type, final String prop, final Class<?> customPropType, final String title, final String desc) {
        final Field field = Finder.findFieldByName(type, prop);

        assertNotNull("The property [" + prop + "] should exist in type [" + type + "].", field);
        // check type
        assertEquals("Incorrect type.", customPropType, field.getType());
        // check Title annotation
        final Title titleAnno = AnnotationReflector.getAnnotation(field, Title.class);
        assertNotNull("The annotation Title should exist.", titleAnno);
        assertEquals("Incorrect title.", title, titleAnno.value());
        assertEquals("Incorrect desc.", desc, titleAnno.desc());

        fieldDoesNotExistInAnyPlaceExcept(type, prop);

        field.getDeclaringClass();
        field.getModifiers();
        // TODO does the field work???
    }

    /**
     * Ensures that specified property field exists with specified "calculated" parameters and that no field with specified "atomic" name exists on the hierarchy.
     *
     * @param type
     * @param prop
     * @param category
     * @param originationProp
     * @param calcPropType
     * @param expr
     * @param title
     * @param desc
     */
    protected static void calcFieldExistsInSinglePlaceAndItWORKS(final Class<?> type, final String prop, final ICalculatedProperty.CalculatedPropertyCategory category, final String originationProp, final Class<?> calcPropType, final String expr, final String title, final String desc) {
        final Field field = Finder.findFieldByName(type, prop);

        assertNotNull("The property [" + prop + "] should exist in type [" + type + "].", field);
        // check type
        assertEquals("Incorrect type.", calcPropType, field.getType());
        // check Calculated annotation
        final Calculated calcAnno = AnnotationReflector.getAnnotation(field, Calculated.class);
        assertNotNull("The annotation Calculated should exist.", calcAnno);
        assertEquals("Incorrect expression.", expr, calcAnno.value());
        assertEquals("Incorrect root.", type.getName(), calcAnno.rootTypeName());
        assertEquals("Incorrect origination property.", originationProp, calcAnno.origination());
        assertEquals("Incorrect category.", category, calcAnno.category());
        // check Title annotation
        final Title titleAnno = AnnotationReflector.getAnnotation(field, Title.class);
        assertNotNull("The annotation Title should exist.", titleAnno);
        assertEquals("Incorrect title.", title, titleAnno.value());
        assertEquals("Incorrect desc.", desc, titleAnno.desc());

        fieldDoesNotExistInAnyPlaceExcept(type, prop);

        field.getDeclaringClass();
        field.getModifiers();
        // TODO does the field work???
    }

    /**
     * Ensures that the specified property field does not exist.
     *
     * @param type
     * @param prop
     */
    protected static void fieldDoesNotExistInAnyPlace(final Class<?> type, final String prop) {
        fieldDoesNotExist(type, prop);
        fieldDoesNotExistInAnyPlaceExcept(type, prop);
    }

    private static void checkOriginalDomain(final IDomainTreeEnhancer dm) {
        // check the snapshot of domain
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Old triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Old quadruple", "Desc");
        checkDomainPreparedForEnhancements(dm);
        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    private static void checkEmptyDomain(final IDomainTreeEnhancer dm) {
        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        checkDomainPreparedForEnhancements(dm);
        assertEquals("Incorrect count of enhanced types byte arrays.", 1, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    protected static void checkDomainPreparedForEnhancements(final IDomainTreeEnhancer dm) {
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "single");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
    }

    private static void checkDiscardOperation(final IDomainTreeEnhancer dm) {
        checkOriginalDomain(dm);

        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "6 * integerProp", "Smth prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setTitle("OlD DoUbLe").setDesc("dESC").setContextualExpression("22 * integerProp");
        dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dm.discard();
        dm.apply();

        checkOriginalDomain(dm);
    }

    private static void check_the_properties_order_and_placing_for_first_level_enhancements(final IDomainTreeEnhancer dm) {
        // modify domain
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "1 * integerProp", "Single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "1 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "1 * integerProp", "Triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "1 * integerProp", "Quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.apply();

        final Class<?> newType = dm.getManagedType(EnhancingMasterEntity.class);
        final int size = newType.getDeclaredFields().length;
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", "quadruple", newType.getDeclaredFields()[size - 1].getName());
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", "triple", newType.getDeclaredFields()[size - 2].getName());
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", "double", newType.getDeclaredFields()[size - 3].getName());
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", "single", newType.getDeclaredFields()[size - 4].getName());

        // check the snapshot of domain
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Old triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Old quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "double", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "triple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "quadruple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Quadruple", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    private static void checkFirstLevelEnhancements(final IDomainTreeEnhancer dm) {
        // modify domain
        dm.addCustomProperty(EnhancingMasterEntity.class, "", "customProp", "Custom Prop", "Custom Prop Desc", Integer.class);
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "aName", "1 * 1 * integerProp", "Title for aName", "Desc for aName", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "", "1 * 1 * integerProp", "Title Bad", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.getCalculatedProperty(EnhancingMasterEntity.class, "titleBad").setTitle("Single").setContextualExpression("1 * integerProp");
        dm.getCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple").setDesc("New Desc").setContextualExpression("4 * 1 * integerProp");
        dm.apply();

        // check the snapshot of domain
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "customProp", Integer.class, "Custom Prop", "Custom Prop Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "aName", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * 1 * integerProp", "Title for aName", "Desc for aName");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Old triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * 1 * integerProp", "Old quadruple", "New Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        // modify domain
        dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        // dm.removeCalculatedProperty(EnhancingMasterEntity.class, "aName");
        dm.apply();

        // check the snapshot of domain
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "customProp", Integer.class, "Custom Prop", "Custom Prop Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "aName", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * 1 * integerProp", "Title for aName", "Desc for aName");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Old triple", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        // modify domain
        dm.removeCalculatedProperty(EnhancingMasterEntity.class, "aName");
        dm.apply();

        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "aName");
    }

    private static void checkSecondLevelEnhancements(final IDomainTreeEnhancer dm) {
        // modify domain
        dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");

        dm.addCustomProperty(EnhancingMasterEntity.class, "masterEntityProp", "customProp2", "Custom Prop 2", "Custom Prop Desc 2", BigDecimal.class);
        dm.addCustomProperty(EnhancingMasterEntity.class, "slaveEntityProp", "customProp3", "Custom Prop 3", "Custom Prop Desc 3", String.class);
        dm.addCustomProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp", "customProp4", "Custom Prop 4", "Custom Prop Desc 4", EnhancingMasterEntity.class);

        dm.addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp", "2 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "3 * integerProp", "Triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp", "4 * integerProp", "Quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.apply();

        // check the snapshot of domain
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.customProp2", BigDecimal.class, "Custom Prop 2", "Custom Prop Desc 2");
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.customProp3", String.class, "Custom Prop 3", "Custom Prop Desc 3");
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.customProp4", EnhancingMasterEntity.class, "Custom Prop 4", "Custom Prop Desc 4");

        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Quadruple", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 6, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    private static void checkThirdLevelEnhancements(final IDomainTreeEnhancer dm) {
        // modify domain
        dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setContextualExpression("1 * 2 * integerProp").setTitle("New Title").setDesc("New Desc");
        dm.apply();

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.newTitle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * 2 * integerProp", "New Title", "New Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Quadruple", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 5, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        // modify domain
        dm.addCustomProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "customProp5", "Custom Prop 5", "Custom Prop Desc 5", Date.class);

        dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.newTitle").setContextualExpression("2 * integerProp").setTitle("Old double").setDesc("Desc");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "5 * integerProp", "Quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.masterEntityProp", "7 * integerProp", "Septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.masterEntityProp", "8 * integerProp", "Octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.apply();

        // check the snapshot of domain
        customFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.customProp5", Date.class, "Custom Prop 5", "Custom Prop Desc 5");

        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.quintuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "5 * integerProp", "Quintuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * integerProp", "Sextuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.masterEntityProp.septuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "7 * integerProp", "Septuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.masterEntityProp.octuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "8 * integerProp", "Octuple", "Desc");
        assertEquals("Incorrect count of enhanced types byte arrays.", 8, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    @Test
    public void test_Discard_operation() {
        checkDiscardOperation(dtm());
    }

    @Test
    public void test_Discard_operation_FOR_THE_COPY_OF_MANAGER() {
        // this is very important test due to JVM's lazy class loading!
        checkDiscardOperation(EntityUtils.deepCopy(dtm(), serialiser()));
    }

    @Test
    public void test_first_level_enhancements() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());
    }

    @Test
    public void test_the_properties_order_and_placing_for_first_level_enhancements() {
        checkDiscardOperation(dtm());
        check_the_properties_order_and_placing_for_first_level_enhancements(dtm());
    }

    @Test
    public void test_first_level_enhancements_FOR_THE_COPY_OF_MANAGER() {
        checkDiscardOperation(dtm());
        // this is very important test due to JVM's lazy class loading!
        checkFirstLevelEnhancements(EntityUtils.deepCopy(dtm(), serialiser()));
    }

    @Test
    public void test_second_level_enhancements() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());
        checkSecondLevelEnhancements(dtm());
    }

    @Test
    public void test_second_level_enhancements_FOR_THE_COPY_OF_MANAGER() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());
        // this is very important test due to JVM's lazy class loading!
        checkSecondLevelEnhancements(EntityUtils.deepCopy(dtm(), serialiser()));
    }

    @Test
    public void test_third_level_enhancements() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());
        checkSecondLevelEnhancements(dtm());
        checkThirdLevelEnhancements(dtm());
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_1() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());
        checkSecondLevelEnhancements(dtm());
        // this is very important test due to JVM's lazy class loading!
        checkThirdLevelEnhancements(EntityUtils.deepCopy(dtm(), serialiser()));
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_2() {
        checkDiscardOperation(dtm());
        checkFirstLevelEnhancements(dtm());

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());
        checkSecondLevelEnhancements(copy);
        checkThirdLevelEnhancements(copy);
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_3() {
        checkDiscardOperation(dtm());

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());
        checkFirstLevelEnhancements(copy);
        checkSecondLevelEnhancements(copy);
        checkThirdLevelEnhancements(copy);
    }

    @Test
    public void test_that_self_type_properties_will_not_be_adapted() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
        rootTypes.add(EnhancingMasterEntity.class);
        final IDomainTreeEnhancer dm = new DomainTreeEnhancer(serialiser(), rootTypes);

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        checkDomainPreparedForEnhancements(dm);
        assertEquals("Incorrect count of enhanced types byte arrays.", 1, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());

        // modify domain

        dm.addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dm.apply();

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "single");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
        calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * integerProp", "Sextuple", "Desc");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
        fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
        assertEquals("Incorrect count of enhanced types byte arrays.", 2, dm.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    private void failAddition(final IDomainTreeEnhancer dm, final Class<?> rootType, final String contextPath, final String title) {
        try {
            dm.addCalculatedProperty(rootType, contextPath, "5 * integerProp", title, "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
            fail("The calculated property key is incorrect. The action should be failed. Context path = [" + contextPath + "], title = " + title);
        } catch (final IncorrectCalcPropertyException e) {
        } catch (final Exception e) {
        }
    }

    private void failGettingAndRemoving(final IDomainTreeEnhancer dm, final Class<?> rootType, final String pathAndName) {
        try {
            dm.getCalculatedProperty(rootType, pathAndName);
            fail("The calculated property key is incorrect. The action should be failed. pathAndName = [" + pathAndName + "]");
        } catch (final IncorrectCalcPropertyException e) {
        }
        try {
            dm.removeCalculatedProperty(rootType, pathAndName);
            fail("The calculated property key is incorrect. The action should be failed. pathAndName = [" + pathAndName + "]");
        } catch (final IncorrectCalcPropertyException e) {
        }
    }

    @Test
    public void test_that_calc_properties_adding_is_validated() {
        // empty name
        failAddition(dtm(), EnhancingMasterEntity.class, "", null);
        failAddition(dtm(), EnhancingMasterEntity.class, "", "");
        // non-existent domain root
        failAddition(dtm(), EnhancingSlaveEntity.class, "", "correctProp");
        // non-existent path
        failAddition(dtm(), EnhancingMasterEntity.class, "masterEntityProp1", "Calc prop");
        failAddition(dtm(), EnhancingMasterEntity.class, "masterEntityProp.evenSlaverEntityProp1", "Calc prop");
        failAddition(dtm(), EnhancingMasterEntity.class, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp1", "Calc prop");
        // existent calc properties
        failAddition(dtm(), EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "Old single");
        failAddition(dtm(), EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "Old double");
        failAddition(dtm(), EnhancingMasterEntity.class, "slaveEntityProp", "Old triple");
        failAddition(dtm(), EnhancingMasterEntity.class, "", "Old quadruple");
        // existent simple properties
        failAddition(dtm(), EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "Integer prop");
        failAddition(dtm(), EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "Integer prop");
        failAddition(dtm(), EnhancingMasterEntity.class, "slaveEntityProp", "Integer prop");
        failAddition(dtm(), EnhancingMasterEntity.class, "", "Integer prop");
    }

    @Test
    public void test_that_calc_properties_removing_and_obtaining_is_validated() {
        // empty name
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "");
        // non-existent domain root
        failGettingAndRemoving(dtm(), EnhancingSlaveEntity.class, "oldQuadRuple");
        // non-existent path
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "masterEntityProp1.calcProp");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "masterEntityProp.evenSlaverEntityProp1.calcProp");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp1.calcProp");
        // non-existent calc properties
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle1");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble1");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "slaveEntityProp.oldTriple1");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "oldQuadruple1");
        // existent simple properties (are not calculated!)
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.integerProp");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.integerProp");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "slaveEntityProp.integerProp");
        failGettingAndRemoving(dtm(), EnhancingMasterEntity.class, "integerProp");
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());

        // modify domain
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "1 * integerProp", "Single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "2 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "3 * integerProp", "Triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "4 * integerProp", "Quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "5 * integerProp", "Quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "7 * integerProp", "Septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "8 * integerProp", "Octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().apply();

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityCollProp.single", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.double", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.triple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.quadruple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.quintuple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "5 * integerProp", "Quintuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.evenSlaverEntityCollProp.sextuple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "6 * integerProp", "Sextuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.slaveEntityProp.septuple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "7 * integerProp", "Septuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp.octuple", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "8 * integerProp", "Octuple", "Desc");
        assertEquals("Incorrect count of enhanced types byte arrays.", 10, copy.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced_by_ATTRIBUTED_COLLECTIONAL_EXPRESSIONs() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());

        // modify domain
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "1 * integerProp", "All of single", "Desc", CalculatedPropertyAttribute.ALL, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "2 * integerProp", "Any of double", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "3 * integerProp", "All of triple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp");

        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "4 * integerProp", "Any of quadruple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "5 * integerProp", "All of quintuple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "6 * integerProp", "Any of sextuple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "7 * integerProp", "All of septuple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "8 * integerProp", "Any of octuple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");

        dtm().apply();

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityCollProp.allOfSingle", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "All of single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.anyOfDouble", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Any of double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.allOfTriple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "All of triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.anyOfQuadruple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Any of quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.allOfQuintuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "5 * integerProp", "All of quintuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.evenSlaverEntityCollProp.anyOfSextuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "6 * integerProp", "Any of sextuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.slaveEntityProp.allOfSeptuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "7 * integerProp", "All of septuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp.anyOfOctuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "8 * integerProp", "Any of octuple", "Desc");
        assertEquals("Incorrect count of enhanced types byte arrays.", 10, copy.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    @Test
    @Ignore
    public void test_that_collectional_hierarchies_can_be_enhanced_by_AGGREGATED_COLLECTIONAL_EXPRESSIONs() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());

        // modify domain
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "SUM(1 * integerProp)", "Sum of single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "AVG(2 * integerProp)", "Avg of double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "SUM(6 * integerProp)", "Sum of sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());

        copy.addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "SUM(7 * integerProp)", "Sum of septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        copy.addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "SUM(8 * integerProp)", "Sum of octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        copy.apply();

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfSingle", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "SUM(1 * integerProp)", "Sum of single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "avgOfDouble", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", BigDecimal.class, "AVG(2 * integerProp)", "Avg of double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "minOfTriple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.maxOfQuadruple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfQuintuple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.sumOfSextuple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "SUM(6 * integerProp)", "Sum of sextuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.sumOfSeptuple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "SUM(7 * integerProp)", "Sum of septuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.sumOfOctuple", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "SUM(8 * integerProp)", "Sum of octuple", "Desc");
        assertEquals("Incorrect count of enhanced types byte arrays.", 5, copy.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }

    @Test
    public void test_that_first_second_and_third_levels_can_be_enhanced_to_form_AGGREGATED_EXPRESSIONs_at_root_level() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());

        // modify domain
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp", "SUM(1 * integerProp)", "Sum of single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "AVG(2 * integerProp)", "Avg of double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp", "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.slaveEntityProp", "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.evenSlaverEntityProp", "SUM(6 * integerProp)", "Sum of sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.slaveEntityProp", "SUM(7 * integerProp)", "Sum of septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        dtm().addCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.slaveEntityProp.masterEntityProp", "SUM(8 * integerProp)", "Sum of octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");

        dtm().apply();

        // this is very important test due to JVM's lazy class loading!
        final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dtm(), serialiser());

        // check the snapshot of domain
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldSingle");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldDouble");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldTriple");
        fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfSingle", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "SUM(1 * integerProp)", "Sum of single", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "avgOfDouble", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", BigDecimal.class, "AVG(2 * integerProp)", "Avg of double", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "minOfTriple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "maxOfQuadruple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfQuintuple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfSextuple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "SUM(6 * integerProp)", "Sum of sextuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfSeptuple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "SUM(7 * integerProp)", "Sum of septuple", "Desc");
        calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "sumOfOctuple", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "SUM(8 * integerProp)", "Sum of octuple", "Desc");
        assertEquals("Incorrect count of enhanced types byte arrays.", 1, copy.getManagedTypeArrays(EnhancingMasterEntity.class).size());
    }
    
    @Test
    public void adjusting_of_a_name_for_ungenerated_type_leads_to_exception() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());
        
        try {
            dtm().adjustManagedTypeName(EnhancingMasterEntity.class, "grwe7w64329y4e3289dfh293h");
            fail("Adjusting of a name for ungenerated type should fail.");
        } catch (final IllegalArgumentException e) {
        }
    }
    
    @Test
    public void adjusting_of_a_name_for_generated_type_replaces_the_name_and_do_not_change_domain_tree_enhancer_semantics() {
        final IDomainTreeEnhancer dtmCopy = EntityUtils.deepCopy(dtm(), serialiser());
        
        final String newSuffix = "grwe7w64329y4e3289dfh293h";
        final Class<?> adjustedType = dtm().adjustManagedTypeName(EnhancingMasterEntity.class, newSuffix);
        assertEquals("ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity$$TgEntity_" + newSuffix, adjustedType.getName());
        
        assertTrue("dte instance should be equal after generated type naming adjustments.", EntityUtils.equalsEx(dtmCopy, dtm()));
    }
    
    @Test
    public void adjusting_of_annotations_for_ungenerated_type_leads_to_exception() {
        // clear domain
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
        dtm().removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
        dtm().apply();

        // check the snapshot of domain
        checkEmptyDomain(dtm());
        
        try {
            dtm().adjustManagedTypeAnnotations(EnhancingMasterEntity.class);
            fail("Adjusting of a annotations for ungenerated type should fail.");
        } catch (final IllegalArgumentException e) {
        }
    }
    
    @Test
    public void adjusting_of_annotations_does_nothing_in_case_of_empty_annotations() {
        final Class<?> originalManagedType = dtm().getManagedType(EnhancingMasterEntity.class);
        final IDomainTreeEnhancer dtmCopy = EntityUtils.deepCopy(dtm(), serialiser());
        
        final Class<?> adjustedType = dtm().adjustManagedTypeAnnotations(EnhancingMasterEntity.class);
        assertEquals(adjustedType, originalManagedType);
        
        assertTrue("dte instance should be equal after no adjustments have been performed.", EntityUtils.equalsEx(dtmCopy, dtm()));
    }

    @Test
    public void adjusting_of_annotations_for_generated_type_adds_those_annotations_to_generated_type_and_do_not_change_domain_tree_enhancer_semantics() {
        final Annotation[] originalAnnotations = dtm().getManagedType(EnhancingMasterEntity.class).getAnnotations();
        assertEquals("There should be 2 type annotations in managedType for EnhancingMasterEntity.", 2, originalAnnotations.length);
        assertEquals("First annotation should be KeyTitle.", KeyTitle.class, originalAnnotations[0].annotationType());
        assertEquals("Second annotation should be KeyType.", KeyType.class, originalAnnotations[1].annotationType());
        
        final IDomainTreeEnhancer dtmCopy = EntityUtils.deepCopy(dtm(), serialiser());
        
        final Annotation[] deepCopiedAnnotations = dtmCopy.getManagedType(EnhancingMasterEntity.class).getAnnotations();
        assertEquals("There should be 2 type annotations in managedType for EnhancingMasterEntity.", 2, deepCopiedAnnotations.length);
        assertEquals("First annotation should be KeyTitle.", KeyTitle.class, deepCopiedAnnotations[0].annotationType());
        assertEquals("Second annotation should be KeyType.", KeyType.class, deepCopiedAnnotations[1].annotationType());
        
        final Class<?> adjustedType = dtm().adjustManagedTypeAnnotations(EnhancingMasterEntity.class, new EntityTypeAnnotation(EnhancingMasterEntity.class).newInstance());
        final Annotation[] adjustedAnnotations = adjustedType.getAnnotations();
        assertEquals("There should be 3 type annotations in managedType for EnhancingMasterEntity.", 3, adjustedAnnotations.length);
        assertEquals("First annotation should be KeyTitle.", KeyTitle.class, adjustedAnnotations[0].annotationType());
        assertEquals("Second annotation should be KeyType.", KeyType.class, adjustedAnnotations[1].annotationType());
        assertEquals("Third annotation should be EntityType.", EntityType.class, adjustedAnnotations[2].annotationType());
        assertEquals(EnhancingMasterEntity.class, adjustedType.getAnnotation(EntityType.class).value());
        
        assertTrue("dte instance should be equal after generated type annotations adjustments.", EntityUtils.equalsEx(dtmCopy, dtm()));
    }
}
