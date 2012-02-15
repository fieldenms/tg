package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.testing.EnhancingEvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity;
import ua.com.fielden.platform.domaintree.testing.EnhancingSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
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
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for {@link DomainTreeEnhancer}.
 *
 * @author TG Team
 *
 */
public class DomainTreeEnhancerTest extends AbstractDomainTreeTest {
    private IDomainTreeEnhancer dm;

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
        private Integer integerProp = null;
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
        @Title(value = "Old quadruple", desc = "Desc")
        @Calculated(contextualExpression = "4 * integerProp", contextPath = "", attribute = CalculatedPropertyAttribute.NO_ATTR, origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldQuadruple;

        public Integer getOldQuadruple() {
            return oldQuadruple;
        }
        @Observable
        public void setOldQuadruple(final Integer oldQuadruple) {
            this.oldQuadruple = oldQuadruple;
        }

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
        private Integer integerProp = null;
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

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
        private Integer integerProp = null;
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
        @Title(value = "Old single", desc = "Desc")
        @Calculated(contextualExpression = "1 * integerProp", contextPath = "masterEntityProp.masterEntityProp", attribute = CalculatedPropertyAttribute.NO_ATTR, origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldSingle;

        public Integer getOldSingle() {
            return oldSingle;
        }
        @Observable
        public void setOldSingle(final Integer oldSingle) {
            this.oldSingle = oldSingle;
        }

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
        private Integer integerProp = null;
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

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
        private Integer integerProp = null;
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
        @Title(value = "Old triple", desc = "Desc")
        @Calculated(contextualExpression = "3 * integerProp", contextPath = "slaveEntityProp", attribute = CalculatedPropertyAttribute.NO_ATTR, origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldTriple;

        public Integer getOldTriple() {
            return oldTriple;
        }
        @Observable
        public void setOldTriple(final Integer oldTriple) {
            this.oldTriple = oldTriple;
        }

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
        private Integer integerProp = null;
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
        @Title(value = "Old double", desc = "Desc")
        @Calculated(contextualExpression = "2 * integerProp", contextPath = "evenSlaverEntityProp.slaveEntityProp", attribute = CalculatedPropertyAttribute.NO_ATTR, origination = "integerProp", category = CalculatedPropertyCategory.EXPRESSION)
        private Integer oldDouble;

        public Integer getOldDouble() {
            return oldDouble;
        }
        @Observable
        public void setOldDouble(final Integer oldDouble) {
            this.oldDouble = oldDouble;
        }

        public Integer getIntegerProp() {
            return integerProp;
        }
        @Observable
        public void setIntegerProp(final Integer integerProp) {
            this.integerProp = integerProp;
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
	assertEquals("Incorrect expression.", expr, calcAnno.contextualExpression());
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
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.masterEntityProp.oldSingle", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "Old single", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityProp.slaveEntityProp.oldDouble", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Old double", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "slaveEntityProp.oldTriple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "Old triple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple", ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Old quadruple", "Desc");
	checkDomainPreparedForEnhancements(dm);
    }

    private static void checkEmptyDomain(final IDomainTreeEnhancer dm) {
	// check the snapshot of domain
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldSingle");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldDouble");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldTriple");
	fieldDoesNotExistInAnyPlace(dm.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	checkDomainPreparedForEnhancements(dm);
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

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "", "6 * integerProp", "Smth prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble").setTitle("OlD DoUbLe").setDesc("dESC").setContextualExpression("22 * integerProp");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.discard();
	dm.apply();

	checkOriginalDomain(dm);
    }

    private static void checkFirstLevelEnhancements(final IDomainTreeEnhancer dm) {
	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "", "1 * 1 * integerProp", "Title Bad", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "titleBad").setTitle("Single").setContextualExpression("1 * integerProp");
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple").setDesc("New Desc").setContextualExpression("4 * 1 * integerProp");
	dm.apply();

	// check the snapshot of domain
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

	// modify domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
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
    }

    private static void checkSecondLevelEnhancements(final IDomainTreeEnhancer dm) {
	// modify domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp", "2 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "3 * integerProp", "Triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp", "4 * integerProp", "Quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.apply();

	// check the snapshot of domain
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

	// modify domain
	dm.getCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.newTitle").setContextualExpression("2 * integerProp").setTitle("Old double").setDesc("Desc");
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "5 * integerProp", "Quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.masterEntityProp", "7 * integerProp", "Septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.masterEntityProp", "8 * integerProp", "Octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.apply();

	// check the snapshot of domain
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
    }

    @Test
    public void test_Discard_operation() {
	checkDiscardOperation(dm);
    }

    @Test
    public void test_Discard_operation_FOR_THE_COPY_OF_MANAGER() {
	// this is very important test due to JVM's lazy class loading!
	checkDiscardOperation(EntityUtils.deepCopy(dm, getSerialiser()));
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
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_1() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);
	checkSecondLevelEnhancements(dm);
	// this is very important test due to JVM's lazy class loading!
	checkThirdLevelEnhancements(EntityUtils.deepCopy(dm, getSerialiser()));
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_2() {
	checkDiscardOperation(dm);
	checkFirstLevelEnhancements(dm);

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());
	checkSecondLevelEnhancements(copy);
	checkThirdLevelEnhancements(copy);
    }

