package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.ListenedArrayList;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for base TG domain tree representation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeTest {
    private static final String CREATE_DTM_FOR = "createDtm_for_";
    private static final String CREATE_IRRELEVANT_DTM_FOR = "createIrrelevantDtm_for_";
    private static final String PERFORM_AFTER_DESERIALISATION_PROCESS_FOR = "performAfterDeserialisationProcess_for_";
    private static final String PERFORM_ADDITIONAL_INITIALISATION_PROCESS_FOR = "performAdditionalInitialisationProcess_for_";
    private static final String MANAGE_TESTING_DTM_FOR = "manageTestingDTM_for_";
    private static final String ASSERT_INNER_CROSS_REFERENCES_FOR = "assertInnerCrossReferences_for_";

    /** A base types to be checked for its non-emptiness and non-emptiness of their children. */
    private static final List<Class<?>> DOMAIN_TREE_TYPES = new ArrayList<Class<?>>() {
        {
            add(AbstractEntity.class); //
            add(ListenedArrayList.class);
            add(LinkedHashMap.class); //
            add(EnhancementSet.class); //
            add(EnhancementLinkedRootsSet.class); //
            add(EnhancementRootsMap.class); //
            add(EnhancementPropertiesMap.class); //
            add(ByteArray.class); //
            add(Ordering.class); //
            add(Function.class); //
            add(CalculatedPropertyCategory.class); //
            add(CalculatedPropertyAttribute.class); //
            add(ICalculatedProperty.class); //
            add(IDomainTreeEnhancer.class); //
            add(IDomainTreeRepresentation.class); //
            add(IDomainTreeManager.class); //
            add(ITickRepresentation.class); //
            add(ITickManager.class); //
        }
    };
    private static Object irrelevantDtm;
    private Object dtm = null;
    private static Class<? extends AbstractDomainTreeTest> testCaseClass1;
    protected static Class<?> classToBeTested = Object.class;
    private static EntityFactory factory;

    private static EntityFactory createFactory() {
        final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
        final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
        return injector.getInstance(EntityFactory.class);
    }

    /**
     * Returns a testing manager. Can be overridden to return specific manager for specific descendant test.
     *
     * @return
     */
    protected Object dtm() {
        return dtm;
    }

    /**
     * Returns an irrelevant "master" manager for current manager. It should be used only for initialisation purposes for current manager.
     *
     * @return
     */
    protected final static Object irrelevantDtm() {
        return irrelevantDtm;
    }

    /**
     * Returns a testing manager.
     *
     * @return
     */
    protected final Object just_a_dtm() {
        return dtm;
    }

    /**
     * Returns a factory instance for all tests.
     *
     * @return
     */
    protected final static EntityFactory factory() {
        return factory;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected static final void initialiseDomainTreeTest(final Class<? extends AbstractDomainTreeTest> testCaseClass) throws Exception {
        testCaseClass1 = testCaseClass;
        try {
            // uncomment following code to enable different LEVEL OF LOADING for all domain tree tests
            //	    final Field loadingLevelField = AbstractDomainTreeRepresentation.class.getDeclaredField("LOADING_LEVEL");
            //	    final boolean isAccessible = loadingLevelField.isAccessible();
            //	    loadingLevelField.setAccessible(true);
            //	    loadingLevelField.set(null, Integer.MAX_VALUE);
            //	    loadingLevelField.setAccessible(isAccessible);
            
            factory = createFactory();
            irrelevantDtm = Reflector.getMethod(testCaseClass, CREATE_IRRELEVANT_DTM_FOR + testCaseClass.getSimpleName()).invoke(null);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Before
    public final void initEachTest() throws Exception {
        dtm = Reflector.getMethod(testCaseClass1, CREATE_DTM_FOR + testCaseClass1.getSimpleName()).invoke(null);
        Reflector.getMethod(testCaseClass1, PERFORM_AFTER_DESERIALISATION_PROCESS_FOR + testCaseClass1.getSimpleName(), Object.class).invoke(null, dtm);
        try {
            Reflector.getMethod(testCaseClass1, PERFORM_ADDITIONAL_INITIALISATION_PROCESS_FOR + testCaseClass1.getSimpleName(), Object.class).invoke(null, dtm);
        } catch (final NoSuchMethodException ex) { // legal situation -- the method can be not present
        }
        Reflector.getMethod(testCaseClass1, MANAGE_TESTING_DTM_FOR + testCaseClass1.getSimpleName(), Object.class).invoke(null, dtm);
    }

    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeTest() {
        final Set<Class<?>> rootTypes = new HashSet<>();
        rootTypes.add(MasterEntity.class);
        rootTypes.add(EvenSlaverEntity.class);
        rootTypes.add(EntityWithNormalNature.class);
        rootTypes.add(EntityWithStringKeyType.class);
        rootTypes.add(MasterEntityForIncludedPropertiesLogic.class);
        rootTypes.add(MasterEntityWithUnionForIncludedPropertiesLogic.class);
        return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_AbstractDomainTreeTest(final Object dtm) {
        final IDomainTreeRepresentation dtr = (IDomainTreeRepresentation) dtm;
        dtr.excludeImmutably(EvenSlaverEntity.class, "");
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                dtr.excludeImmutably(MasterEntity.class, name);
            }
        }, "excludedManuallyProp");

        dtr.getFirstTick().disableImmutably(EntityWithNormalNature.class, "");
        dtr.getSecondTick().disableImmutably(EntityWithNormalNature.class, "");
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                if (!dtr.isExcludedImmutably(MasterEntity.class, name)) {
                    dtr.getFirstTick().disableImmutably(MasterEntity.class, name);
                    dtr.getSecondTick().disableImmutably(MasterEntity.class, name);
                }
            }
        }, "disabledManuallyProp");
    }

    /**
     * Returns <code>true</code> if all desired fields (recursive) are initialised, <code>false</code> otherwise.
     *
     * @param instance
     * @return
     */
    private final static boolean allDomainTreeFieldsAreInitialised(final Object instance) {
        return allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(instance, null);
    }

    private static List<Field> getDomainTreeFields(final Class<?> type) {
        // A base types to be checked for its non-emptiness and non-emptiness of their children.
        final List<Class<?>> types = new ArrayList<>(DOMAIN_TREE_TYPES);
        // A base types to be checked for its non-emptiness.
        // covered by EnhancementSetAndMaps? types.add(Set.class);
        // covered by EnhancementSetAndMaps? types.add(Map.class);
        types.add(Enum.class); // CalculatedProperty implementation
        types.add(String.class); // CalculatedProperty implementation
        types.add(Class.class); // CalculatedProperty implementation
        return Finder.getFieldsOfSpecifiedTypes(type, types);
    }

    /**
     * Returns <code>true</code> if all desired fields (recursive) are initialised, <code>false</code> otherwise.
     *
     * @param instance
     * @return
     */
    protected final static boolean allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(final Object instance, final Object originalInstance, final String... fieldWhichReferenceShouldNotBeDistictButShouldBeEqual) {
        final List<Field> fields = getDomainTreeFields(instance.getClass());
        try {
            for (final Field field : fields) {
                // System.err.println("Instance = [" + instance + "], field = [" + field + "].");
                final Object fieldValue = Finder.getFieldValue(field, instance);
                // System.out.println("allDomainTreeFieldsAreInitialised : Field [" + field + "]; value [" + fieldValue + "];");
                // all field values should be initialised! Including transient fields, domain tree types, maps, etc.
                if (fieldValue == null) {
                    fail("After deserialisation of the manager all the fields should be initialised (including transient). But field's [" + field
                            + "] value is null for instance [" + instance + "].");
                    return false;
                } else {
                    final Class<?> fieldValueType = fieldValue.getClass();
                    if (!Modifier.isStatic(field.getModifiers()) && originalInstance != null && !EntityUtils.isEnum(fieldValueType)
                            && !Arrays.asList(fieldWhichReferenceShouldNotBeDistictButShouldBeEqual).contains(field.getName())) {
                        final Object originalFieldValue = Finder.getFieldValue(field, originalInstance);
                        assertFalse("The references of corresponding fields should be distinct. Field = [" + field + "]; value original = [" + originalFieldValue
                                + "]; value new = [" + fieldValue + "].", fieldValue == originalFieldValue);
                    }
                    // all non-transient domain-tree-typed fields should have children initialised!
                    if (!Modifier.isTransient(field.getModifiers())
                            && Finder.isAssignableFrom(fieldValueType, DOMAIN_TREE_TYPES)
                            && !EntityUtils.isEnum(fieldValueType)
                            && !allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(fieldValue, originalInstance != null ? Finder.getFieldValue(field, originalInstance)
                                    : null, fieldWhichReferenceShouldNotBeDistictButShouldBeEqual)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Test
    public final void initialisation_of_dtm_completes_with_integrity() throws Exception {
        if (dtm == null) {
            return;
        }
        // at first the fullness and correctness of dtm() should be checked
        assertTrue("After test initialisation all the fields of the manager should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm));
        Reflector.getMethod(getClass(), ASSERT_INNER_CROSS_REFERENCES_FOR + getClass().getSimpleName(), Object.class).invoke(null, dtm);
    }

    /**
     * Performs after deserialisation process for "dtm" to define it fully, for e.g. when it is dependent on higher level structures. Can be 'overridden' to perform specific
     * processes for concrete manager in specific descendant tests.
     *
     * @param dtm
     */
    protected static void performAfterDeserialisationProcess_for_AbstractDomainTreeTest(final Object dtm) {
    }

    /**
     * Asserts inner cross-references for its correctness for "dtm". Can be 'overridden' to assert specifically concrete manager for specific descendant tests.
     *
     * @param dtm
     */
    protected static void assertInnerCrossReferences_for_AbstractDomainTreeTest(final Object dtm) {
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// Utilities ///////////////////////////////
    ////////////////////////////////////////////////////////////////

    protected void checkOrSetMethodValues(final Object value, final String property, final Object instance, final String methodName, final Class<?>... setterArg) {
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                try {
                    if (methodName.startsWith("set")) {
                        Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class, (setterArg.length <= 0 ? Object.class : setterArg[0])).invoke(instance, MasterEntity.class, name, value);
                    } else {
                        assertEquals("Value is incorrect.", value, Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class).invoke(instance, MasterEntity.class, name));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, property);
    }

    protected void checkOrSetMethodValuesForNonCollectional(final Object value, final String property, final Object instance, final String methodName, final Class<?>... setterArg) {
        allLevelsWithoutCollections(new IAction() {
            @Override
            public void action(final String name) {
                try {
                    if (methodName.startsWith("set")) {
                        Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class, (setterArg.length <= 0 ? Object.class : setterArg[0])).invoke(instance, MasterEntity.class, name, value);
                    } else {
                        assertEquals("Value is incorrect.", value, Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class).invoke(instance, MasterEntity.class, name));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, property);
    }

    protected void checkOrSetMethodValuesForOneLevel(final Object value, final String property, final Object instance, final String methodName, final Class<?>... setterArg) {
        oneLevel(new IAction() {
            @Override
            public void action(final String name) {
                try {
                    if (methodName.startsWith("set")) {
                        Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class, (setterArg.length <= 0 ? Object.class : setterArg[0])).invoke(instance, MasterEntity.class, name, value);
                    } else {
                        assertEquals("Value is incorrect.", value, Reflector.getMethod(instance.getClass(), methodName, Class.class, String.class).invoke(instance, MasterEntity.class, name));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, property);
    }

    /**
     * Just an action.
     *
     * @author TG Team
     *
     */
    protected interface IAction {
        void action(final String name);
    }

    /**
     * Runs simply an action for one level of properties :
     *
     * <pre>
     * action.action(atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void oneLevel(final IAction action, final String... atomicNames) {
        for (final String atomicName : atomicNames) {
            action.action(atomicName);
        }
    }

    /**
     * Runs simply an action for all appropriate levels of properties :
     *
     * <pre>
     * action.action(atomicName);
     * action.action(&quot;entityProp.&quot; + atomicName);
     * action.action(&quot;entityProp.entityProp.&quot; + atomicName);
     * action.action(&quot;collection.&quot; + atomicName);
     * action.action(&quot;entityProp.collection.&quot; + atomicName);
     * action.action(&quot;entityProp.collection.slaveEntityProp.&quot; + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevels(final IAction action, final String... atomicNames) {
        allLevelsWithoutCollections(action, atomicNames);
        allLevelsWithCollections(action, atomicNames);
    }

    /**
     * Runs simply an action for levels without collections:
     *
     * <pre>
     * action.action(atomicName);
     * action.action(&quot;entityProp.&quot; + atomicName);
     * action.action(&quot;entityProp.entityProp.&quot; + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevelsWithoutCollections(final IAction action, final String... atomicNames) {
        for (final String atomicName : atomicNames) {
            action.action(atomicName);
            action.action("".equals(atomicName) ? "entityProp" : ("entityProp" + "." + atomicName));
            action.action("".equals(atomicName) ? "entityProp.entityProp" : ("entityProp.entityProp" + "." + atomicName));
        }
    }

    /**
     * Runs simply an action for levels with collections:
     *
     * <pre>
     * action.action(&quot;collection.&quot; + atomicName);
     * action.action(&quot;entityProp.collection.&quot; + atomicName);
     * action.action(&quot;entityProp.collection.slaveEntityProp.&quot; + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevelsWithCollections(final IAction action, final String... atomicNames) {
        for (final String atomicName : atomicNames) {
            action.action("".equals(atomicName) ? "collection" : ("collection" + "." + atomicName));
            action.action("".equals(atomicName) ? "entityProp.collection" : ("entityProp.collection" + "." + atomicName));
            action.action("".equals(atomicName) ? "entityProp.collection.slaveEntityProp" : ("entityProp.collection.slaveEntityProp" + "." + atomicName));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////////////////////// 7. Persistence, equality & comparison //////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    protected static final IDomainTreeManagerAndEnhancer enhanceDomainWithCalculatedPropertiesOfDifferentTypes(final IDomainTreeManagerAndEnhancer dtm) {
        // enhance domain to 1) check whether the inherited representation logic will be ok 2) check calculated properties representation
        // EXPRESSION
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "2 * integerProp", "Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "2 * integerProp", "Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        // AGGREGATED_EXPRESSION
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "SUM(integerProp)", "Aggr Expr Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "SUM(integerProp)", "Aggr Expr Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        // COLLECTIONAL_EXPRESSION
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Coll Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "2 * integerProp", "Coll Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        // 				TODO AGGREGATED_COLLECTIONAL_EXPRESSION
        // 				TODO dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "SUM(integerProp)", "Aggr Coll Expr Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        // 				TODO dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "SUM(integerProp)", "Aggr Coll Expr Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
        // ATTRIBUTED_COLLECTIONAL_EXPRESSION
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Attr Coll Expr Prop1", "desc", CalculatedPropertyAttribute.ALL, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "2 * integerProp", "Attr Coll Expr Prop2", "desc", CalculatedPropertyAttribute.ANY, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dtm.getEnhancer().apply();

        dtm.getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "entityProp.exprProp");
        dtm.getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "aggrExprProp1");
        dtm.getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, "entityProp.collection.collExprProp");
        dtm.getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, "entityProp.collection.simpleEntityProp.collExprProp");
        return dtm;
    }

    private void checkAccessabilityOfCalculatedPropertiesAndTheirState(final IDomainTreeManagerAndEnhancer dtm) {
        checkAccessabilityOfCalculatedProperty(dtm, "exprProp");
        checkAccessabilityOfCalculatedProperty(dtm, "entityProp.exprProp");

        checkAccessabilityOfCalculatedProperty(dtm, "aggrExprProp1");
        checkAccessabilityOfCalculatedProperty(dtm, "aggrExprProp2");

        checkAccessabilityOfCalculatedProperty(dtm, "entityProp.collection.collExprProp");
        checkAccessabilityOfCalculatedProperty(dtm, "entityProp.collection.simpleEntityProp.collExprProp");

        // 				TODO checkAccessabilityOfCalculatedProperty(dtm, "entityProp.aggrCollExprProp1");
        // 				TODO checkAccessabilityOfCalculatedProperty(dtm, "entityProp.aggrCollExprProp2");

        checkAccessabilityOfCalculatedProperty(dtm, "entityProp.collection.attrCollExprProp1");
        checkAccessabilityOfCalculatedProperty(dtm, "entityProp.collection.simpleEntityProp.attrCollExprProp2");

        assertTrue("The calculated property [entityProp.exprProp] should be excluded. The path towards calculated property should be properly resolved.", dtm.getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.exprProp"));
        assertTrue("The calculated property [aggrExprProp1] should be disabled. The path towards calculated property should be properly resolved.", dtm.getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "aggrExprProp1"));
        assertTrue("The calculated property [entityProp.collection.collExprProp] should be disabled. The path towards calculated property should be properly resolved.", dtm.getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.collExprProp"));
        assertTrue("The calculated property [entityProp.collection.simpleEntityProp.collExprProp] should be excluded. The path towards calculated property should be properly resolved.", dtm.getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.simpleEntityProp.collExprProp"));
    }

    private void checkAccessabilityOfCalculatedProperty(final IDomainTreeManagerAndEnhancer dtm, final String calcProperty) {
        final boolean isExcl;
        try {
            // just invoke appropriate accessor method
            isExcl = dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, calcProperty);
            if (!isExcl) {
                // to be sure -- invoke another accessor method
                dtm.getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, calcProperty);
                dtm.getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, calcProperty);
            }
        } catch (final DomainTreeException e) {
            fail("The calculated property [" + calcProperty + "] should not be excluded. The path towards calculated property should be properly resolved.");
        }
    }

    @Test
    public void test_that_domain_tree_enhancements_work_as_expected_for_original_and_copied_manager() {
        if (dtm() instanceof IDomainTreeManagerAndEnhancer) {
            enhanceDomainWithCalculatedPropertiesOfDifferentTypes((IDomainTreeManagerAndEnhancer) dtm());
            // to perform such a test it is enough to ask if the added calc properties can be asked for "excludement / disablement" (and the state is appropriate), after manual "disabling / excluding".
            // It will process the domain tree to the needed level of enhanced hierarchy.
            final IDomainTreeManagerAndEnhancer dtm = (IDomainTreeManagerAndEnhancer) dtm();

            dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "7 * integerProp", "Calculated Property", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
            dtm.getEnhancer().apply();

            checkAccessabilityOfCalculatedPropertiesAndTheirState(dtm);

            dtm.getEnhancer().getCalculatedProperty(MasterEntity.class, "calculatedProperty").contextType();
        }
    }

    protected static String name(final String path, final String name) {
        return "".equals(path) ? name : path + "." + name;
    }

}