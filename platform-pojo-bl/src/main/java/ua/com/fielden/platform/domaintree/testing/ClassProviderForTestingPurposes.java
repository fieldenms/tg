package ua.com.fielden.platform.domaintree.testing;

import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.serialisation.api.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;

public class ClassProviderForTestingPurposes extends ProvidedSerialisationClassProvider {

    private static final List<Class<?>> testTypes = new ArrayList<Class<?>>();
    static {
        testTypes.addAll(DefaultSerialisationClassProvider.utilityGeneratedClasses());
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.MiMasterEntityForGlobalDomainTree"));
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
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManager1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1$AbstractAnalysisAddToAggregationTickRepresentation1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1$AbstractAnalysisAddToDistributionTickRepresentation1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeRepresentation1$TickRepresentationForTest"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1$AbstractAnalysisAddToAggregationTickManager1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1$AbstractAnalysisAddToDistributionTickManager1"));
        testTypes.add(findClass("ua.com.fielden.platform.domaintree.testing.DomainTreeManager1$TickManager1ForTest"));
    }

    public ClassProviderForTestingPurposes() {
        this(new Class<?>[0]);
    }

    public ClassProviderForTestingPurposes(final Class<?>... classes) {
        super(concatenate(testTypes.toArray(new Class<?>[0]), classes));
    }

    private static Class<?>[] concatenate(final Class<?>[] array, final Class<?>[] classes) {
        final Class<?> concatenated[] = new Class<?>[array.length + classes.length];
        addToArray(concatenated, 0, array);
        addToArray(concatenated, array.length, classes);
        return concatenated;
    }

    private static void addToArray(final Class<?>[] addTo, final int pos, final Class<?>[] toAdd) {
        for (int toAddIndex = 0; toAddIndex < toAdd.length; toAddIndex++) {
            addTo[pos + toAddIndex] = toAdd[toAddIndex];
        }
    }
}