    @Test
    public void test_third_level_enhancements_FOR_THE_COPY_OF_MANAGER_3() {
	checkDiscardOperation(dm);

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());
	checkFirstLevelEnhancements(copy);
	checkSecondLevelEnhancements(copy);
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
	checkDomainPreparedForEnhancements(dm);

	// modify domain

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
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
    }

    private void failAddition(final IDomainTreeEnhancer dm, final Class<?> rootType, final String contextPath, final String title) {
	try {
	    dm.addCalculatedProperty(new CalculatedProperty(rootType, contextPath, "5 * integerProp", title, "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	    fail("The calculated property key is incorrect. The action should be failed. Context path = [" + contextPath + "], title = " + title);
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
    }

    private void failGettingAndRemoving(final IDomainTreeEnhancer dm, final Class<?> rootType, final String pathAndName) {
	try {
	    dm.getCalculatedProperty(rootType, pathAndName);
	    fail("The calculated property key is incorrect. The action should be failed. pathAndName = [" + pathAndName + "]");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
	try {
	    dm.removeCalculatedProperty(rootType, pathAndName);
	    fail("The calculated property key is incorrect. The action should be failed. pathAndName = [" + pathAndName + "]");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
    }

    @Test
    public void test_that_calc_properties_adding_is_validated() {
	// empty name
	failAddition(dm, EnhancingMasterEntity.class, "", null);
	failAddition(dm, EnhancingMasterEntity.class, "", "");
	// non-existent domain root
	failAddition(dm, EnhancingSlaveEntity.class, "", "correctProp");
	// non-existent path
	failAddition(dm, EnhancingMasterEntity.class, "masterEntityProp1", "Calc prop");
	failAddition(dm, EnhancingMasterEntity.class, "masterEntityProp.evenSlaverEntityProp1", "Calc prop");
	failAddition(dm, EnhancingMasterEntity.class, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp1", "Calc prop");
	// existent calc properties
	failAddition(dm, EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "Old single");
	failAddition(dm, EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "Old double");
	failAddition(dm, EnhancingMasterEntity.class, "slaveEntityProp", "Old triple");
	failAddition(dm, EnhancingMasterEntity.class, "", "Old quadruple");
	// existent simple properties
	failAddition(dm, EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "Integer prop");
	failAddition(dm, EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp", "Integer prop");
	failAddition(dm, EnhancingMasterEntity.class, "slaveEntityProp", "Integer prop");
	failAddition(dm, EnhancingMasterEntity.class, "", "Integer prop");
    }

    @Test
    public void test_that_calc_properties_removing_and_obtaining_is_validated() {
	// empty name
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "");
	// non-existent domain root
	failGettingAndRemoving(dm, EnhancingSlaveEntity.class, "oldQuadRuple");
	// non-existent path
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "masterEntityProp1.calcProp");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "masterEntityProp.evenSlaverEntityProp1.calcProp");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "slaveEntityProp.evenSlaverEntityProp.masterEntityProp1.calcProp");
	// non-existent calc properties
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle1");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble1");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "slaveEntityProp.oldTriple1");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "oldQuadruple1");
	// existent simple properties (are not calculated!)
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.integerProp");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.integerProp");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "slaveEntityProp.integerProp");
	failGettingAndRemoving(dm, EnhancingMasterEntity.class, "integerProp");
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced() {
	// clear domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
	checkEmptyDomain(dm);

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "1 * integerProp", "Single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "2 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "3 * integerProp", "Triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "4 * integerProp", "Quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "5 * integerProp", "Quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "6 * integerProp", "Sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "7 * integerProp", "Septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "8 * integerProp", "Octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	copy.apply();

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
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced_by_ATTRIBUTED_COLLECTIONAL_EXPRESSIONs() {
	// clear domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
	checkEmptyDomain(dm);

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "1 * integerProp", "All of single", "Desc", CalculatedPropertyAttribute.ALL, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "2 * integerProp", "Any of double", "Desc", CalculatedPropertyAttribute.ANY, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "3 * integerProp", "All of triple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "4 * integerProp", "Any of quadruple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "5 * integerProp", "All of quintuple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "6 * integerProp", "Any of sextuple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp"));

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "7 * integerProp", "All of septuple", "Desc", CalculatedPropertyAttribute.ALL, "integerProp"));
	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "8 * integerProp", "Any of octuple", "Desc", CalculatedPropertyAttribute.ANY, "integerProp"));

	copy.apply();

	// check the snapshot of domain
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldSingle");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldDouble");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldTriple");
	fieldDoesNotExistInAnyPlace(copy.getManagedType(EnhancingMasterEntity.class), "oldQuadruple");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "allOfSingle", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "1 * integerProp", "All of single", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "anyOfDouble", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "2 * integerProp", "Any of double", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "allOfTriple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "3 * integerProp", "All of triple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.anyOfQuadruple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "4 * integerProp", "Any of quadruple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "allOfQuintuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "5 * integerProp", "All of quintuple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "evenSlaverEntityCollProp.anyOfSextuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "6 * integerProp", "Any of sextuple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "masterEntityProp.allOfSeptuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "7 * integerProp", "All of septuple", "Desc");
	calcFieldExistsInSinglePlaceAndItWORKS(copy.getManagedType(EnhancingMasterEntity.class), "slaveEntityCollProp.slaveEntityProp.anyOfOctuple", CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION, "integerProp", Integer.class, "8 * integerProp", "Any of octuple", "Desc");
    }

    @Test
    public void test_that_collectional_hierarchies_can_be_enhanced_by_AGGREGATED_COLLECTIONAL_EXPRESSIONs() {
	// clear domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
	checkEmptyDomain(dm);

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityCollProp", "SUM(1 * integerProp)", "Sum of single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp", "AVG(2 * integerProp)", "Avg of double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp", "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp", "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp", "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityCollProp.evenSlaverEntityCollProp", "SUM(6 * integerProp)", "Sum of sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityCollProp.slaveEntityProp", "SUM(7 * integerProp)", "Sum of septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityCollProp.slaveEntityProp.masterEntityCollProp", "SUM(8 * integerProp)", "Sum of octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

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
    }

    @Test
    public void test_that_first_second_and_third_levels_can_be_enhanced_to_form_AGGREGATED_EXPRESSIONs_at_root_level() {
	// clear domain
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.oldSingle");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.slaveEntityProp.oldDouble");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.oldTriple");
	dm.removeCalculatedProperty(EnhancingMasterEntity.class, "oldQuadruple");
	dm.apply();

	// check the snapshot of domain
	checkEmptyDomain(dm);

	// modify domain
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp", "SUM(1 * integerProp)", "Sum of single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp", "AVG(2 * integerProp)", "Avg of double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp", "MIN(3 * integerProp) + MAX(4 * integerProp)", "Min of triple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp", "MAX(4 * integerProp) * MIN(3 * integerProp)", "Max of quadruple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.slaveEntityProp", "SUM(5 * integerProp + masterEntityProp.slaveEntityProp.integerProp)", "Sum of quintuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	dm.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "evenSlaverEntityProp.evenSlaverEntityProp", "SUM(6 * integerProp)", "Sum of sextuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	// this is very important test due to JVM's lazy class loading!
	final IDomainTreeEnhancer copy = EntityUtils.deepCopy(dm, getSerialiser());

	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "masterEntityProp.masterEntityProp.slaveEntityProp", "SUM(7 * integerProp)", "Sum of septuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));
	copy.addCalculatedProperty(new CalculatedProperty(EnhancingMasterEntity.class, "slaveEntityProp.slaveEntityProp.masterEntityProp", "SUM(8 * integerProp)", "Sum of octuple", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp"));

	copy.apply();

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
    }
}
