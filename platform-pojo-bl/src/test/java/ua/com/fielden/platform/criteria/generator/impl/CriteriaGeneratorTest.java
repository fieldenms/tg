package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CriteriaGeneratorTest {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final ICriteriaGenerator cg = injector.getInstance(ICriteriaGenerator.class);

    @SuppressWarnings("serial")
    private final CentreDomainTreeManagerAndEnhancer cdtm = new CentreDomainTreeManagerAndEnhancer(null, new HashSet<Class<?>>(){{ add(TopLevelEntity.class); }});
    {
	cdtm.getFirstTick().check(TopLevelEntity.class, "integerProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "moneyProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "booleanProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "stringProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.dateProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp", true);
    }

    @SuppressWarnings("serial")
    private final List<String> propertyNames = new ArrayList<String>(){{
	add("TopLevelEntity_integerProp_from");
	add("TopLevelEntity_integerProp_to");
	add("TopLevelEntity_moneyProp_from");
	add("TopLevelEntity_moneyProp_to");
	add("TopLevelEntity_booleanProp_is");
	add("TopLevelEntity_booleanProp_not");
	add("TopLevelEntity_stringProp");
	add("TopLevelEntity_");
	add("TopLevelEntity_entityProp");
	add("TopLevelEntity_entityProp_entityProp_dateProp_from");
	add("TopLevelEntity_entityProp_entityProp_dateProp_to");
	add("TopLevelEntity_entityProp_entityProp_simpleEntityProp");
	add("TopLevelEntity_critSingleEntity");
	add("TopLevelEntity_critRangeEntity");
	add("TopLevelEntity_critISingleProperty");
	add("TopLevelEntity_critIRangeProperty_from");
	add("TopLevelEntity_critIRangeProperty_to");
	add("TopLevelEntity_critSSingleProperty");
	add("TopLevelEntity_critSRangeProperty");
	add("TopLevelEntity_critBSingleProperty");
	add("TopLevelEntity_critBRangeProperty_is");
	add("TopLevelEntity_critBRangeProperty_not");
    }};

    @SuppressWarnings("serial")
    private final List<Class<?>> propertyTypes = new ArrayList<Class<?>>(){{
	add(Integer.class);
	add(Integer.class);
	add(Money.class);
	add(Money.class);
	add(Boolean.class);
	add(Boolean.class);
	add(String.class);
	add(List.class);
	add(List.class);
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
    }};

    @SuppressWarnings({ "unchecked", "serial" })
    List<Map<Class<? extends Annotation>, Map<String, Object>>> annotationValues = new ArrayList<Map<Class<? extends Annotation>,Map<String,Object>>>(){{
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "integer property"), new Pair<String, Object>("desc", "integer property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "integerProp")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_integerProp_to")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "integer property"), new Pair<String, Object>("desc", "integer property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "integerProp")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_integerProp_from")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "money property"), new Pair<String, Object>("desc", "money property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "moneyProp")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_moneyProp_to")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "money property"), new Pair<String, Object>("desc", "money property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "moneyProp")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_moneyProp_from")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "boolean property"), new Pair<String, Object>("desc", "boolean property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "booleanProp")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_booleanProp_not")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "boolean property"), new Pair<String, Object>("desc", "boolean property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "booleanProp")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_booleanProp_is")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "string property"), new Pair<String, Object>("desc", "string property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "stringProp")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "key"), new Pair<String, Object>("desc", "key")));
	    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", TopLevelEntity.class)));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "entity property"), new Pair<String, Object>("desc", "entity property description")));
	    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", SecondLevelEntity.class)));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "entityProp")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "date property"), new Pair<String, Object>("desc", "date property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "entityProp.entityProp.dateProp")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_entityProp_entityProp_dateProp_to")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "date property"), new Pair<String, Object>("desc", "date property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "entityProp.entityProp.dateProp")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_entityProp_entityProp_dateProp_from")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "entity property"), new Pair<String, Object>("desc", "entity property description")));
	    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", LastLevelEntity.class)));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "entityProp.entityProp.simpleEntityProp")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single entity property"), new Pair<String, Object>("desc", "single entity property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critSingleEntity")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap(new Pair<String, Object>("value", String.class)));
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range entity property"), new Pair<String, Object>("desc", "range entity property description")));
	    put(EntityType.class, createAnnotationMap(new Pair<String, Object>("value", LastLevelEntity.class)));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critRangeEntity")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single integer property"), new Pair<String, Object>("desc", "single integer property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critISingleProperty")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range integer property"), new Pair<String, Object>("desc", "range integer property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critIRangeProperty")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_critIRangeProperty_to")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range integer property"), new Pair<String, Object>("desc", "range integer property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critIRangeProperty")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_critIRangeProperty_from")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single string property"), new Pair<String, Object>("desc", "single string property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critSSingleProperty")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range string property"), new Pair<String, Object>("desc", "range string property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critSRangeProperty")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "single boolean property"), new Pair<String, Object>("desc", "single boolean property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critBSingleProperty")));
	}});
	////reconfigure
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range boolean property"), new Pair<String, Object>("desc", "range boolean property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critBRangeProperty")));
	    put(FirstParam.class, createAnnotationMap(new Pair<String, Object>("secondParam", "TopLevelEntity_critBRangeProperty_not")));
	}});
	add(new HashMap<Class<? extends Annotation>, Map<String, Object>>(){{
	    put(IsProperty.class, createAnnotationMap());
	    put(Title.class, createAnnotationMap(new Pair<String, Object>("value", "range boolean property"), new Pair<String, Object>("desc", "range boolean property description")));
	    put(CriteriaProperty.class, createAnnotationMap(new Pair<String, Object>("propertyName", "critBRangeProperty")));
	    put(SecondParam.class, createAnnotationMap(new Pair<String, Object>("firstParam", "TopLevelEntity_critBRangeProperty_is")));
	}});
    }};

    @SuppressWarnings("serial")
    private Map<String, Object> createAnnotationMap(final Pair<String, Object>... values) {
	return new HashMap<String, Object>(){{
	    for(final Pair<String, Object> valuePair : values){
		put(valuePair.getKey(), valuePair.getValue());
	    }
	}};
    }

    @Test
    public void test_that_criteria_generation_works_correctly(){
	final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TopLevelEntity, IEntityDao2<TopLevelEntity>> criteriaEntity = cg.generateCentreQueryCriteria(TopLevelEntity.class, cdtm);
	assertNotNull("The centre domain tree manager can not be null", criteriaEntity.getCentreDomainTreeMangerAndEnhancer());
	final List<Field> criteriaProperties = CriteriaReflector.getCriteriaProperties(criteriaEntity.getClass());
	assertEquals("The number of criteria properties is incorrect", propertyNames.size(), criteriaProperties.size());
	for(int propertyIndex = 0; propertyIndex < propertyNames.size(); propertyIndex++){
	    assertProperty(propertyIndex, criteriaProperties);
	}
    }

    private void assertProperty(final int propertyIndex, final List<Field> criteriaProperties) {
	final String propertyName = propertyNames.get(propertyIndex);
	final Field propertyField = findFieldByName(criteriaProperties, propertyName);
	assertNotNull("The field with " + propertyName + " name doesn't exists", propertyField);
	assertEquals("The type of the " + propertyName + " property is incorrect", propertyTypes.get(propertyIndex), propertyField.getType());
	final Map<Class<? extends Annotation>, Map<String, Object>> annotations = annotationValues.get(propertyIndex);
	final Annotation[] fieldAnnotations = propertyField.getAnnotations();
	// Check whether all field annotations are correct.
	for(final Map.Entry<Class<? extends Annotation>, Map<String, Object>> annotationEntry : annotations.entrySet()){
	    // Find annotation for specified type.
	    final Class<? extends Annotation> annotationClass = annotationEntry.getKey();
	    final Annotation annotation = containAnnotation(fieldAnnotations, annotationClass);
	    assertNotNull("The " + propertyName + " is not annotated with " + annotationClass.getSimpleName(), annotation);

	    for(final Map.Entry<String, Object> valueEntry : annotationEntry.getValue().entrySet()){
		try {
		    final Method method = Reflector.getMethod(annotation.annotationType(), valueEntry.getKey());
		    assertEquals("The value for " + valueEntry.getKey() + " annotation parameter - " + annotation.annotationType().getSimpleName() + " annotation", valueEntry.getValue(), method.invoke(annotation));
		} catch (final NoSuchMethodException e) {
		    fail("The " + valueEntry.getKey() + " method for " + annotation.annotationType().getSimpleName() + " annotation doesn't exist.");
		} catch (final Exception e) {
		    fail("Fail to invoke " + valueEntry.getKey() + " method for " + annotation.annotationType().getSimpleName() + " annotation");
		}
	    }

	}
    }

    private Annotation containAnnotation(final Annotation[] fieldAnnotations, final Class<? extends Annotation> annotationClass) {
	for(final Annotation annotation : fieldAnnotations){
	    if(annotationClass.isAssignableFrom(annotation.annotationType())){
		return annotation;
	    }
	}
	return null;
    }

    private Field findFieldByName(final List<Field> criteriaProperties, final String propertyName) {
	for(final Field field : criteriaProperties){
	    if(field.getName().equals(propertyName)){
		return field;
	    }
	}
	return null;
    }
}
