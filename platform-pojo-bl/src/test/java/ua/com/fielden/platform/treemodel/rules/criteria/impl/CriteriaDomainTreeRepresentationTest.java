package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EntityWithCompositeKey;
import ua.com.fielden.platform.domain.tree.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domain.tree.EvenSlaverEntity;
import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.domain.tree.MasterSyntheticEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeManager.ICriteriaDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentationTest;
import ua.com.fielden.platform.treemodel.rules.impl.CalculatedProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for entity centres tree representation.
 *
 * @author TG Team
 *
 */
public class CriteriaDomainTreeRepresentationTest extends AbstractDomainTreeRepresentationTest {
    @Override
    protected ICriteriaDomainTreeManagerAndEnhancer dtm() {
	return (ICriteriaDomainTreeManagerAndEnhancer) super.dtm();
    }

    @Override
    protected Set<Class<?>> createRootTypes() {
	final Set<Class<?>> rootTypes = super.createRootTypes();
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	return rootTypes;
    }

    @Override
    protected ICriteriaDomainTreeManagerAndEnhancer createManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	return new CriteriaDomainTreeManagerAndEnhancer(serialiser, rootTypes);
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Excluding logic //////////////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 2.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_first_tick_disabling() {
	super.test_entities_itself_first_tick_disabling();
	assertTrue("Entity itself (represented by empty 'property') with composite key type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("Entity itself (represented by empty 'property') with AE key type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
    }

    ////////////////////// 2.2. Annotation related logic //////////////////////
    @Test
    public void test_that_first_tick_for_crit_only_properties_are_disabled() {
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    @Test
    public void test_that_first_tick_for_result_only_properties_and_their_children_are_disabled() {
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp"));
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp"));
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp"));
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp"));
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp"));
	assertTrue("Result-only property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp"));

	// (1-level children)
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp.integerProp"));

	// (2-level children)
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp.entityProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp.slaveEntityProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp.entityProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp.slaveEntityProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp.entityProp.integerProp"));
	assertTrue("Result-only property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp.slaveEntityProp.integerProp"));
    }

    ////////////////////// 2.3. Type related logic //////////////////////
    @Test
    public void test_that_first_tick_for_properties_of_entity_with_AE_or_composite_key_type_are_disabled() {
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityPropWithAEKeyType"));

	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityWithCompositeKeyProp"));
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_second_tick_disabling() {
	super.test_entities_itself_second_tick_disabling();
	assertTrue("Entity itself (represented by empty 'property') with composite key type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("Entity itself (represented by empty 'property') with AE key type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
	assertTrue("An synthetic entity itself (represented by empty 'property') should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterSyntheticEntity.class, ""));
    }

    ////////////////////// 3.2. Annotation related logic //////////////////////
    @Test
    public void test_that_second_tick_for_crit_only_properties_are_disabled() {
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
	assertTrue("Crit-only property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    @Test
    public void test_that_second_tick_for_collectional_properties_children_are_disabled() {
	// (1-level children)
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.collection.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.collection.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.collection.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.collection.integerProp"));

	// (2-level children)
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.collection.entityProp.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.collection.slaveEntityProp.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.collection.entityProp.integerProp"));
	assertTrue("Collectional property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.collection.slaveEntityProp.integerProp"));
    }

    ////////////////////// 3.3. Type related logic //////////////////////
    @Test
    public void test_that_second_tick_for_properties_of_entity_with_AE_or_composite_key_type_are_disabled() {
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityPropWithAEKeyType"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityPropWithAEKeyType"));

	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityWithCompositeKeyProp"));
	assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityWithCompositeKeyProp"));
    }

    ////////////////////// 4.2. Annotation related logic //////////////////////
    @Test
    public void test_that_first_tick_for_crit_only_properties_are_checked() {
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "critOnlyProp"));
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "collection.critOnlyProp"));
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
	assertTrue("Crit-only property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    ////////////////////// 4.3. Disabling of immutable checked properties //////////////////////
    @Override
    @Test
    public void test_that_checked_properties_first_tick_are_actually_disabled() {
	super.test_that_checked_properties_first_tick_are_actually_disabled();
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
	assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    ////////////////////// 5. Calculated properties logic //////////////////////
    @Test
    public void test_that_first_tick_for_AGGR_EXPR_calculated_properties_are_disabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertTrue("AGGREGATED EXPRESSION calculated properties should be disabled for first tick.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "aggrExprProp");
    }

    @Test
    public void test_that_second_tick_for_ATTR_COLL_EXPR_calculated_properties_are_disabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "collection.integerProp", Integer.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertTrue("ATTRIBUTED COLLECTIONAL EXPRESSION calculated properties should be disabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "attrCollExprProp");
    }

    ////////////////////// 6. Specific entity-centre logic //////////////////////
    @Test
    public void test_that_excluded_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Excluded property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set for value1/2 by default
		try {
		    dtm().getRepresentation().getFirstTick().getValueByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getFirstTick().setValueByDefault(MasterEntity.class, name, "a value");
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getFirstTick().getValue2ByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getFirstTick().setValue2ByDefault(MasterEntity.class, name, "a value");
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}

		// isOrderingDisabled/disableOrdering
		try {
		    dtm().getRepresentation().getSecondTick().isOrderingDisabledImmutably(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}

		// get/set Ordering by default
		try {
		    dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EvenSlaverEntity.class);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().setOrderedPropertiesByDefault(EvenSlaverEntity.class, new ArrayList<Pair<String, Ordering>>());
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}

		// get/set width by default
		try {
		    dtm().getRepresentation().getSecondTick().getWidthByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().setWidthByDefault(MasterEntity.class, name, 85);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");
    }

    @Test
    public void test_that_values_1_and_2_by_default_for_first_tick_are_desired_and_can_be_altered() {
	checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues("", "stringProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues("", "stringProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues(true, "booleanProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(true, "booleanProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues(null, "dateProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(null, "dateProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues(null, "integerProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(null, "integerProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
	checkOrSetMethodValues(null, "moneyProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(null, "moneyProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	final Date d = new Date();
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");

	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "getValueByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "getValue2ByDefault");
    }

    @Test
    public void test_that_widths_by_default_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// default width should be 80
	checkOrSetMethodValues(80, "dateProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");
	checkOrSetMethodValues(80, "integerProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");

	// Alter DEFAULT and check //
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");
    }

    @Test
    public void test_that_ordering_disablements_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// none of the properties should have ordering disabled by default.
	checkOrSetMethodValues(false, "dateProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");
	checkOrSetMethodValues(false, "integerProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");

	// Alter DEFAULT and check //
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm().getRepresentation().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
	    }
	}, "dateProp");
	checkOrSetMethodValues(true, "dateProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");
    }

    @Test
    public void test_that_orderings_by_default_for_second_tick_are_desired_and_can_be_altered(){
	// DEFAULT CONTRACT //
	// entities with simple key should have ASC ordering on that key
	assertEquals("Default value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	// entities with composite and other complicated key should have no ordering applied
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EntityWithCompositeKey.class));
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter DEFAULT and check //
	final List<Pair<String, Ordering>> ordering = Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING));
	dtm().getRepresentation().getSecondTick().setOrderedPropertiesByDefault(MasterEntity.class, ordering);
	assertEquals("Default value is incorrect.", ordering, dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
    }
}
