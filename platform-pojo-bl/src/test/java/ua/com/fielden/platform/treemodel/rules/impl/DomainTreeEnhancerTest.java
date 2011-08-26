package ua.com.fielden.platform.treemodel.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EnhancingEvenSlaverEntity;
import ua.com.fielden.platform.domain.tree.EnhancingMasterEntity;
import ua.com.fielden.platform.domain.tree.EnhancingSlaveEntity;
import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeEnhancer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeEnhancer.IncorrectPlaceException;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for {@link DomainTreeEnhancer}.
 *
 * @author TG Team
 *
 */
public class DomainTreeEnhancerTest extends AbstractDomainTreeTest {
    private IDomainTreeEnhancer dm;

    @Override
    protected IDomainTreeManagerAndEnhancer createManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	return null; // not needed
    }

    /**
     * Entity for "domain enhancer" testing (derived from {@link MasterEntity}).
     *
     * @author TG Team
     *
     */
    @KeyType(String.class)
    public class EnhancingMasterEntity$$TgEntity172 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingMasterEntity$$TgEntity172() {
        }

        @IsProperty
        private EnhancingMasterEntity$$TgEntity175 masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity$$TgEntity173 slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity$$TgEntity177 evenSlaverEntityProp;

        public EnhancingMasterEntity$$TgEntity175 getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity$$TgEntity175 masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity$$TgEntity173 getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity$$TgEntity173 slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity$$TgEntity177 getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity$$TgEntity177 evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }

        @IsProperty
        @Title(value = "Title", desc = "Desc")
        @Calculated(expression = "4 * [integerProp]", origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldQuadruple;

        public Integer getOldQuadruple() {
            return oldQuadruple;
        }
        @Observable
        public void setOldQuadruple(final Integer oldQuadruple) {
            this.oldQuadruple = oldQuadruple;
        }
    }

    /**
     * Entity for "domain tree enhancing" testing.
     *
     * @author TG Team
     *
     */
    @KeyType(String.class)
    @KeyTitle(value = "Key title", desc = "Key desc")
    @DescTitle(value = "Desc title", desc = "Desc desc")
    public class EnhancingEvenSlaverEntity$$TgEntity177 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingEvenSlaverEntity$$TgEntity177() {
        }

        @IsProperty
        private EnhancingMasterEntity masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity$$TgEntity176 slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity evenSlaverEntityProp;

        public EnhancingMasterEntity getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity$$TgEntity176 getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity$$TgEntity176 slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }
    }

    /**
     * Entity for "domain enhancer" testing (derived from {@link MasterEntity}).
     *
     * @author TG Team
     *
     */
    @KeyType(String.class)
    public class EnhancingMasterEntity$$TgEntity174 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingMasterEntity$$TgEntity174() {
        }

        @IsProperty
        private EnhancingMasterEntity masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity evenSlaverEntityProp;

        public EnhancingMasterEntity getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }

        @IsProperty
        @Title(value = "Title", desc = "Desc")
        @Calculated(expression = "1 * [integerProp]", origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldSingle;

        public Integer getOldSingle() {
            return oldSingle;
        }
        @Observable
        public void setOldSingle(final Integer oldSingle) {
            this.oldSingle = oldSingle;
        }
    }

    /**
     * Entity for "domain tree enhancing" testing.
     *
     * @author TG Team
     *
     */
    @KeyTitle(value = "Key title", desc = "Key desc")
    @KeyType(String.class)
    public class EnhancingMasterEntity$$TgEntity175 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingMasterEntity$$TgEntity175() {
        }

        @IsProperty
        private EnhancingMasterEntity$$TgEntity174 masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity evenSlaverEntityProp;

        public EnhancingMasterEntity$$TgEntity174 getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity$$TgEntity174 masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }
    }

    /**
     * Entity for "domain tree enhancing" testing.
     *
     * @author TG Team
     *
     */
    @KeyType(String.class)
    @KeyTitle(value = "Key title", desc = "Key desc")
    @DescTitle(value = "Desc title", desc = "Desc desc")
    public class EnhancingSlaveEntity$$TgEntity173 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingSlaveEntity$$TgEntity173() {
        }

        @IsProperty
        private EnhancingMasterEntity masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity evenSlaverEntityProp;

        public EnhancingMasterEntity getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }

        @IsProperty
        @Title(value = "Title", desc = "Desc")
        @Calculated(expression = "3 * [integerProp]", origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldTriple;

        public Integer getOldTriple() {
            return oldTriple;
        }
        @Observable
        public void setOldTriple(final Integer oldTriple) {
            this.oldTriple = oldTriple;
        }
    }

    /**
     * Entity for "domain tree enhancing" testing.
     *
     * @author TG Team
     *
     */
    @KeyType(String.class)
    @KeyTitle(value = "Key title", desc = "Key desc")
    @DescTitle(value = "Desc title", desc = "Desc desc")
    public class EnhancingSlaveEntity$$TgEntity176 extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        protected EnhancingSlaveEntity$$TgEntity176() {
        }

        @IsProperty
        private EnhancingMasterEntity masterEntityProp;
        @IsProperty
        private EnhancingSlaveEntity slaveEntityProp;
        @IsProperty
        private EnhancingEvenSlaverEntity evenSlaverEntityProp;

        public EnhancingMasterEntity getMasterEntityProp() {
    	return masterEntityProp;
        }
        @Observable
        public void setMasterEntityProp(final EnhancingMasterEntity masterEntityProp) {
    	this.masterEntityProp = masterEntityProp;
        }

        public EnhancingSlaveEntity getSlaveEntityProp() {
    	return slaveEntityProp;
        }
        @Observable
        public void setSlaveEntityProp(final EnhancingSlaveEntity slaveEntityProp) {
    	this.slaveEntityProp = slaveEntityProp;
        }

        public EnhancingEvenSlaverEntity getEvenSlaverEntityProp() {
            return evenSlaverEntityProp;
        }
        @Observable
        public void setEvenSlaverEntityProp(final EnhancingEvenSlaverEntity evenSlaverEntityProp) {
            this.evenSlaverEntityProp = evenSlaverEntityProp;
        }

        @IsProperty
        @Title(value = "Title", desc = "Desc")
        @Calculated(expression = "2 * [integerProp]", origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldDouble;

        public Integer getOldDouble() {
            return oldDouble;
        }
        @Observable
        public void setOldDouble(final Integer oldDouble) {
            this.oldDouble = oldDouble;
        }
    }

    @Before
    public void setUp() throws ClassNotFoundException {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
	rootTypes.add(EnhancingMasterEntity.class);

	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	final List<String> names = new ArrayList<String>();
	names.add(EnhancingMasterEntity$$TgEntity172.class.getName());
//	names.add(EnhancingEvenSlaverEntity$$TgEntity177.class.getName());
//	names.add(EnhancingMasterEntity$$TgEntity174.class.getName());
//	names.add(EnhancingMasterEntity$$TgEntity175.class.getName());
//	names.add(EnhancingSlaveEntity$$TgEntity173.class.getName());
//	names.add(EnhancingSlaveEntity$$TgEntity176.class.getName());
	// TODO add other enhanced sub-types?

	final Map<Class<?>, List<ByteArray>> originalAndEnhancedRootTypes = new HashMap<Class<?>, List<ByteArray>>();
	originalAndEnhancedRootTypes.put(EnhancingMasterEntity.class, new ArrayList<ByteArray>());
	for (final String name : names) {
	    classLoader.startModification(name).endModification();
	    originalAndEnhancedRootTypes.get(EnhancingMasterEntity.class).add(new ByteArray(classLoader.getCachedByteArray(name)));
	}
	dm = new DomainTreeEnhancer(rootTypes, originalAndEnhancedRootTypes);
    }

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

		"masterEntityProp.masterEntityProp." + atomicName,
		"masterEntityProp.masterEntityProp.masterEntityProp." + atomicName,
		"masterEntityProp.masterEntityProp.slaveEntityProp." + atomicName,
		"masterEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

		"masterEntityProp.slaveEntityProp." + atomicName,
		"masterEntityProp.slaveEntityProp.masterEntityProp." + atomicName,
		"masterEntityProp.slaveEntityProp.slaveEntityProp." + atomicName,
		"masterEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

		"masterEntityProp.evenSlaverEntityProp." + atomicName,
		"masterEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName,
		"masterEntityProp.evenSlaverEntityProp.slaveEntityProp." + atomicName,
		"masterEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName,
		////////////////////////////////////////////////////////////////////////////
		"slaveEntityProp." + atomicName,

		"slaveEntityProp.masterEntityProp." + atomicName,
		"slaveEntityProp.masterEntityProp.masterEntityProp." + atomicName,
		"slaveEntityProp.masterEntityProp.slaveEntityProp." + atomicName,
		"slaveEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

		"slaveEntityProp.slaveEntityProp." + atomicName,
		"slaveEntityProp.slaveEntityProp.masterEntityProp." + atomicName,
		"slaveEntityProp.slaveEntityProp.slaveEntityProp." + atomicName,
		"slaveEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

		"slaveEntityProp.evenSlaverEntityProp." + atomicName,
		"slaveEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName,
		"slaveEntityProp.evenSlaverEntityProp.slaveEntityProp." + atomicName,
		"slaveEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName,
		////////////////////////////////////////////////////////////////////////////
		"evenSlaverEntityProp." + atomicName,

		"evenSlaverEntityProp.masterEntityProp." + atomicName,
		"evenSlaverEntityProp.masterEntityProp.masterEntityProp." + atomicName,
		"evenSlaverEntityProp.masterEntityProp.slaveEntityProp." + atomicName,
		"evenSlaverEntityProp.masterEntityProp.evenSlaverEntityProp." + atomicName,

		"evenSlaverEntityProp.slaveEntityProp." + atomicName,
		"evenSlaverEntityProp.slaveEntityProp.masterEntityProp." + atomicName,
		"evenSlaverEntityProp.slaveEntityProp.slaveEntityProp." + atomicName,
		"evenSlaverEntityProp.slaveEntityProp.evenSlaverEntityProp." + atomicName,

		"evenSlaverEntityProp.evenSlaverEntityProp." + atomicName,
		"evenSlaverEntityProp.evenSlaverEntityProp.masterEntityProp." + atomicName,
		"evenSlaverEntityProp.evenSlaverEntityProp.slaveEntityProp." + atomicName,
		"evenSlaverEntityProp.evenSlaverEntityProp.evenSlaverEntityProp." + atomicName
	);
	for (final String name : names) {
	    if (!name.equals(prop)) {
		fieldDoesNotExist(type, name);
	    }
	}
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
	final Calculated calcAnno = field.getAnnotation(Calculated.class);
	assertNotNull("The annotation Calculated should exist.", calcAnno);
	assertEquals("Incorrect expression.", expr, calcAnno.expression());
	assertEquals("Incorrect origination property.", originationProp, calcAnno.origination());
	assertEquals("Incorrect category.", category, calcAnno.category());
	// check Title annotation
	final Title titleAnno = field.getAnnotation(Title.class);
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
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
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

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "smthProp", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Double.class, "anyProp", "Smth", "Smth"));
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setTitle("T").setDesc("D").setExpression("Exp").setResultType(Double.class);
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.discard();
	dm.apply();

	checkOriginalDomain(dm);
    }

    private static void checkFirstLevelEnhancements(final IDomainTreeEnhancer dm) {
	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * 1 * [integerProp]", "Title Bad", "Desc"));
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "single").setTitle("Title").setExpression("1 * [integerProp]");
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple").setDesc("New Desc").setExpression("4 * 1 * [integerProp]");
	dm.apply();

	// check the snapshot of domain
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * 1 * [integerProp]", "Title", "New Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");

	// modify domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "double");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "triple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quadruple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
    }

    private static void checkSecondLevelEnhancements(final IDomainTreeEnhancer dm) {
	// modify domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc"));
	dm.apply();

	// check the snapshot of domain
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
    }

    private static void checkThirdLevelEnhancements(final IDomainTreeEnhancer dm) {
	// modify domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setExpression("1 * 2 * [integerProp]").setResultType(BigInteger.class).setTitle("New Title").setDesc("New Desc");
	dm.apply();

	// check the snapshot of domain
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", BigInteger.class, "1 * 2 * [integerProp]", "New Title", "New Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "quintuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");

	// modify domain
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setExpression("2 * [integerProp]").setResultType(Integer.class).setTitle("Title").setDesc("Desc");
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.quintuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "5 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.masterEntityProp.septuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "7 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.masterEntityProp.octuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "8 * [integerProp]", "Title", "Desc"));
	dm.apply();

	// check the snapshot of domain
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "single", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.double", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.triple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.quintuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "5 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.masterEntityProp.septuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "7 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.masterEntityProp.octuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "8 * [integerProp]", "Title", "Desc");
    }

    @Test
    public void test_that_Discard_operation_is_actually_working() {
	checkDiscardOperation(dm);
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	// check the snapshot of domain
//	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "single");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "double");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "triple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "quadruple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "quintuple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "octuple");

	// checkDiscardOperation(copy);
    }

    @Test
    public void test_first_level_enhancements() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);
    }

    @Test
    public void test_first_level_enhancements_FOR_THE_COPY_OF_MANAGER() {
	checkDiscardOperation(dm);
	// this is very important test due to JVM's lazy class loading!
	checkFirstLevelEnhancements(EntityUtils.deepCopy(dm, getSerialiser()));
    }

    @Test
    public void test_second_level_enhancements() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);
	checkSecondLevelEnhancements(dm);
    }

    @Test
    public void test_second_level_enhancements_FOR_THE_COPY_OF_MANAGER() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);
	// this is very important test due to JVM's lazy class loading!
	checkSecondLevelEnhancements(EntityUtils.deepCopy(dm, getSerialiser()));
    }

    @Test
    public void test_third_level_enhancements() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);
	checkSecondLevelEnhancements(dm);
	checkThirdLevelEnhancements(dm);
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER() {
	checkDiscardOperation(dm);

	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	checkFirstLevelEnhancements(copy);
	checkSecondLevelEnhancements(copy);
	// this is very important test due to JVM's lazy class loading!
	checkThirdLevelEnhancements(copy);
    }

    @Test
    public void test_that_self_type_properties_will_not_be_adapted() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
	rootTypes.add(EnhancingMasterEntity.class);
	final IDomainTreeEnhancer dm = new DomainTreeEnhancer(rootTypes);

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
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc"));
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
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");
    }

    private void failDomainModification(final IDomainTreeEnhancer dm, final Class<?> rootType, final String calcProperty) {
	// try to modify domain
	try {
	    dm.addCalculatedProperty(new CalculatedProperty(rootType, calcProperty, ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "789 * [integerProp]", "Title", "Desc"));
	    fail("The place for calculated property is incorrect. The action should be failed. Path = [" + calcProperty + "]");
	} catch (final IncorrectPlaceException e) {
	}
	try {
	    dm.removeCalculatedProperty(rootType, calcProperty);
	    fail("The place for calculated property is incorrect. The action should be failed. Path = [" + calcProperty + "]");
	} catch (final IncorrectPlaceException e) {
	}
	try {
	    dm.getCalculatedProperty(rootType, calcProperty);
	    fail("The place for calculated property is incorrect. The action should be failed. Path = [" + calcProperty + "]");
	} catch (final IncorrectPlaceException e) {
	}
    }

    @Test
    public void test_that_calc_properties_adding_removing_and_obtaining_is_validated_upon_place_in_hierarchy() {
	failDomainModification(dm, EnhancingSlaveEntity.class, "anyProp.calcProp");
	failDomainModification(dm, EnhancingMasterEntity.class, "masterEntityProp1.calcProp");
	failDomainModification(dm, EnhancingMasterEntity.class, "masterEntityProp.evenSlaverEntityProp1.calcProp");
	failDomainModification(dm, EnhancingMasterEntity.class, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp1.calcProp");
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
	rootTypes.add(EnhancingMasterEntity.class);
	final IDomainTreeEnhancer dm = new DomainTreeEnhancer(rootTypes);

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
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "sextuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "septuple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "octuple");

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp.single", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.double", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.triple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.quintuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "5 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp.septuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "7 * [integerProp]", "Title", "Desc"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp.octuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "8 * [integerProp]", "Title", "Desc"));

	dm.apply();

	// check the snapshot of domain
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldSingle");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldDouble");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldTriple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityCollProp.single", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "1 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.double", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "2 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.triple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "3 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.quadruple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "4 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.quintuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "5 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.evenSlaverEntityCollProp.sextuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "6 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityCollProp.slaveEntityProp.septuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "7 * [integerProp]", "Title", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp.octuple", ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "8 * [integerProp]", "Title", "Desc");
    }

    @Override
    public void test_that_serialisation_works() throws Exception {
    }

    @Override
    public void test_that_equality_and_copying_works() {
    }

    @Override
    public void test_that_domain_tree_enhancements_work_as_expected_for_original_and_copied_manager() {
    }
}
