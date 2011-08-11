package ua.com.fielden.platform.serialisation.ui.impl;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.DefaultTooltipGetter;
import ua.com.fielden.platform.swing.review.AnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.LifecycleAnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyColumnMappingsPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.types.Ordering;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Inject;

public class DefaultUiSerialisationClassProvider extends DefaultSerialisationClassProvider {

    @Inject
    public DefaultUiSerialisationClassProvider(final IApplicationSettings settings) throws Exception {
	super(settings);

	types.add(DynamicCriteriaPersistentObjectUi.class);
	types.add(LocatorPersistentObject.class);
	types.add(PropertyColumnMappingsPersistentObject.class);
	types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-ui/target/classes", "ua.com.fielden.platform.swing.egi", AbstractPropertyColumnMapping.class));
	types.add(PropertyPersistentObject.class);
	types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.utils", Converter.class));
	types.add(EntityUtils.ShowingStrategy.class);
	types.add(DefaultTooltipGetter.class);
	types.add(SortKey.class);
	types.add(SortOrder.class);
	types.add(boolean[].class);
	types.add(LifecycleAnalysisPersistentObject.class);
	types.add(AnalysisPersistentObject.class);
	types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.reportquery", IDistributedProperty.class));
	types.add(Ordering.class);
	types.add(AnalysisPropertyAggregationFunction.class);
	types.add(AnalysisPropertyAggregationFunction.COUNT.getClass());
	types.add(AnalysisPropertyAggregationFunction.DISTINCT_COUNT.getClass());
	types.add(HqlDateFunctions.DAY.getClass());
	types.add(HqlDateFunctions.MONTH.getClass());
	types.add(HqlDateFunctions.YEAR.getClass());
	types.add(PropertyAggregationFunction.NONE.getClass());
	types.add(PropertyAggregationFunction.AVG.getClass());
	types.add(PropertyAggregationFunction.COUNT.getClass());
	types.add(PropertyAggregationFunction.DISTINCT_COUNT.getClass());
	types.add(PropertyAggregationFunction.DISTINCT_COUNT_DAY.getClass());
	types.add(PropertyAggregationFunction.DISTINCT_COUNT_MONTH.getClass());
	types.add(PropertyAggregationFunction.DISTINCT_COUNT_YEAR.getClass());
	types.add(PropertyAggregationFunction.MAX.getClass());
	types.add(PropertyAggregationFunction.MIN.getClass());
	types.add(PropertyAggregationFunction.SUM.getClass());
    }
}
