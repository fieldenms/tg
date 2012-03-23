package ua.com.fielden.platform.swing.review.configuration.persistens;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.DefaultTooltipGetter;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.types.Ordering;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

public class DynamicCriteriaSerialisationClassProvider implements ISerialisationClassProvider {

    private final List<Class<?>> types = new ArrayList<Class<?>>();

    public DynamicCriteriaSerialisationClassProvider() {
	try {
	    types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-ui/target/classes", "ua.com.fielden.platform.swing.egi", AbstractPropertyColumnMapping.class));
	    types.add(PropertyPersistentObject.class);
	    types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.utils", Converter.class));
	    types.add(EntityUtils.ShowingStrategy.class);
	    types.add(DefaultTooltipGetter.class);
	    types.add(SortKey.class);
	    types.add(SortOrder.class);
	    types.add(boolean[].class);
	    types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.reportquery", IDistributedProperty.class));
	    types.add(CategoryForTesting.class);
	    types.add(Ordering.class);
	    types.addAll(EntityUtils.extractTypes(AnalysisPropertyAggregationFunction.class));
	    types.addAll(EntityUtils.extractTypes(HqlDateFunctions.class));
	    types.add(SerialisationEntity.class);
	    types.add(NestedSerialisationEntity.class);
	    types.addAll(EntityUtils.extractTypes(PropertyAggregationFunction.class));
	    types.addAll(EntityUtils.extractTypes(MnemonicEnum.class));
	    types.addAll(EntityUtils.extractTypes(DateRangePrefixEnum.class));
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public List<Class<?>> classes() {
	return types;
    }

}
