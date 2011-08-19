package ua.com.fielden.platform.treemodel.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EntityWithNormalNature;
import ua.com.fielden.platform.domain.tree.EntityWithStringKeyType;
import ua.com.fielden.platform.domain.tree.EvenSlaverEntity;
import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.domain.tree.TgKryo1;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A test for base TG domain tree representation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeTest {
    private final ISerialiser serialiser;
    private final IDomainTreeManagerAndEnhancer dtm;
    {
	dtm = createManager(serialiser = createSerialiser(createFactory()), createRootTypes());
	if (dtm != null) {
	    manageTestingDTM(dtm);
	    enhanceDomainWithCalculatedPropertiesOfDifferentTypes(dtm);
	}
    }

    /**
     * Returns a testing manager. Can be overridden to return specific manager for specific descendant test.
     *
     * @return
     */
    protected IDomainTreeManagerAndEnhancer dtm() {
	return dtm;
    }

    /**
     * Creates root types.
     *
     * @return
     */
    protected Set<Class<?>> createRootTypes() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
	rootTypes.add(MasterEntity.class);
	rootTypes.add(EvenSlaverEntity.class);
	rootTypes.add(EntityWithNormalNature.class);
	rootTypes.add(EntityWithStringKeyType.class);
	return rootTypes;
    }

    protected static EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = Guice.createInjector(module);
	return injector.getInstance(EntityFactory.class);
    }

    protected static ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryo1(factory, new ProvidedSerialisationClassProvider());
    }

    /**
     * Creates testing manager.
     * @param serialiser TODO
     * @param rootTypes
     *
     * @return
     */
    protected abstract IDomainTreeManagerAndEnhancer createManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes);

    ////////////////////////////////////////////////////////////////
    ////////////////////// Utilities ///////////////////////////////
    ////////////////////////////////////////////////////////////////
    protected IDomainTreeManager enhanceDomainWithCalculatedPropertiesOfDifferentTypes(final IDomainTreeManagerAndEnhancer managerAndEnhancer) {
	// enhance domain to 1) check whether the inherited representation logic will be ok 2) check calculated properties representation
	// EXPRESSION
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "exprProp", CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.exprProp", CalculatedPropertyCategory.EXPRESSION, "entityProp.integerProp", Integer.class, "expr", "title", "desc"));
	// AGGREGATED_EXPRESSION
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "aggrExprProp", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.aggrExprProp", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "entityProp.integerProp", Integer.class, "expr", "title", "desc"));
	// COLLECTIONAL_EXPRESSION
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.collection.collExprProp", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "entityProp.collection.integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp.collExprProp", CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "entityProp.collection.simpleEntityProp.integerProp", Integer.class, "expr", "title", "desc"));
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.aggrCollExprProp1", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "entityProp.collection.integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.aggrCollExprProp2", CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, "entityProp.collection.simpleEntityProp.integerProp", Integer.class, "expr", "title", "desc"));
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.attrCollExprProp1", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "entityProp.collection.integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "entityProp.attrCollExprProp2", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "entityProp.collection.simpleEntityProp.integerProp", Integer.class, "expr", "title", "desc"));
	managerAndEnhancer.getEnhancer().apply();
	return managerAndEnhancer;
    }

    /**
     * Provides a testing configuration for the representation.
     *
     * @param dtr
     */
    protected void manageTestingDTM(final IDomainTreeManager dtm) {
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
	    action.action("entityProp." + atomicName);
	    action.action("entityProp.entityProp." + atomicName);
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
	    action.action("collection." + atomicName);
	    action.action("entityProp.collection." + atomicName);
	    action.action("entityProp.collection.slaveEntityProp." + atomicName);
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
		final Object fieldValue = Finder.getFieldValue(field, instance);
		// System.out.println("allDomainTreeFieldsAreInitialised : Field [" + field + "]; value [" + fieldValue + "];");
		// all field values should be initialised! Including transient fields, domain tree types, maps, etc.
		if (fieldValue == null) {
		    return false;
		} else {
		    if (!Modifier.isStatic(field.getModifiers()) && originalInstance != null) {
			final Object originalFieldValue = Finder.getFieldValue(field, originalInstance);
			assertFalse("The references of corresponding fields should be distinct. Field = [" + field + "]; value original = [" + originalFieldValue + "]; value new = [" + fieldValue + "].", fieldValue == originalFieldValue);
		    }
		    final Class<?> fieldValueType = fieldValue.getClass();
		    // all non-transient domain-tree-typed fields should have children initialised!
		    if (!Modifier.isTransient(field.getModifiers()) && Finder.isAssignableFrom(fieldValueType, AbstractDomainTree.DOMAIN_TREE_TYPES) && !allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(fieldValue, originalInstance != null ? Finder.getFieldValue(field, originalInstance) : null)) {
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
	final List<Class<?>> types = new ArrayList<Class<?>>(AbstractDomainTree.DOMAIN_TREE_TYPES);
	// A base types to be checked for its non-emptiness.
	types.add(Set.class);
	types.add(Map.class);
	types.add(Enum.class); // CalculatedProperty implementation
	types.add(String.class); // CalculatedProperty implementation
	types.add(Class.class); // CalculatedProperty implementation
	// TODO ?
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

    @Test
    public void test_that_equality_and_copying_works() {
	final IDomainTreeManagerAndEnhancer dtm = dtm();
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