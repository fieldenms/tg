package ua.com.fielden.platform.criteria.generator.impl;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.FirstParam;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

public class CriteriaGeneratorTest {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    {
	module.setInjector(injector);
    }
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    private final ICriteriaGenerator cg = injector.getInstance(ICriteriaGenerator.class);
    TgKryoForDomainTreesTestingPurposes serialiser = new TgKryoForDomainTreesTestingPurposes(entityFactory, new ClassProviderForTestingPurposes(TopLevelEntity.class, LastLevelEntity.class, SecondLevelEntity.class, ThirdLevelEntity.class));

    @SuppressWarnings("serial")
    private final CentreDomainTreeManagerAndEnhancer cdtm = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
	{
	    add(TopLevelEntity.class);
	}
    });
    {
	//Adding calculated properties to the centre domain tree manager and enhancer.
	cdtm.getEnhancer().addCalculatedProperty(TopLevelEntity.class, "", "3 + integerProp", "firstCalc", "firstCalc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	cdtm.getEnhancer().addCalculatedProperty(TopLevelEntity.class, "entityProp.entityProp", "3 + MONTH(dateProp)", "secondCalc", "secondCalc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	cdtm.getEnhancer().addCalculatedProperty(TopLevelEntity.class, "", "3 + moneyProp", "thirdCalc", "thirdCalc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	cdtm.getEnhancer().apply();

	//Configuring first tick check properties.
	cdtm.getFirstTick().check(TopLevelEntity.class, "critSingleEntity", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "critRangeEntity", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "critISingleProperty", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "critIRangeProperty", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "critSSingleProperty", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "critSRangeProperty", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "firstCalc", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "thirdCalc", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "integerProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "moneyProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "booleanProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "stringProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.dateProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.secondCalc", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp", true);
	cdtm.getFirstTick().setValue(TopLevelEntity.class, "integerProp", Integer.valueOf(30));
	cdtm.getFirstTick().setValue(TopLevelEntity.class, "moneyProp", new Money(BigDecimal.valueOf(30.0)));
    }

    private final Class<?> managedType = cdtm.getEnhancer().getManagedType(TopLevelEntity.class);
    private final Class<?> entityPropType = PropertyTypeDeterminator.determinePropertyType(managedType, "entityProp");

    @SuppressWarnings("serial")
    private final Map<String, Object> oldValues = new HashMap<String, Object>() {
	{
	    put("topLevelEntity_firstCalc_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "firstCalc"));
	    put("topLevelEntity_firstCalc_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "firstCalc"));
	    put("topLevelEntity_thirdCalc_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "thirdCalc"));
	    put("topLevelEntity_thirdCalc_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "thirdCalc"));
	    put("topLevelEntity_integerProp_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "integerProp"));
	    put("topLevelEntity_integerProp_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "integerProp"));
	    put("topLevelEntity_moneyProp_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "moneyProp"));
	    put("topLevelEntity_moneyProp_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "moneyProp"));
	    put("topLevelEntity_booleanProp_is", cdtm.getFirstTick().getValue(TopLevelEntity.class, "booleanProp"));
	    put("topLevelEntity_booleanProp_not", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "booleanProp"));
	    put("topLevelEntity_stringProp", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
	    put("topLevelEntity_", cdtm.getFirstTick().getValue(TopLevelEntity.class, ""));
	    put("topLevelEntity_entityProp", cdtm.getFirstTick().getValue(TopLevelEntity.class, "entityProp"));
	    put("topLevelEntity_entityProp_entityProp_secondCalc_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "entityProp.entityProp.secondCalc"));
	    put("topLevelEntity_entityProp_entityProp_secondCalc_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "entityProp.entityProp.secondCalc"));
	    put("topLevelEntity_entityProp_entityProp_dateProp_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "entityProp.entityProp.dateProp"));
	    put("topLevelEntity_entityProp_entityProp_dateProp_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "entityProp.entityProp.dateProp"));
	    put("topLevelEntity_entityProp_entityProp_simpleEntityProp", cdtm.getFirstTick().getValue(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp"));
	    put("topLevelEntity_critSingleEntity", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critSingleEntity"));
	    put("topLevelEntity_critRangeEntity", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critRangeEntity"));
	    put("topLevelEntity_critISingleProperty", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critISingleProperty"));
	    put("topLevelEntity_critIRangeProperty_from", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critIRangeProperty"));
	    put("topLevelEntity_critIRangeProperty_to", cdtm.getFirstTick().getValue2(TopLevelEntity.class, "critIRangeProperty"));
	    put("topLevelEntity_critSSingleProperty", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critSSingleProperty"));
	    put("topLevelEntity_critSRangeProperty", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critSRangeProperty"));
	}
    };

    @SuppressWarnings("serial")
    private final Map<String, Object> newValues = new HashMap<String, Object>() {
	{
	    put("topLevelEntity_firstCalc_from", Integer.valueOf(10));
	    put("topLevelEntity_firstCalc_to", Integer.valueOf(20));
	    put("topLevelEntity_thirdCalc_from", new Money(BigDecimal.valueOf(10.0)));
	    put("topLevelEntity_thirdCalc_to", new Money(BigDecimal.valueOf(20.0)));
	    put("topLevelEntity_integerProp_from", Integer.valueOf(20));
	    put("topLevelEntity_integerProp_to", Integer.valueOf(40));
	    put("topLevelEntity_moneyProp_from", new Money(BigDecimal.valueOf(20.0)));
	    put("topLevelEntity_moneyProp_to", new Money(BigDecimal.valueOf(40.0)));
	    put("topLevelEntity_booleanProp_is", Boolean.TRUE);
	    put("topLevelEntity_booleanProp_not", Boolean.FALSE);
	    put("topLevelEntity_stringProp", "string value, another string value");
	    put("topLevelEntity_", new ArrayList<String>() {
		{
		    add("A");
		    add("B");
		}
	    });
	    put("topLevelEntity_entityProp", new ArrayList<String>() {
		{
		    add("A");
		    add("B");
		}
	    });
	    put("topLevelEntity_entityProp_entityProp_dateProp_from", new Date(1L));
	    put("topLevelEntity_entityProp_entityProp_dateProp_to", new Date(2L));
	    put("topLevelEntity_entityProp_entityProp_secondCalc_from", Integer.valueOf(201201));
	    put("topLevelEntity_entityProp_entityProp_secondCalc_to", Integer.valueOf(201205));
	    put("topLevelEntity_entityProp_entityProp_simpleEntityProp", new ArrayList<String>() {
		{
		    add("A");
		    add("B");
		}
	    });
	    put("topLevelEntity_critSingleEntity", entityFactory.newByKey(LastLevelEntity.class, "EntityKey"));
	    put("topLevelEntity_critRangeEntity", new ArrayList<String>() {
		{
		    add("A");
		    add("B");
		}
	    });
	    put("topLevelEntity_critISingleProperty", Integer.valueOf(20));
	    put("topLevelEntity_critIRangeProperty_from", Integer.valueOf(10));
	    put("topLevelEntity_critIRangeProperty_to", Integer.valueOf(30));
	    put("topLevelEntity_critSSingleProperty", "string value");
	    put("topLevelEntity_critSRangeProperty", "string value, string value 1");
	}
    };

    @SuppressWarnings("serial")
    private final List<String> propertyNames = new ArrayList<String>() {
	{
	    add("topLevelEntity_firstCalc_from");
	    add("topLevelEntity_firstCalc_to");
	    add("topLevelEntity_thirdCalc_from");
	    add("topLevelEntity_thirdCalc_to");
	    add("topLevelEntity_integerProp_from");
	    add("topLevelEntity_integerProp_to");
	    add("topLevelEntity_moneyProp_from");
	    add("topLevelEntity_moneyProp_to");
	    add("topLevelEntity_booleanProp_is");
	    add("topLevelEntity_booleanProp_not");
	    add("topLevelEntity_stringProp");
	    add("topLevelEntity_");
	    add("topLevelEntity_entityProp");
	    add("topLevelEntity_entityProp_entityProp_secondCalc_from");
	    add("topLevelEntity_entityProp_entityProp_secondCalc_to");
	    add("topLevelEntity_entityProp_entityProp_dateProp_from");
	    add("topLevelEntity_entityProp_entityProp_dateProp_to");
	    add("topLevelEntity_entityProp_entityProp_simpleEntityProp");
	    add("topLevelEntity_critSingleEntity");
	    add("topLevelEntity_critRangeEntity");
	    add("topLevelEntity_critISingleProperty");
	    add("topLevelEntity_critIRangeProperty_from");
	    add("topLevelEntity_critIRangeProperty_to");
	    add("topLevelEntity_critSSingleProperty");
	    add("topLevelEntity_critSRangeProperty");
	}
    };

    @SuppressWarnings("serial")
    private final List<Class<?>> propertyTypes = new ArrayList<Class<?>>() {
	{
	    add(Integer.class);
	    add(Integer.class);
	    add(Money.class);
	    add(Money.class);
	    add(Integer.class);
	    add(Integer.class);
	    add(Money.class);
	    add(Money.class);
	    add(Boolean.class);
	    add(Boolean.class);
	    add(String.class);
	    add(List.class);
	    add(List.class);
	    add(Integer.class);
	    add(Integer.class);
	    add(Date.class);
	    add(Date.class);
	    add(List.class);
	    add(LastLevelEntity.class);
	    add(List.class);
	    add(Integer.class);
	    add(Integer.class);
	    add(Integer.class);
	    add(String.class);
	    add(String.class);
	    add(Boolean.class);
	    add(Boolean.class);
	    add(Boolean.class);
	}
    };

    @SuppressWarnings({ "unchecked", "serial" })
    List<Map<Class<? extends Annotation>, Map<String, Object>>> annotationValues = new ArrayList<Map<Class<? extends Annotation>, Map<String, Object>>>() {
	{
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "firstCalc"), new Pair<String, Object>("desc", "firstCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "firstCalc")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_firstCalc_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "firstCalc"), new Pair<String, Object>("desc", "firstCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "firstCalc")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_firstCalc_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "thirdCalc"), new Pair<String, Object>("desc", "thirdCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "thirdCalc")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_thirdCalc_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "thirdCalc"), new Pair<String, Object>("desc", "thirdCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "thirdCalc")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_thirdCalc_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "integer property"), new Pair<String, Object>("desc", "integer property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "integerProp")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_integerProp_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "integer property"), new Pair<String, Object>("desc", "integer property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "integerProp")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_integerProp_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "money property"), new Pair<String, Object>("desc", "money property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "moneyProp")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_moneyProp_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "money property"), new Pair<String, Object>("desc", "money property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "moneyProp")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_moneyProp_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "boolean property"), new Pair<String, Object>("desc", "boolean property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "booleanProp")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_booleanProp_not")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "boolean property"), new Pair<String, Object>("desc", "boolean property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "booleanProp")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_booleanProp_is")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "string property"), new Pair<String, Object>("desc", "string property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "stringProp")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "key"), new Pair<String, Object>("desc", "key")));
		    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", managedType)));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "entity property"), new Pair<String, Object>("desc", "entity property description")));
		    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", entityPropType)));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "secondCalc"), new Pair<String, Object>("desc", "secondCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp.entityProp.secondCalc")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_entityProp_entityProp_secondCalc_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "secondCalc"), new Pair<String, Object>("desc", "secondCalc")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp.entityProp.secondCalc")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_entityProp_entityProp_secondCalc_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });

	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "date property"), new Pair<String, Object>("desc", "date property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp.entityProp.dateProp")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_entityProp_entityProp_dateProp_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "date property"), new Pair<String, Object>("desc", "date property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp.entityProp.dateProp")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_entityProp_entityProp_dateProp_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "entity property"), new Pair<String, Object>("desc", "entity property description")));
		    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", LastLevelEntity.class)));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "entityProp.entityProp.simpleEntityProp")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single entity property"), new Pair<String, Object>("desc", "single entity property description")));
		    put(Required.class, createAnnotationMap());
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critSingleEntity")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range entity property"), new Pair<String, Object>("desc", "range entity property description")));
		    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", LastLevelEntity.class)));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critRangeEntity")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single integer property"), new Pair<String, Object>("desc", "single integer property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critISingleProperty")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range integer property"), new Pair<String, Object>("desc", "range integer property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critIRangeProperty")));
		    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "topLevelEntity_critIRangeProperty_to")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range integer property"), new Pair<String, Object>("desc", "range integer property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critIRangeProperty")));
		    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "topLevelEntity_critIRangeProperty_from")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single string property"), new Pair<String, Object>("desc", "single string property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critSSingleProperty")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	    add(new HashMap<Class<? extends Annotation>, Map<String, Object>>() {
		{
		    put(IsProperty.class, createAnnotationMap());
		    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range string property"), new Pair<String, Object>("desc", "range string property description")));
		    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("rootType", managedType), new Pair<String, Object>("propertyName", "critSRangeProperty")));
		    put(AfterChange.class, createAnnotationMap(new Pair<String, Object>("value", SynchroniseCriteriaWithModelHandler.class)));
		}
	    });
	}
    };

    @SuppressWarnings({ "serial", "unchecked" })
    private Map<String, Object> createAnnotationMap(final Pair<String, Object>... values) {
	return new HashMap<String, Object>() {
	    {
		for (final Pair<String, Object> valuePair : values) {
		    put(valuePair.getKey(), valuePair.getValue());
		}
	    }
	};
    }

    @Test
    public void test_that_criteria_generation_works_correctly() {
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity = cg.generateCentreQueryCriteria(TopLevelEntity.class, cdtm);
	assertNotNull("The centre domain tree manager can not be null", criteriaEntity.getCentreDomainTreeMangerAndEnhancer());
	final List<Field> criteriaProperties = CriteriaReflector.getCriteriaProperties(criteriaEntity.getClass());
	assertEquals("The number of criteria properties is incorrect", propertyNames.size(), criteriaProperties.size());
	assertOldPropertyValues(criteriaEntity, criteriaProperties);
	for (int propertyIndex = 0; propertyIndex < propertyNames.size(); propertyIndex++) {
	    assertPropertyConfig(propertyIndex, criteriaProperties);
	}
	for (final Map.Entry<String, Object> valueEntry : newValues.entrySet()) {
	    criteriaEntity.set(valueEntry.getKey(), valueEntry.getValue());
	}
	assertNewPropertyValues(criteriaEntity, criteriaProperties);
    }

    @Test
    public void test_that_crit_only_single_property_has_before_change_handler(){
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity = cg.generateCentreQueryCriteria(TopLevelEntity.class, cdtm);
	final Field critField = Finder.findFieldByName(criteriaEntity.getType(), "topLevelEntity_critSingleEntity");
	assertNotNull(critField);
	final BeforeChange beforeChange = critField.getAnnotation(BeforeChange.class);
	assertNotNull(beforeChange);
	final Handler[] handlers = beforeChange.value();
	assertNotNull(handlers);
	assertEquals("The number of handlers is incorrect", 1, handlers.length);
	final Handler handler = handlers[0];
	assertNotNull(handler);
	final ClassParam[] classParams = handler.non_ordinary();
	assertNotNull(classParams);
	assertEquals("The number of class params is incorrect", 1, classParams.length);
	final ClassParam classParam = classParams[0];
	assertEquals("The name of class param property is incorrect", "controller", classParam.name());
	assertEquals("The value of class param annottion is incorrect", ILastLevelEntity.class, classParam.value());
    }

    @Test
    public void test_that_crit_only_single_validates_entity_exists(){
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity = cg.generateCentreQueryCriteria(TopLevelEntity.class, cdtm);
	criteriaEntity.set("topLevelEntity_critSingleEntity", entityFactory.newByKey(LastLevelEntity.class, "invalid key"));
	final Result res = criteriaEntity.isValid();
	assertFalse(res.isSuccessful());
	assertNull("The value for crit only single entity property is incorrect", cdtm.getFirstTick().getValue(TopLevelEntity.class, "critSingleEntity"));
	criteriaEntity.set("topLevelEntity_critSingleEntity", entityFactory.newByKey(LastLevelEntity.class, "EntityKey"));
	final Result newRes = criteriaEntity.isValid();
	assertTrue(newRes.isSuccessful());
	assertEquals("The value for crit only single entity property is incorrect", "EntityKey", ((LastLevelEntity)cdtm.getFirstTick().getValue(TopLevelEntity.class, "critSingleEntity")).getKey());
    }

    @Test
    public void test_that_setting_default_value_when_criterion_already_has_other_value_works() {
	cdtm.getRepresentation().getFirstTick().setValueByDefault(TopLevelEntity.class, "stringProp", "default");
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity = cg.generateCentreQueryCriteria(TopLevelEntity.class, cdtm);
	assertEquals("Value should have been set", "default", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
	criteriaEntity.set("topLevelEntity_stringProp", "value");
	assertEquals("Value should have been set", "value", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
	criteriaEntity.set("topLevelEntity_stringProp", "default");
	assertEquals("Value should have been set", "default", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
    }

    @Test
    public void test_that_setting_default_value_when_criterion_has_no_value_works() {
	assertEquals("Value should not be set", "", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
	cdtm.getRepresentation().getFirstTick().setValueByDefault(TopLevelEntity.class, "stringProp", "default");
	assertEquals("Value should have been set", "default", cdtm.getFirstTick().getValue(TopLevelEntity.class, "stringProp"));
    }

    private void assertNewPropertyValues(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity, final List<Field> criteriaProperties) {
	final IAddToCriteriaTickManager ftm = criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	for (final Field propertyField : criteriaProperties) {
	    final SecondParam secondParam = propertyField.getAnnotation(SecondParam.class);
	    final CriteriaProperty critProperty = propertyField.getAnnotation(CriteriaProperty.class);
	    final Class<TopLevelEntity> root = criteriaEntity.getEntityClass();
	    final Object value = secondParam == null ? ftm.getValue(root, critProperty.propertyName()) : ftm.getValue2(root, critProperty.propertyName());
	    assertEquals("The property with " + critProperty.propertyName() + " name has unsynchronised value", newValues.get(propertyField.getName()), value);
	}
    }

    private void assertOldPropertyValues(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity, final List<Field> criteriaProperties) {
	for (final Field critProperty : criteriaProperties) {
	    assertEquals("The property with " + critProperty.getName() + " name has incorrect value", oldValues.get(critProperty.getName()), criteriaEntity.get(critProperty.getName()));
	}
    }

    private void assertPropertyConfig(final int propertyIndex, final List<Field> criteriaProperties) {
	final String propertyName = propertyNames.get(propertyIndex);
	final Field propertyField = findFieldByName(criteriaProperties, propertyName);
	assertNotNull("The field with " + propertyName + " name doesn't exists", propertyField);
	assertEquals("The type of the " + propertyName + " property is incorrect", propertyTypes.get(propertyIndex), propertyField.getType());
	final Map<Class<? extends Annotation>, Map<String, Object>> annotations = annotationValues.get(propertyIndex);
	final Annotation[] fieldAnnotations = propertyField.getAnnotations();
	// Check whether all field annotations are correct.
	for (final Map.Entry<Class<? extends Annotation>, Map<String, Object>> annotationEntry : annotations.entrySet()) {
	    // Find annotation for specified type.
	    final Class<? extends Annotation> annotationClass = annotationEntry.getKey();
	    final Annotation annotation = containAnnotation(fieldAnnotations, annotationClass);
	    assertNotNull("The " + propertyName + " is not annotated with " + annotationClass.getSimpleName(), annotation);

	    for (final Map.Entry<String, Object> valueEntry : annotationEntry.getValue().entrySet()) {
		try {
		    final Method method = Reflector.getMethod(annotation.annotationType(), valueEntry.getKey());
		    assertEquals("The value for " + valueEntry.getKey() + " annotation parameter - " + annotation.annotationType().getSimpleName() + " annotation", valueEntry.getValue(), method.invoke(annotation));
		} catch (final NoSuchMethodException e) {
		    fail("The " + valueEntry.getKey() + " method for " + annotation.annotationType().getSimpleName() + " annotation doesn't exist.");
		} catch (final Exception e) {
		    e.printStackTrace();
		    fail("Fail to invoke " + valueEntry.getKey() + " method for " + annotation.annotationType().getSimpleName() + " annotation");
		}
	    }

	}
    }

    private Annotation containAnnotation(final Annotation[] fieldAnnotations, final Class<? extends Annotation> annotationClass) {
	for (final Annotation annotation : fieldAnnotations) {
	    if (annotationClass.isAssignableFrom(annotation.annotationType())) {
		return annotation;
	    }
	}
	return null;
    }

    private Field findFieldByName(final List<Field> criteriaProperties, final String propertyName) {
	for (final Field field : criteriaProperties) {
	    if (field.getName().equals(propertyName)) {
		return field;
	    }
	}
	return null;
    }
}
