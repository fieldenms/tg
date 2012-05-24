package ua.com.fielden.platform.domaintree.testing;

import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;

public class ClassProviderForTestingPurposes extends ProvidedSerialisationClassProvider {

    private static final List<Class<?>> testTypes = new ArrayList<Class<?>>();
    static {
	testTypes.addAll(DefaultSerialisationClassProvider.utilityGeneratedClasses());
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager$SearchBy"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation$ListenedArrayList"));
	testTypes.add(findClass("java.util.LinkedHashMap"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.EnhancementSet"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer$ByteArray"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation$Ordering"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$2"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$3"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$4"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$5"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$6"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$7"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$8"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$9"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$10"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.Function$11"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.ICalculatedProperty$CalculatedPropertyCategory"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.ICalculatedProperty$CalculatedPropertyAttribute"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.ICalculatedProperty"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.IDomainTreeEnhancer"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.IDomainTreeRepresentation"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.IDomainTreeManager"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.IDomainTreeRepresentation$ITickRepresentation"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.IDomainTreeManager$ITickManager"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EnhancingEvenSlaverEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EnhancingSlaveEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityForCentreCheckedProperties"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithAbstractNature"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EntityWithoutKeyTitleAndWithKeyType"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterEntityForCentreDomainTree"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterEntityForGlobalDomainTree"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.SlaveEntity"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.SlaveEntityForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.Union1ForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.Union2ForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.UnionEntityForIncludedPropertiesLogic"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1$AbstractAnalysisDomainTreeRepresentationAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManager1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1$AbstractAnalysisDomainTreeRepresentationAndEnhancer1$AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1$AbstractAnalysisDomainTreeRepresentationAndEnhancer1$AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1$AbstractAnalysisAddToAggregationTickRepresentation1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1$AbstractAnalysisAddToDistributionTickRepresentation1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1$TickRepresentationForTest"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1$AbstractAnalysisAddToAggregationTickManager1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1$AbstractAnalysisAddToDistributionTickManager1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1$AbstractAnalysisAddToAggregationTickManagerAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1$AbstractAnalysisAddToDistributionTickManagerAndEnhancer1"));
	testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManager1$TickManager1ForTest"));
    }

    public ClassProviderForTestingPurposes(){
	this(new Class<?>[0]);
    }

    public ClassProviderForTestingPurposes(final Class<?>... classes){
	super(concatenate(testTypes.toArray(new Class<?>[0]), classes));
    }

    private static Class<?>[] concatenate(final Class<?>[] array, final Class<?>[] classes) {
	final Class<?> concatenated[] = new Class<?>[array.length + classes.length];
	addToArray(concatenated, 0, array);
	addToArray(concatenated, array.length, classes);
	return concatenated;
    }

    private static void addToArray(final Class<?>[] addTo, final int pos, final Class<?>[] toAdd) {
	for(int toAddIndex = 0; toAddIndex < toAdd.length; toAddIndex++){
	    addTo[pos + toAddIndex] = toAdd[toAddIndex];
	}
    }
}
