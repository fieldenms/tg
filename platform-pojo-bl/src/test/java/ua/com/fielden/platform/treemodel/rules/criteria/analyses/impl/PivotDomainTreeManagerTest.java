package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EntityWithCompositeKey;
import ua.com.fielden.platform.domain.tree.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeManagerTest;
import ua.com.fielden.platform.treemodel.rules.impl.CalculatedProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;


/**
 * A test for {@link AbstractAnalysisDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManagerTest extends AbstractDomainTreeManagerTest {
    @Override
    protected IDomainTreeManagerAndEnhancer createManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	return new PivotDomainTreeManagerAndEnhancer(serialiser, rootTypes);
    }

    @Override
    protected IAbstractAnalysisDomainTreeManagerAndEnhancer dtm() {
	return (IAbstractAnalysisDomainTreeManagerAndEnhancer)super.dtm();
    }

    @Override
    protected Set<Class<?>> createRootTypes() {
	final Set<Class<?>> rootTypes = super.createRootTypes();
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	return rootTypes;
    }

    @Override
    protected void manageTestingDTM(final IDomainTreeManager dtm) {
	super.manageTestingDTM(dtm);
	dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "intAggExprProp", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "expr", "title", "desc"));
	dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "bigDecimalAggExprProp", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "bigDecimalProp", BigDecimal.class, "expr", "title", "desc"));
	dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "moneyAggExprProp", CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "moneyProp", Money.class, "expr", "title", "desc"));
	dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "dateExprProp", CalculatedPropertyCategory.EXPRESSION, "dateProp", Integer.class, "expr", "title", "desc"));
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "moneyProp", Money.class, "expr", "title", "desc"));
	    }
	}, "uncheckedAggExprProp");
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, "dateExprProp", CalculatedPropertyCategory.EXPRESSION, "dateProp", Integer.class, "expr", "title", "desc"));
	    }
	}, "uncheckedDateExprProp");
	dtm().getEnhancer().apply();
	dtm().getSecondTick().check(MasterEntity.class, "intAggExprProp", true);
	dtm().getSecondTick().check(MasterEntity.class, "bigDecimalAggExprProp", true);
	dtm().getSecondTick().check(MasterEntity.class, "moneyAggExprProp", true);
	dtm().getFirstTick().check(MasterEntity.class, "dateExprProp", true);
	dtm().getFirstTick().check(MasterEntity.class, "simpleEntityProp", true);
	dtm().getFirstTick().check(MasterEntity.class, "booleanProp", true);
    }

    @Override
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
	// this test is redundant due to lack of special isChecked logic in AnalysisDomainTreeManager
    }

    @Override
    @Test
    public void test_that_CHECK_state_for_mutated_by_Check_method_properties_is_actually_mutated() {
	// checked properties, mutated_by_Check_method
	final String message = "Checked property, defined in isChecked() contract, should return 'true' CHECK state, and after manual mutation its state should be desired.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getFirstTick().check(MasterEntity.class, name, false);
		isCheck_equals_to_state(name, message, false);
		dtm().getFirstTick().check(MasterEntity.class, name, true);
		isCheck_equals_to_state(name, message, true);
	    }
	}, "mutablyCheckedProp");
    }


    //////////////////////1. Specific analysis-manager logic //////////////////////

    @Test
    public void test_that_manager_visibility_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be TRUE
	assertTrue("Analysis manager by default should be visible", dtm().isVisible());

	// Alter and check //
	assertTrue("The manager reference should be the same",dtm() == dtm().setVisible(false));
	assertFalse("Analysis manager should be invisible", dtm().isVisible());
    }

    @Test
    public void test_that_unchecked_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Unchecked property should cause IllegalArgument exception.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		// FIRST TICK
		// usage manager
		try {
		    dtm().getFirstTick().isUsed(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().use(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "uncheckedDateExprProp");
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		// SECOND TICK
		//usage manager
		try {
		    dtm().getSecondTick().isUsed(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().use(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// ordering
		try {
		    dtm().getSecondTick().toggleOrdering(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "uncheckedAggExprProp");
    }

    @Test
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered(){
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// Default ordering for the analysis's second tick should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithCompositeKey.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter and check //
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.DESCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
    }

    @Test
    @Ignore
    public void test_that_usgae_management_works_correctly_for_first_tick(){
	//At the beginning the list of used properties should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));

	//Add "use properties" and see whether list of "used properties" is correctly ordered.
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("dateExprProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityPropProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityPropProp"));
	assertEquals("value is incorrect.", Arrays.asList("dateExprProp", "simpleEntityProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityProp"));
	assertEquals("value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
    }

    @Test
    @Ignore
    public void test_that_usgae_management_works_correctly_for_second_tick(){
	//At the beginning the list of used properties should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().usedProperties(MasterEntity.class));

	//Add "use properties" and see whether list of "used properties" is correctly ordered.
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "moneyAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "intAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("intAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "bigDecimalAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("intAggExprProp", "bigDecimalAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "intAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("bigDecimalAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "moneyAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("bigDecimalAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "bigDecimalAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList(), dtm().getSecondTick().usedProperties(MasterEntity.class));
    }

    @Override
    public void test_that_CHECKed_properties_order_is_correct() throws Exception {
    }
    
    @Override
    public void test_that_CHECKed_properties_Move_Swap_operations_work() throws Exception {
    }
}
