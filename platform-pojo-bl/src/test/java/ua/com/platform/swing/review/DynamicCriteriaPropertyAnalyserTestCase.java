package ua.com.platform.swing.review;

import junit.framework.TestCase;
import ua.com.fielden.platform.swing.review.DefaultDynamicCriteriaPropertyFilter;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPropertyAnalyser;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

public class DynamicCriteriaPropertyAnalyserTestCase extends TestCase {

    private final IPropertyFilter propertyFilter = new DefaultDynamicCriteriaPropertyFilter();

    public void testWhetherKeyIsVisible() {
	DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "", propertyFilter);
	assertTrue("The " + KeyEntity.class.getSimpleName() + " criteria item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + " fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(EntityWithEntityKey.class, "", propertyFilter);
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " result item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(EntityWithCompositeKey.class, "", propertyFilter);
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
    }

    public void testNestedTypeAvailability() {
	DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "anotherProperty.keyEntity", propertyFilter);
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".anotherProperty.keyEntity criteria item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".anotherProperty.keyEntity fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "anotherProperty", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".anotherProperty criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".anotherProperty fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "compositeKeyEntity.keyEntityMember", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".compositeKeyEntity.keyEntityMember criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".compositeKeyEntity.keyEntityMember fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
    }

    public void testAnnotatedPropertiesAvailability() {
	DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "entityString", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".entityString criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".entityString fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "invisibleEntityString", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".invisibleEntityString criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".invisibleEntityString fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "listPropertyString", propertyFilter);
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".listPropertyString criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".listPropertyString fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "enumPropertyString", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".enumPropertyString criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".enumPropertyString fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "critEntityProperty.keyProperty", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".critEntityProperty.keyProperty criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".critEntityProperty.keyProperty fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "critEntityProperty", propertyFilter);
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".critEntityProperty criteria item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".critEntityProperty fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "resultEntityProperty", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".resultEntityProperty criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".resultEntityProperty fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser = new DynamicCriteriaPropertyAnalyser(KeyEntity.class, "resultEntityProperty.keyProperty", propertyFilter);
	assertFalse("The " + KeyEntity.class.getSimpleName() + ".keyProperty criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + ".keyProperty fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
    }

    public void testPropertyAnalyserMutability() {
	final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser();
	propertyAnalyser.setAnalyseProperty(KeyEntity.class, "");
	assertTrue("The " + KeyEntity.class.getSimpleName() + " criteria item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + " fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser.setAnalyseProperty(EntityWithEntityKey.class, "");
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " result item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser.setAnalyseProperty(EntityWithCompositeKey.class, "");
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
    }

    public void testPropertyAnalyserFilterMutability() {
	final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser();
	propertyAnalyser.setPropertyFilter(propertyFilter);
	propertyAnalyser.setAnalyseProperty(KeyEntity.class, "");
	assertTrue("The " + KeyEntity.class.getSimpleName() + " criteria item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertTrue("The " + KeyEntity.class.getSimpleName() + " fetch item should be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser.setAnalyseProperty(EntityWithEntityKey.class, "");
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithEntityKey.class.getSimpleName() + " result item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
	propertyAnalyser.setAnalyseProperty(EntityWithCompositeKey.class, "");
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " criteria item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isCriteriaPropertyAvailable());
	assertFalse("The " + EntityWithCompositeKey.class.getSimpleName() + " fetch item shouldn't be available", propertyAnalyser.isPropertyVisible()
		&& propertyAnalyser.isFetchPropertyAvailable());
    }
}
