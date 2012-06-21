package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.ListenedArrayList;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Injector;

/**
 * A test for base TG domain tree representation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeTest {
    /** A base types to be checked for its non-emptiness and non-emptiness of their children. */
    public static final List<Class<?>> DOMAIN_TREE_TYPES = new ArrayList<Class<?>>() {{
	add(AbstractEntity.class); //
	add(SearchBy.class);
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
	add(IMasterDomainTreeManager.class); //
	add(IDomainTreeEnhancer.class); //
	add(IDomainTreeRepresentation.class); //
	add(IDomainTreeManager.class); //
	add(ITickRepresentation.class); //
	add(ITickManager.class); //
    }};
    private final static ISerialiser serialiser = createSerialiser(createFactory());
    protected static byte[] managerArray = null;
    private IDomainTreeManagerAndEnhancer dtm = null;

    /**
     * Returns a testing manager. Can be overridden to return specific manager for specific descendant test.
     *
     * @return
     */
    protected IDomainTreeManagerAndEnhancer dtm() {
	return dtm;
    }

    /**
     * Returns a serialiser instance for all tests.
     *
     * @return
     */
    protected static ISerialiser serialiser() {
	return serialiser;
    }

    /**
     * Returns a factory instance for all tests.
     *
     * @return
     */
    protected static EntityFactory factory() {
	return serialiser.factory();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Before
    public void initEachTest() throws Exception {
	dtm = managerArray == null ? null : serialiser.deserialise(managerArray, IDomainTreeManagerAndEnhancer.class);
    }

    /**
     * Sets a concrete domain tree manager's byte array to be deserialised and used in all tests.
     *
     * @param dtmArray
     */
    protected static void setDtmArray(final byte[] dtmArray) {
        AbstractDomainTreeTest.managerArray = dtmArray;
    }

    private static EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	return injector.getInstance(EntityFactory.class);
    }

    private static ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryoForDomainTreesTestingPurposes(factory, new ClassProviderForTestingPurposes());
    }

    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
	rootTypes.add(MasterEntity.class);
	rootTypes.add(EvenSlaverEntity.class);
	rootTypes.add(EntityWithNormalNature.class);
	rootTypes.add(EntityWithStringKeyType.class);
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_AbstractDomainTreeTest(final IDomainTreeManager dtm) {
	dtm.getRepresentation().excludeImmutably(EvenSlaverEntity.class, "");
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getRepresentation().excludeImmutably(MasterEntity.class, name);
	    }
	}, "excludedManuallyProp");

	dtm.getRepresentation().getFirstTick().disableImmutably(EntityWithNormalNature.class, "");
	dtm.getRepresentation().getSecondTick().disableImmutably(EntityWithNormalNature.class, "");
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, name);
		dtm.getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, name);
	    }
	}, "disabledManuallyProp");

	dtm.getRepresentation().getFirstTick().checkImmutably(EntityWithStringKeyType.class, "");
	dtm.getRepresentation().getSecondTick().checkImmutably(EntityWithStringKeyType.class, "");
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getRepresentation().getFirstTick().checkImmutably(MasterEntity.class, name);
		dtm.getRepresentation().getSecondTick().checkImmutably(MasterEntity.class, name);
	    }
	}, "checkedManuallyProp");

	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getRepresentation().getSecondTick().checkImmutably(MasterEntity.class, name);
	    }
	}, "immutablyCheckedUntouchedProp");
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// Utilities ///////////////////////////////
    ////////////////////////////////////////////////////////////////

    protected void checkOrSetMethodValues(final Object value, final String property, final Object instance, final String methodName, final Class<?> ... setterArg) {
	allLevels(new IAction() {
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

    protected void checkOrSetMethodValuesForNonCollectional(final Object value, final String property, final Object instance, final String methodName, final Class<?> ... setterArg) {
	allLevelsWithoutCollections(new IAction() {
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

    protected void checkOrSetMethodValuesForOneLevel(final Object value, final String property, final Object instance, final String methodName, final Class<?> ... setterArg) {
	oneLevel(new IAction() {
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
    public static void oneLevel(final IAction action, final String ... atomicNames) {
	for (final String atomicName : atomicNames) {
	    action.action(atomicName);
	}
    }

    /**
     * Runs simply an action for all appropriate levels of properties :
     *
     * <pre>
     * action.action(atomicName);
     * action.action("entityProp." + atomicName);
     * action.action("entityProp.entityProp." + atomicName);
     * action.action("collection." + atomicName);
     * action.action("entityProp.collection." + atomicName);
     * action.action("entityProp.collection.slaveEntityProp." + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevels(final IAction action, final String ... atomicNames) {
	allLevelsWithoutCollections(action, atomicNames);
	allLevelsWithCollections(action, atomicNames);
    }

    /**
     * Runs simply an action for levels without collections:
     *
     * <pre>
     * action.action(atomicName);
     * action.action("entityProp." + atomicName);
     * action.action("entityProp.entityProp." + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevelsWithoutCollections(final IAction action, final String ... atomicNames) {
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
     * action.action("collection." + atomicName);
     * action.action("entityProp.collection." + atomicName);
     * action.action("entityProp.collection.slaveEntityProp." + atomicName);
     * </pre>
     *
     * @param action
     * @param atomicNames
     */
    public static void allLevelsWithCollections(final IAction action, final String ... atomicNames) {
	for (final String atomicName : atomicNames) {
	    action.action("".equals(atomicName) ? "collection" : ("collection" + "." + atomicName));
	    action.action("".equals(atomicName) ? "entityProp.collection" : ("entityProp.collection" + "." + atomicName));
	    action.action("".equals(atomicName) ? "entityProp.collection.slaveEntityProp" : ("entityProp.collection.slaveEntityProp" + "." + atomicName));
	}
    }

    protected ISerialiser getSerialiser() {
	return serialiser;
    }

    /**
     * Returns <code>true</code> if all desired fields (recursive) are initialised, <code>false</code> otherwise.
     *
     * @param instance
     * @return
     */
    protected static boolean allDomainTreeFieldsAreInitialised(final Object instance) {
	return allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(instance, null);
    }


    /**
     * Returns <code>true</code> if all desired fields (recursive) are initialised, <code>false</code> otherwise.
     *
     * @param instance
     * @return
     */
    protected static boolean allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(final Object instance, final Object originalInstance) {
	final List<Field> fields = getDomainTreeFields(instance.getClass());
	try {
	    for (final Field field : fields) {
		// System.err.println("Instance = [" + instance + "], field = [" + field + "].");
		final Object fieldValue = Finder.getFieldValue(field, instance);
		// System.out.println("allDomainTreeFieldsAreInitialised : Field [" + field + "]; value [" + fieldValue + "];");
		// all field values should be initialised! Including transient fields, domain tree types, maps, etc.
		if (fieldValue == null) {
		    fail("After deserialisation of the manager all the fields should be initialised (including transient). But field's [" + field + "] value is null for instance [" + instance + "].");
		    return false;
		} else {
		    final Class<?> fieldValueType = fieldValue.getClass();
		    if (!Modifier.isStatic(field.getModifiers()) && originalInstance != null && !EntityUtils.isEnum(fieldValueType)) {
			final Object originalFieldValue = Finder.getFieldValue(field, originalInstance);
			assertFalse("The references of corresponding fields should be distinct. Field = [" + field + "]; value original = [" + originalFieldValue + "]; value new = [" + fieldValue + "].", fieldValue == originalFieldValue);
		    }
		    // all non-transient domain-tree-typed fields should have children initialised!
		    if (!Modifier.isTransient(field.getModifiers()) && Finder.isAssignableFrom(fieldValueType, DOMAIN_TREE_TYPES) && !EntityUtils.isEnum(fieldValueType) && !allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(fieldValue, originalInstance != null ? Finder.getFieldValue(field, originalInstance) : null)) {
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

    protected static List<Field> getDomainTreeFields(final Class<?> type) {
	// A base types to be checked for its non-emptiness and non-emptiness of their children.
	final List<Class<?>> types = new ArrayList<Class<?>>(DOMAIN_TREE_TYPES);
	// A base types to be checked for its non-emptiness.
	// covered by EnhancementSetAndMaps? types.add(Set.class);
	// covered by EnhancementSetAndMaps? types.add(Map.class);
	types.add(Enum.class); // CalculatedProperty implementation
	types.add(String.class); // CalculatedProperty implementation
	types.add(Class.class); // CalculatedProperty implementation
	return Finder.getFieldsOfSpecifiedTypes(type, types);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ////////////////////// 7. Persistence, equality & comparison //////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    protected void test_that_manager_instantiation_works_for_inner_cross_references(final IDomainTreeManagerAndEnhancer dtm) {
	final AbstractDomainTreeManagerAndEnhancer dtme = (AbstractDomainTreeManagerAndEnhancer) dtm;
	final AbstractDomainTreeManager abstractDtm = (AbstractDomainTreeManager) dtme.base();
	final AbstractDomainTreeRepresentation dtr = (AbstractDomainTreeRepresentation) abstractDtm.getRepresentation();

	// check representation cross-references on itself (first / second representation ticks)
	assertNotNull("Should be not null.", dtr);
	final AbstractTickRepresentation firstTick = (AbstractTickRepresentation) dtr.getFirstTick();
	assertNotNull("Should be not null.", firstTick);
	assertNotNull("Should be not null.", firstTick.getDtr());
	assertTrue("Should be identical.", dtr == firstTick.getDtr());

	final AbstractTickRepresentation secondTick = (AbstractTickRepresentation) dtr.getSecondTick();
	assertNotNull("Should be not null.", secondTick);
	assertNotNull("Should be not null.", secondTick.getDtr());
	assertTrue("Should be identical.", dtr == secondTick.getDtr());

	final TickManager firstTm = (TickManager) abstractDtm.getFirstTick();
	assertNotNull("Should be not null.", firstTm);
	assertNotNull("Should be not null.", firstTm.tr());
	assertNotNull("Should be not null.", firstTm.dtr());
	assertTrue("Should be identical.", firstTick == firstTm.tr());
	assertTrue("Should be identical.", dtr == firstTm.dtr());

	final TickManager secondTm = (TickManager) abstractDtm.getSecondTick();
	assertNotNull("Should be not null.", secondTm);
	assertNotNull("Should be not null.", secondTm.tr());
	assertNotNull("Should be not null.", secondTm.dtr());
	assertTrue("Should be identical.", secondTick == secondTm.tr());
	assertTrue("Should be identical.", dtr == secondTm.dtr());
    }

    @Test
    public void test_that_serialisation_works() throws Exception {
	if (dtm() != null) {
	    final IDomainTreeManagerAndEnhancer dtm = dtm();
	    assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm));
	    test_that_manager_instantiation_works_for_inner_cross_references(dtm());

	    // test that serialisation works
	    final byte[] array = getSerialiser().serialise(dtm);
	    assertNotNull("Serialised byte array should not be null.", array);
	    final IDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
	    // final ICriteriaDomainTreeManager copy = getSerialiser().deserialise(array, ICriteriaDomainTreeManager.class);
	    // final CriteriaDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, CriteriaDomainTreeManagerAndEnhancer.class);
	    assertNotNull("Deserialised instance should not be null.", copy);

	    // after deserialisation the instance should be fully defined (even for transient fields).
	    // for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	    // So it should be checked:
	    assertTrue("After deserialisation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm));
	    test_that_manager_instantiation_works_for_inner_cross_references(copy);
	}
    }

    @Test
    public void test_that_equality_and_copying_works() {
	if (dtm() != null) {
	    final IDomainTreeManagerAndEnhancer dtm = dtm();
	    dtm.getEnhancer().apply();
	    assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm));

	    final IDomainTreeManagerAndEnhancer copy = EntityUtils.deepCopy(dtm, getSerialiser());
	    // after copying the instance should be fully defined (even for transient fields).
	    // for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	    // So it should be checked:
	    assertTrue("After coping of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm));
	    test_that_manager_instantiation_works_for_inner_cross_references(copy);
	    assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(copy, dtm));
	}
    }

    protected static final IDomainTreeManagerAndEnhancer enhanceDomainWithCalculatedPropertiesOfDifferentTypes(final IDomainTreeManagerAndEnhancer dtm) {
	// enhance domain to 1) check whether the inherited representation logic will be ok 2) check calculated properties representation
	// EXPRESSION
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "2 * integerProp", "Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "2 * integerProp", "Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	// AGGREGATED_EXPRESSION
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "SUM(integerProp)", "Aggr Expr Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "SUM(integerProp)", "Aggr Expr Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	// COLLECTIONAL_EXPRESSION
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Coll Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "2 * integerProp", "Coll Expr Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	// 				TODO AGGREGATED_COLLECTIONAL_EXPRESSION
	// 				TODO dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "SUM(integerProp)", "Aggr Coll Expr Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	// 				TODO dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "SUM(integerProp)", "Aggr Coll Expr Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Attr Coll Expr Prop1", "desc", CalculatedPropertyAttribute.ALL, "integerProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "2 * integerProp", "Attr Coll Expr Prop2", "desc", CalculatedPropertyAttribute.ANY, "integerProp");
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
	} catch (final IllegalArgumentException e) {
	    fail("The calculated property [" + calcProperty + "] should not be excluded. The path towards calculated property should be properly resolved.");
	}
    }

    @Test
    public void test_that_domain_tree_enhancements_work_as_expected_for_original_and_copied_manager() {
	if (dtm() != null) {
	    enhanceDomainWithCalculatedPropertiesOfDifferentTypes(dtm());
	    // to perform such a test it is enough to ask if the added calc properties can be asked for "excludement / disablement" (and the state is appropriate), after manual "disabling / excluding".
	    // It will process the domain tree to the needed level of enhanced hierarchy.
	    final IDomainTreeManagerAndEnhancer dtm = dtm();

	    dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "7 * integerProp", "Calculated Property", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	    dtm.getEnhancer().apply();

	    checkAccessabilityOfCalculatedPropertiesAndTheirState(dtm);

	    final IDomainTreeManagerAndEnhancer copy = EntityUtils.deepCopy(dtm, getSerialiser());
	    checkAccessabilityOfCalculatedPropertiesAndTheirState(copy);

	    copy.getEnhancer().getCalculatedProperty(MasterEntity.class, "calculatedProperty").contextType();
	}
    }

    protected static String name(final String path, final String name) {
        return "".equals(path) ? name : path + "." + name;
    }

    protected static void illegalAllLocatorActions(final ILocatorManager lm, final String message, final String name) {
	// locators
	try {
	    lm.refreshLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
	try {
	    lm.resetLocatorManagerToDefault(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}

	try {
	    lm.acceptLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
	try {
	    lm.discardLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}

	try {
	    lm.saveLocatorManagerGlobally(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
	try {
	    lm.freezeLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}

	try {
	    lm.getLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
	try {
	    lm.phaseAndTypeOfLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
	try {
	    lm.isChangedLocatorManager(MasterEntity.class, name);
	    fail(message);
	} catch (final IllegalArgumentException e) {
	}
    }
}