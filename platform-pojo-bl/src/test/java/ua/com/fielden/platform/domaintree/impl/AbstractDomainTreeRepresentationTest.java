package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.EnhancingSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithoutKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;

/**
 * A test for base TG domain tree representation.
 *
 * @author TG Team
 *
 */
public class AbstractDomainTreeRepresentationTest extends AbstractDomainTreeTest {
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractDomainTreeTest();
	rootTypes.add(MasterEntityForIncludedPropertiesLogic.class);
	rootTypes.add(MasterEntityWithUnionForIncludedPropertiesLogic.class);
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_AbstractDomainTreeRepresentationTest(final IDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractDomainTreeTest(dtm);

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, name, "1 * integerProp", "Calc integer prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtm.getEnhancer().apply();
	    }
	}, "");

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IDomainTreeManagerAndEnhancer dtm = new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractDomainTreeRepresentationTest());
	manageTestingDTM_for_AbstractDomainTreeRepresentationTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Excluding logic //////////////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 1.0. Non-TG properties excluding logic //////////////////////
    @Test
    public void test_entities_itself_excluding() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be excluded if it is a root type.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, ""));
	assertFalse("An entity itself (represented by empty 'property') should NOT be excluded if it is a root type.", dtm().getRepresentation().isExcludedImmutably(EntityWithNormalNature.class, ""));
	assertFalse("An entity itself (represented by empty 'property') should NOT be excluded if it is a root type.", dtm().getRepresentation().isExcludedImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("An entity itself (represented by empty 'property') should be excluded if it is not a root type.", dtm().getRepresentation().isExcludedImmutably(EntityWithoutKeyType.class, ""));

	assertTrue("A first level property should be excluded if its parent type is not a root type.", dtm().getRepresentation().isExcludedImmutably(EnhancingSlaveEntity.class, "slaveEntityProp"));

	assertTrue("Manually excluded entity itself (represented by empty 'property') should be excluded (even if it is a root type).", dtm().getRepresentation().isExcludedImmutably(EvenSlaverEntity.class, ""));
    }

    @Test
    public void test_that_non_TG_properties_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Non-TG property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "moneyProp.amount");
    }

    @Test
    public void test_that_non_existent_properties_cause_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name);
		    fail("Non-existent property should cause exception.");
		} catch (final Exception e) {
		}
	    }
	}, "moneyPropBeliberda");
    }

    ////////////////////// 1.1. Specific excluding logic //////////////////////
    @Test
    public void test_that_specifically_excluded_properties_are_actually_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Specifically excluded property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "excludedManuallyProp");
    }

    ////////////////////// 1.2. AbstractEntity specific properties logic ("key" and "desc") //////////////////////
    @Test
    public void test_that_Desc_properties_without_DescTitle_on_parent_type_are_excluded_and_otherwise_NOT_excluded() {
	assertTrue("'desc' property without 'DescTitle' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "desc"));
	assertFalse("'desc' property with 'DescTitle' on parent type should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.desc"));
	assertTrue("'desc' property without 'DescTitle' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.desc"));
	assertFalse("'desc' property with 'DescTitle' on parent type should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection.desc"));
	assertTrue("'desc' property without 'DescTitle' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.desc"));
	assertFalse("'desc' property with 'DescTitle' on parent type should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.desc"));
    }

    @Test
    public void test_that_Key_properties_with_AE_KeyType_and_with_KeyTitle_on_parent_type_are_NOT_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertFalse("'key' property without 'KeyType' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithAEKeyType.key");
    }

    @Test
    public void test_that_Key_properties_without_KeyType_on_parent_type_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("'key' property without 'KeyType' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithoutKeyType.key");
    }

    @Test
    public void test_that_Key_properties_without_KeyTitle_on_parent_type_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("'key' property without 'KeyTitle' on parent type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithoutKeyTitle.key");
    }

    @Test
    public void test_that_Key_properties_of_non_entity_type_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("'key' property of non-AE type should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "key");
    }

    ////////////////////// 1.3. Type related logic //////////////////////
    @Test
    public void test_that_enumeration_type_properties_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Enumeration property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "enumProp");
    }

    @Test
    public void test_that_collections_itself_are_NOT_excluded() {
	assertFalse("Collection itself should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection"));
	assertFalse("Collection itself should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection"));
    }

    @Test
    public void test_that_properties_of_entity_type_with_abstract_nature_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("AbstractEntity property with 'abstract' modifier should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithAbstractNature");
    }

    @Test
    public void test_that_properties_of_entity_type_without_KeyType_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("AbstractEntity property without 'KeyType' annotation should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithoutKeyType");
    }

    @Test
    public void test_that_cyclic_key_parts_are_excluded() {
	// the key parts properties which type was in hierarchy before should be excluded (e.g. "WorkOrder.vehicle.fuelUsages.vehicle" should be excluded and "WorkOrder.vehicle.replacing" should not be excluded)
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "entityWithCompositeKeyProp.keyPartProp");

	// test slave entity key part excludability
	assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityWithCompositeKeyProp.keyPartPropFromSlave"));
	assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.entityWithCompositeKeyProp.keyPartPropFromSlave"));
	assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection.entityWithCompositeKeyProp.keyPartPropFromSlave"));
	assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.entityWithCompositeKeyProp.keyPartPropFromSlave"));
	assertTrue("Cyclic key part property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityWithCompositeKeyProp.keyPartPropFromSlave"));
    }

    ////////////////////// 1.4. Annotation related logic //////////////////////
    @Test
    public void test_that_invisible_properties_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Invisible property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "invisibleProp");
    }

    @Test
    public void test_that_ignore_properties_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Ignore property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "ignoreProp");
    }

    @Test
    public void test_that_children_of_crit_only_AE_or_AE_collection_property_are_excluded() {
	// test that crit-only entity properties itself are included
	allLevels(new IAction() {
	    public void action(final String name) {
		assertFalse("Crit-only AE property/collection itself should not be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "critOnlyAEProp","critOnlyAECollectionProp");

	// test that crit-only entity properties children are excluded (1-level children)
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Crit-only AE property/collection child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "critOnlyAEProp.integerProp", "critOnlyAECollectionProp.integerProp");

	// test that crit-only entity properties children are excluded (2-level children)
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "critOnlyAEProp.entityProp.integerProp"));
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.critOnlyAEProp.slaveEntityProp.integerProp"));
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyAEProp.entityProp.integerProp"));
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection.critOnlyAEProp.slaveEntityProp.integerProp"));
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.critOnlyAEProp.entityProp.integerProp"));
	assertTrue("Crit-only AE property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyAEProp.slaveEntityProp.integerProp"));

	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "critOnlyAECollectionProp.entityProp.integerProp"));
	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.critOnlyAECollectionProp.slaveEntityProp.integerProp"));
	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyAECollectionProp.entityProp.integerProp"));
	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection.critOnlyAECollectionProp.slaveEntityProp.integerProp"));
	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.critOnlyAECollectionProp.entityProp.integerProp"));
	assertTrue("Crit-only AE collection property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyAECollectionProp.slaveEntityProp.integerProp"));
    }

    ////////////////////// 1.5. Recursive excluding logic //////////////////////
    @Test
    public void test_that_children_of_excluded_property_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Excluded property or child of excluded property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "excludedManuallyProp","excludedManuallyProp.integerProp");

	// test that excluded properties children are excluded (2-level children)
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "excludedManuallyProp.entityProp.integerProp"));
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.excludedManuallyProp.slaveEntityProp.integerProp"));
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.excludedManuallyProp.entityProp.integerProp"));
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "collection.excludedManuallyProp.slaveEntityProp.integerProp"));
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.excludedManuallyProp.entityProp.integerProp"));
	assertTrue("Excluded property child should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.excludedManuallyProp.slaveEntityProp.integerProp"));
    }

    ////////////////////// 1.6. Included properties with order //////////////////////
    @Test
    public void test_that_order_of_included_properties_is_correct_and_circular_references_manage_Dummy_property() {
	assertEquals("Not root type -- should return empty list of included properties.", Collections.emptyList(), dtm().getRepresentation().includedProperties(EntityWithoutKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp"), dtm().getRepresentation().includedProperties(EntityWithStringKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList(""), dtm().getRepresentation().includedProperties(EntityWithNormalNature.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// test that heavy-weight entities will be processed correctly
	dtm().getRepresentation().includedProperties(MasterEntity.class);
	dtm().getRepresentation().includedProperties(SlaveEntity.class);
	dtm().getRepresentation().includedProperties(EvenSlaverEntity.class);
    }

    @Test
    public void test_that_warming_up_of_included_properties_works_correctly() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	// warm up first-level circular property
	dtm().getRepresentation().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropCollection.moneyProp", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// warm up n-th-level circular property (for example third)
	dtm().getRepresentation().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.moneyProp", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropCollection.moneyProp", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_included_properties_for_union_entities_hierarchy_are_correct_and_manage_Common_and_Union_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "unionEntityProp", "unionEntityProp.common-properties", "unionEntityProp.common-properties.desc", "unionEntityProp.common-properties.commonProp", "unionEntityProp.unionProp1", "unionEntityProp.unionProp1.nonCommonPropFrom1", "unionEntityProp.unionProp2", "unionEntityProp.unionProp2.nonCommonPropFrom2"), dtm().getRepresentation().includedProperties(MasterEntityWithUnionForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_domain_changes_for_First_Level_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_domain_changes_for_Second_Level_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop1", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "2 * integerProp", "Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop1", "entityProp.prop2", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop2", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop2");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_domain_changes_for_Second_Level_circular_properties_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropCollection.moneyProp", "entityPropOfSelfType.prop1", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
    }

    @Test
    public void test_that_manual_exclusion_is_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getRepresentation().excludeImmutably(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection.integerProp");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_Included_Properties_are_correct_after_serialisation_deserialisation() throws Exception {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = getSerialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp"), copy.getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 2. 1-st tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 2.0. Non-applicable properties exceptions //////////////////////
    @Test
    public void test_entities_itself_first_tick_disabling() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("Manually disabled entity itself (represented by empty 'property') should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithNormalNature.class, ""));
    }

    @Test
    public void test_that_non_existent_properties_first_tick_disabling_cause_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Non-existent property should cause exception.");
		} catch (final Exception e) {
		}
	    }
	}, "moneyPropBeliberda");
    }

    @Test
    public void test_that_any_excluded_properties_first_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	// excluded manually stuff and excluded stuff manual disabling
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");


	oneLevel(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "entityProp.collection.slaveEntityProp.moneyProp.amount", //
		"collection.entityPropWithoutKeyType.key", //
		"entityProp.collection.enumProp", //
		"entityProp.collection.entityWithCompositeKeyProp.keyPartProp", //
		"entityProp.collection.critOnlyAECollectionProp.integerProp"); //
    }


    ////////////////////// 2.1. Specific disabling logic //////////////////////
    @Test
    public void test_that_specifically_disabled_properties_first_tick_are_actually_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Specifically disabled property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "disabledManuallyProp");
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////
    @Test
    public void test_entities_itself_second_tick_disabling() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("Manually disabled entity itself (represented by empty 'property') should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithNormalNature.class, ""));
    }

    @Test
    public void test_that_non_existent_properties_second_tick_disabling_cause_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Non-existent property should cause exception.");
		} catch (final Exception e) {
		}
	    }
	}, "moneyPropBeliberda");
    }

    @Test
    public void test_that_any_excluded_properties_second_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	// excluded manually stuff and excluded stuff manual disabling
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");


	oneLevel(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "entityProp.collection.slaveEntityProp.moneyProp.amount", //
		"collection.entityPropWithoutKeyType.key", //
		"entityProp.collection.enumProp", //
		"entityProp.collection.entityWithCompositeKeyProp.keyPartProp", //
		"entityProp.collection.critOnlyAECollectionProp.integerProp"); //
    }

    ////////////////////// 3.1. Specific disabling logic //////////////////////
    @Test
    public void test_that_specifically_disabled_properties_second_tick_are_actually_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Specifically disabled property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "disabledManuallyProp");
    }

    ////////////////////// 3.3. Type related logic //////////////////////
    @Test
    public void test_that_other_properties_are_not_disabled() {
	assertFalse("Should not be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "simpleEntityProp"));
	assertFalse("Should not be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertFalse("Should not be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.simpleEntityProp"));
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 4. 1-st tick checking+disabling logic ///
    ////////////////////////////////////////////////////////////////

    ////////////////////// 4.0. Non-applicable properties exceptions //////////////////////
    @Test
    public void test_entities_itself_first_tick_checking() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, ""));
	assertTrue("Manually checked entity itself (represented by empty 'property') should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("Manually checked entity itself (represented by empty 'property') should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
    }

    @Test
    public void test_that_non_existent_properties_first_tick_checking_and_isChecked_cause_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Non-existent property should cause exception.");
		} catch (final Exception e) {
		}
	    }
	}, "moneyPropBeliberda");
    }

    @Test
    public void test_that_any_excluded_properties_first_tick_checking_and_isChecked_cause_IllegalArgument_exception() {
	// excluded manually stuff and excluded stuff manual disabling
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getFirstTick().checkImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");

	oneLevel(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "entityProp.collection.slaveEntityProp.moneyProp.amount", //
		"collection.entityPropWithoutKeyType.key", //
		"entityProp.collection.enumProp", //
		"entityProp.collection.entityWithCompositeKeyProp.keyPartProp", //
		"entityProp.collection.critOnlyAECollectionProp.integerProp"); //
    }

    ////////////////////// 4.1. Specific checking logic //////////////////////
    @Test
    public void test_that_specifically_checked_properties_first_tick_are_actually_checked() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Specifically checked property should be checked.", dtm().getRepresentation().getFirstTick().isCheckedImmutably(MasterEntity.class, name));
	    }
	}, "checkedManuallyProp");
    }

    ////////////////////// 4.3. Disabling of immutable checked properties //////////////////////
    @Test
    public void test_that_checked_properties_first_tick_are_actually_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Checked property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "checkedManuallyProp");
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 5. 2-nd tick checking+disabling logic ///
    ////////////////////////////////////////////////////////////////

    ////////////////////// 5.0. Non-applicable properties exceptions //////////////////////
    @Test
    public void test_entities_itself_second_tick_checking() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be checked.", dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, ""));
	assertTrue("Manually checked entity itself (represented by empty 'property') should be checked.", dtm().getRepresentation().getSecondTick().isCheckedImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("Manually checked entity itself (represented by empty 'property') should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
    }

    @Test
    public void test_that_non_existent_properties_second_tick_checking_and_isChecked_cause_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Non-existent property should cause exception.");
		} catch (final Exception e) {
		}
	    }
	}, "moneyPropBeliberda");
    }

    @Test
    public void test_that_any_excluded_properties_second_tick_checking_and_isChecked_cause_IllegalArgument_exception() {
	// excluded manually stuff and excluded stuff manual disabling
	allLevels(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().checkImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");

	oneLevel(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "entityProp.collection.slaveEntityProp.moneyProp.amount", //
		"collection.entityPropWithoutKeyType.key", //
		"entityProp.collection.enumProp", //
		"entityProp.collection.entityWithCompositeKeyProp.keyPartProp", //
		"entityProp.collection.critOnlyAECollectionProp.integerProp"); //
    }

    ////////////////////// 5.1. Specific checking logic //////////////////////
    @Test
    public void test_that_specifically_checked_properties_second_tick_are_actually_checked() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Specifically checked property should be checked.", dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, name));
	    }
	}, "checkedManuallyProp");
    }

    ////////////////////// 5.2. Disabling of immutable checked properties //////////////////////
    @Test
    public void test_that_checked_properties_second_tick_are_actually_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Checked property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "checkedManuallyProp");
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////// 6. Available functions.//////////////////////
    ////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_integer_calculated_property_has_functions_with_Count_Distinct() {
	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.EXPRESSION based on date property.
	final String m = "Available functions are incorrect.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "YEAR(dateProp)", "Integer Prop Originated From Date", "desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
		dtm().getEnhancer().apply();
		assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "integerPropOriginatedFromDate"))));
	    }
	}, ""); // integerPropOriginatedFromDate

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION based on date property.
	allLevelsWithCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "YEAR(dateProp)", "Integer Prop Originated From Date", "desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
		dtm().getEnhancer().apply();
		assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "integerPropOriginatedFromDate"))));
	    }
	}, ""); // integerPropOriginatedFromDate

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on entity property.
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(entityProp)", "Calc From Entity Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(entityProp)", "Calc From Entity Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(entityProp)", "Calc From Entity Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromEntityProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp2")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp3")));

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on boolean property.
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(booleanProp)", "Calc From Boolean Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(booleanProp)", "Calc From Boolean Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(booleanProp)", "Calc From Boolean Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromBooleanProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp2")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp3")));

	/////////////////////////////////////////////////// There are no count distinct //////////////////////////////////////////////
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "2 * integerProp", "Calculated Integer Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtm().getEnhancer().apply();
		assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "calculatedIntegerProp"))));
	    }
	}, ""); // calculatedIntegerProp

	allLevelsWithCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "2 * integerProp", "Calculated Integer Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtm().getEnhancer().apply();
		assertEquals(m, enhanceFunctionsWithCollectionalAttributes(Arrays.asList(Function.SELF,  Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "calculatedIntegerProp"))));
	    }
	}, ""); // calculatedIntegerProp

	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "COUNT(integerProp)", "Calc From Integer Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "COUNT(integerProp)", "Calc From Integer Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.entityProp", "COUNT(integerProp)", "Calc From Integer Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp2")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp3")));
    }

    @Test
    public void test_which_calculated_property_categories_have_which_functions() {
	enhanceDomainWithCalculatedPropertiesOfDifferentTypes(dtm());
	final String m = "Available functions are incorrect.";

	// EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "exprProp")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.exprProp")));
	// AGGREGATED_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "aggrExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "aggrExprProp2")));
	// COLLECTIONAL_EXPRESSION
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.collExprProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.simpleEntityProp.collExprProp")));
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp2")));
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.attrCollExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.attrCollExprProp2")));
    }

    protected List<Function> enhanceFunctionsWithCollectionalAttributes(final List<Function> functions) {
	final List<Function> newFunctions = new ArrayList<Function>(functions);
	newFunctions.add(Function.ALL);
	newFunctions.add(Function.ANY);
	return newFunctions;
    }

    @Test
    public void test_that_any_property_has_type_related_functions() {
	final String m = "Available functions are incorrect.";
	// Entity property
	assertEquals(m, Arrays.asList(Function.COUNT_DISTINCT), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection")));

	List<Function> functions = Arrays.asList(Function.SELF, Function.COUNT_DISTINCT);
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "simpleEntityProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.simpleEntityProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.simpleEntityProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.simpleEntityProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.simpleEntityProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.simpleEntityProp")));

	// String property
	functions = Arrays.asList(Function.SELF, Function.MIN, Function.MAX);
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "stringProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.stringProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.stringProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.stringProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.stringProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.stringProp")));

	functions = Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX);
	// Money property
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "moneyProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.moneyProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.moneyProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.moneyProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.moneyProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.moneyProp")));
	// Integer property
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "integerProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.integerProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.integerProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.integerProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.integerProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp")));
	// BigDecimal property
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "bigDecimalProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.bigDecimalProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.bigDecimalProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.bigDecimalProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.bigDecimalProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.bigDecimalProp")));

	// Boolean property
	functions = Arrays.asList(Function.SELF, Function.COUNT_DISTINCT);
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "booleanProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.booleanProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.booleanProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.booleanProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.booleanProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.booleanProp")));

	// Date property
	functions = Arrays.asList(Function.SELF, Function.YEAR, Function.MONTH, Function.DAY, Function.MIN, Function.MAX);
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "dateProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.dateProp")));
	assertEquals(m, functions, new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.entityProp.dateProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "collection.dateProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.dateProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes(functions), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.slaveEntityProp.dateProp")));
    }

    @Test
    public void test_that_any_excluded_properties_available_functions_cause_IllegalArgument_exception() {
	allLevels(new IAction() {
	    public void action(final String name) {
		//available functions.
		//excluded manually stuff (available functions)
		try {
		    dtm().getRepresentation().availableFunctions(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");

	// other excluded stuff
	oneLevel(new IAction() {
	    public void action(final String name) {
		try {
		    dtm().getRepresentation().availableFunctions(MasterEntity.class, name);
		    fail("Excluded property should cause illegal argument exception.");
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "entityProp.collection.slaveEntityProp.moneyProp.amount", //
		"collection.entityPropWithoutKeyType.key", //
		"entityProp.collection.enumProp", //
		"entityProp.collection.entityWithCompositeKeyProp.keyPartProp", //
		"entityProp.collection.critOnlyAECollectionProp.integerProp"); //
    }
}