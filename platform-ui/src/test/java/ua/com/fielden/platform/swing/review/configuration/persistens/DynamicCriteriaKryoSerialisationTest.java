package ua.com.fielden.platform.swing.review.configuration.persistens;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.reportquery.AggregationProperty;
import ua.com.fielden.platform.reportquery.DistributionDateProperty;
import ua.com.fielden.platform.reportquery.DistributionProperty;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.AnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.LifecycleAnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyColumnMappingsPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.types.Ordering;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class DynamicCriteriaKryoSerialisationTest {

    static {
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
    }

    private Injector injector = Guice.createInjector(new CommonTestEntityModuleWithPropertyFactory());
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final TgKryo kryo = new TgKryo(factory, new DynamicCriteriaSerialisationClassProvider());

    @Test
    public void test_dynamic_criteira_kryo_serialisation() {
	final DynamicCriteriaPersistentObjectUi centerConfiguration = new DynamicCriteriaPersistentObjectUi(createLocatorPersistentObject(), createTableHeaders(), createProperties(), createExcludeProperties(), createColumnPersistentObject(), createCriteriaMappings(), 2, true, createAnalysis(), createTotoals(), Boolean.FALSE);
	final byte[] serialisedData = kryo.serialise(centerConfiguration);
	try {
	    final DynamicCriteriaPersistentObjectUi persistedObject = kryo.deserialise(serialisedData, DynamicCriteriaPersistentObjectUi.class);
	    assertTrue("This should be true", centerConfiguration.isChanged(persistedObject));
	} catch (final Exception e) {
	    // FIXME Oleh, could you please confirm whether thrown exception is legal situation during deserialisation?
	    e.printStackTrace();
	}

    }

    private Map<String, PropertyAggregationFunction> createTotoals() {
	final Map<String, PropertyAggregationFunction> totals = new HashMap<String, PropertyAggregationFunction>();
	totals.put("", PropertyAggregationFunction.DISTINCT_COUNT);
	totals.put("money", PropertyAggregationFunction.SUM);
	totals.put("string", PropertyAggregationFunction.COUNT);
	totals.put("entity", PropertyAggregationFunction.DISTINCT_COUNT);
	totals.put("entity.string", PropertyAggregationFunction.COUNT);
	return totals;
    }

    private LocatorPersistentObject createLocatorPersistentObject() {
	final DynamicCriteriaPersistentObjectUi locatorConfiguration = new DynamicCriteriaPersistentObjectUi(null, createTableHeaders(), createProperties(), createExcludeProperties(), createColumnPersistentObject(), createCriteriaMappings(), 2, true, createAnalysis(), true, true, false, true);
	final LocatorPersistentObject locatorPersistentObject = new LocatorPersistentObject();
	locatorPersistentObject.setLocatorConfiguration("SerialisationEntity.key", locatorConfiguration);
	return locatorPersistentObject;
    }

    private Map<String, IAnalysisReportPersistentObject> createAnalysis() {
	final Map<String, IAnalysisReportPersistentObject> analysis = new HashMap<String, IAnalysisReportPersistentObject>();
	analysis.put("first", createSimpleAnalysis());
	analysis.put("second", createLifecycleAnalysis());
	return analysis;
    }

    private Map<String, PropertyPersistentObject> createCriteriaMappings() {
	final Map<String, PropertyPersistentObject> criteriaMappings = new HashMap<String, PropertyPersistentObject>();
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "key", "key1", 0, 0, null);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "money_from", new Double(1), 0, 1, true);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "money_to", new Double(2), 0, 1, false);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "date_from", new Date(), 0, 2, false);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "date_to", new Double(1), 0, 2, true);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "entity.key", "key2", 1, 0, null);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "entity.integer_from", new Integer(1), 1, 1, true);
	createPropertyPersistentObject(criteriaMappings, SerialisationEntity.class, "entity.integer_to", new Integer(2), 1, 1, false);
	return criteriaMappings;
    }

    private PropertyColumnMappingsPersistentObject createColumnPersistentObject() {
	final PropertyTableModelBuilder columnBuilder = new PropertyTableModelBuilder<SerialisationEntity>(SerialisationEntity.class);
	columnBuilder.addReadonly("money", 80)//
	.addReadonly("", 80)//
	.addReadonly("string", 80)//
	.addReadonly("entity.string", 80)//
	.addReadonly("entity", 80);
	final List<SortKey> sortKeys = new ArrayList<SortKey>();
	sortKeys.add(new SortKey(0, SortOrder.ASCENDING));
	sortKeys.add(new SortKey(1, SortOrder.DESCENDING));
	final boolean[] isSortable = new boolean[5];
	isSortable[0] = true;
	isSortable[1] = true;
	isSortable[2] = false;
	isSortable[3] = false;
	isSortable[4] = true;
	final PropertyColumnMappingsPersistentObject propertyColumnMappings = new PropertyColumnMappingsPersistentObject(columnBuilder.getPropertyColumnMappings(), sortKeys, isSortable);
	return propertyColumnMappings;
    }

    private List<String> createExcludeProperties() {
	final List<String> excludeProperties = new ArrayList<String>();
	return excludeProperties;
    }

    private List<String> createProperties() {
	final List<String> properties = new ArrayList<String>();
	properties.add("");
	properties.add("money");
	properties.add("date");
	properties.add("entity");
	properties.add("entity.integer");
	return properties;
    }

    private List<String> createTableHeaders() {
	final List<String> tableHeaders = new ArrayList<String>();
	tableHeaders.add("");
	tableHeaders.add("money");
	tableHeaders.add("string");
	tableHeaders.add("entity");
	tableHeaders.add("entity.string");
	return tableHeaders;
    }

    private PropertyPersistentObject createPropertyPersistentObject(final Map<String, PropertyPersistentObject> criteriaMappings, final Class<? extends AbstractEntity> clazz, final String propertyName, final Object value, final int posx, final int posy, final Boolean exclusive) {
	final String propertyNamePrefix = clazz.getSimpleName();
	final PropertyPersistentObject property = new PropertyPersistentObject(propertyName);
	property.setPropertyValue(value);
	property.setExclusive(exclusive);
	property.setPosition(new Pair<Integer, Integer>(new Integer(posx), new Integer(posy)));
	criteriaMappings.put(propertyNamePrefix + "." + propertyName, property);
	return property;
    }

    private LifecycleAnalysisPersistentObject createLifecycleAnalysis() {
	final DistributionProperty lifecycleProperty = new DistributionProperty("name", "desc", "actProp");
	lifecycleProperty.setTableAlias("alias");
	final DistributionProperty distributionProperty = new DistributionProperty("name1", "desc", "actPrope");
	distributionProperty.setTableAlias("alias");
	final Date from = new Date();
	final Date to = new Date();
	final AggregationProperty aggregationProperty = new AggregationProperty("name1", "desc1", "actProp", AnalysisPropertyAggregationFunction.SUM);
	aggregationProperty.setTableAlias("alias");
	final Ordering<ICategory, IDistributedProperty> lifecycleOrdering = new Ordering<ICategory, IDistributedProperty>(aggregationProperty, CategoryForTesting.FIRST_CATEGORY, SortOrder.ASCENDING);
	final List<ICategory> lifecycleCategories = new ArrayList<ICategory>();
	lifecycleCategories.add(CategoryForTesting.FIRST_CATEGORY);
	lifecycleCategories.add(CategoryForTesting.SECOND_CATEGORY);
	final LifecycleAnalysisPersistentObject lifecycleAnalysisPersistentObject = new LifecycleAnalysisPersistentObject(lifecycleProperty, distributionProperty, from, to, lifecycleOrdering, lifecycleCategories, Boolean.TRUE, true);
	return lifecycleAnalysisPersistentObject;
    }

    private AnalysisPersistentObject createSimpleAnalysis() {
	final DistributionProperty name1DProperty = new DistributionProperty("name1", "desc1", "actProperty1");
	name1DProperty.setTableAlias("alias");
	final DistributionProperty name2DProperty = new DistributionProperty("name2", "desc2", "actProperty2");
	name2DProperty.setTableAlias("alias");
	final DistributionProperty name3DProperty = new DistributionProperty("name3", "desc3", "actProperty3");
	name3DProperty.setTableAlias("alias");
	final DistributionProperty name4DProperty = new DistributionProperty("name4", "desc4", "actProperty4");
	name4DProperty.setTableAlias("alias");
	final DistributionProperty name5DProperty = new DistributionDateProperty("name5", "desc5", "actProperty5", HqlDateFunctions.MONTH);
	name5DProperty.setTableAlias("alias");
	final List<IDistributedProperty> selectedDistributionProperty = new ArrayList<IDistributedProperty>();
	final List<IDistributedProperty> availableDistributionProperties = new ArrayList<IDistributedProperty>();
	availableDistributionProperties.add(name1DProperty);
	availableDistributionProperties.add(name2DProperty);
	availableDistributionProperties.add(name3DProperty);
	availableDistributionProperties.add(name4DProperty);
	availableDistributionProperties.add(name5DProperty);
	final AggregationProperty name1AProperty = new AggregationProperty("name1", "desc1", "actProperty1", AnalysisPropertyAggregationFunction.AVG);
	name1AProperty.setTableAlias("alias1");
	final AggregationProperty name2AProperty = new AggregationProperty("name2", "desc2", "actProperty2", AnalysisPropertyAggregationFunction.SUM);
	name2AProperty.setTableAlias("alias1");
	final AggregationProperty name3AProperty = new AggregationProperty("name3", "desc3", "actProperty3", AnalysisPropertyAggregationFunction.MAX);
	name3AProperty.setTableAlias("alias1");
	final AggregationProperty name4AProperty = new AggregationProperty("name4", "desc4", "actProperty4", AnalysisPropertyAggregationFunction.MIN);
	name4AProperty.setTableAlias("alias1");
	final AggregationProperty name5AProperty = new AggregationProperty("name5", "desc5", "actProperty5", AnalysisPropertyAggregationFunction.COUNT);
	name5AProperty.setTableAlias("alias1");
	final List<IAggregatedProperty> selectedAggregationProperties = new ArrayList<IAggregatedProperty>();
	selectedAggregationProperties.add(name5AProperty);
	selectedAggregationProperties.add(name3AProperty);
	final List<IAggregatedProperty> availableAggregationProperties = new ArrayList<IAggregatedProperty>();
	availableAggregationProperties.add(name1AProperty);
	availableAggregationProperties.add(name2AProperty);
	availableAggregationProperties.add(name3AProperty);
	availableAggregationProperties.add(name4AProperty);
	availableAggregationProperties.add(name5AProperty);
	final Pair<IAggregatedProperty, SortOrder> orderedProeprty = new Pair<IAggregatedProperty, SortOrder>(name5AProperty, SortOrder.ASCENDING);
	final AnalysisPersistentObject analysis = new AnalysisPersistentObject(name5DProperty, selectedAggregationProperties, orderedProeprty, availableDistributionProperties, availableAggregationProperties, 13, true);
	return analysis;
    }
}
