package ua.com.fielden.platform.test.criteria;

import static ua.com.fielden.platform.test.criteria.CriteriaRestorerForTestingPurposes.TYPE_KEY;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.function.Consumer;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * Interface for constructing mocked selection criteria entities for tests.
 * The main method is {@link #mockSelectionCrit(Class, Consumer)} that creates mocked criteria entity for some root type and its centre enhancements.
 */
public interface ICriteriaTestUtils {

    /**
     * Creates mocked criteria entity for type {@code root}.
     * <p>
     * It is expected that following methods will be mocked here:
     * <ul>
     *   <li>{@link EnhancedCentreEntityQueryCriteria#centreContextHolder()} with help of {@link #createCentreContextHolderFor(Class)}</li>
     *   <li>{@link EnhancedCentreEntityQueryCriteria#getEntityClass()} with {@code root} instance</li>
     * </ul>
     */
    <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria mockSelectionCrit(final Class<T> root);
    /**
     * Binds {@code centreManager} to {@code mockSelectionCrit}.
     * <p>
     * It is expected that {@link EntityQueryCriteria#getCentreDomainTreeMangerAndEnhancer()} method will be mocked here with help of {@link #createCentreManagerFor(Class)}
     */
    void mockSelectionCritBindCentreManager(final EnhancedCentreEntityQueryCriteria mockSelectionCrit, final ICentreDomainTreeManagerAndEnhancer centreManager);
    /**
     * Method to get instance from {@link Injector} by its type.
     */
    <T> T getInstance(final Class<T> type);

    /**
     * Creates {@link CentreContextHolder} with {@code root} type indication. Used later for criteria restoration in {@link CriteriaRestorerForTestingPurposes}.
     */
    default <T extends AbstractEntity<?>> CentreContextHolder createCentreContextHolderFor(final Class<T> root) {
        return new CentreContextHolder().setCustomObject(mapOf(t2(TYPE_KEY, root)));
    }

    /**
     * Creates empty centre manager and registers it by {@code root} type in {@link CriteriaRestorerForTestingPurposes}.
     */
    default <T extends AbstractEntity<?>> ICentreDomainTreeManagerAndEnhancer createCentreManagerFor(final Class<T> root) {
        final ICentreDomainTreeManagerAndEnhancer centreManager = new CentreDomainTreeManagerAndEnhancer(getInstance(EntityFactory.class), setOf(root));
        ((CriteriaRestorerForTestingPurposes) getInstance(ICriteriaEntityRestorer.class)).addCentre(root, centreManager);
        return centreManager;
    }

    /**
     * Creates mocked selection criteria for {@code root} entity type.
     * <p>
     * It is expected that following methods will be mocked:
     * <ul>
     *   <li>{@link EnhancedCentreEntityQueryCriteria#centreContextHolder()}</li>
     *   <li>{@link EnhancedCentreEntityQueryCriteria#getEntityClass()}</li>
     *   <li>{@link EntityQueryCriteria#getCentreDomainTreeMangerAndEnhancer()}</li>
     * </ul>
     * 
     * @param enhanceCentreManager -- function to adjust empty centre manager from which criteria entity will be created
     */
    default <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria mockSelectionCrit(final Class<T> root, final Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager) {
        final var mockedSelectionCrit = mockSelectionCrit(root);
        final var centreManager = createCentreManagerFor(root);
        enhanceCentreManager.accept(centreManager);
        mockSelectionCritBindCentreManager(mockedSelectionCrit, centreManager);
        return mockedSelectionCrit;
    }

}
