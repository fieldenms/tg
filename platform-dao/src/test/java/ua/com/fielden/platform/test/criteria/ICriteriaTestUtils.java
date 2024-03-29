package ua.com.fielden.platform.test.criteria;

import static ua.com.fielden.platform.test.criteria.CriteriaRestorerForTestingPurposes.TYPE_KEY;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.function.Consumer;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

public interface ICriteriaTestUtils {

    <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria mockSelectionCrit(final Class<T> root);
    void mockSelectionCritBindCentreManager(final EnhancedCentreEntityQueryCriteria mockSelectionCrit, final ICentreDomainTreeManagerAndEnhancer centreManager);
    <T> T getInstance(final Class<T> type);

    default <T extends AbstractEntity<?>> CentreContextHolder createCentreContextHolderFor(final Class<T> root) {
        return new CentreContextHolder().setCustomObject(mapOf(t2(TYPE_KEY, root)));
    }

    default <T extends AbstractEntity<?>> ICentreDomainTreeManagerAndEnhancer createCentreManagerFor(final Class<T> root) {
        final ICentreDomainTreeManagerAndEnhancer centreManager = new CentreDomainTreeManagerAndEnhancer(getInstance(EntityFactory.class), setOf(root));
        ((CriteriaRestorerForTestingPurposes) getInstance(ICriteriaEntityRestorer.class)).addCentre(root, centreManager);
        return centreManager;
    }

    default <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria mockSelectionCrit(final Class<T> root, final Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager) {
        final var mockedSelectionCrit = mockSelectionCrit(root);
        final var centreManager = createCentreManagerFor(root);
        enhanceCentreManager.accept(centreManager);
        mockSelectionCritBindCentreManager(mockedSelectionCrit, centreManager);
        return mockedSelectionCrit;
    }

}
