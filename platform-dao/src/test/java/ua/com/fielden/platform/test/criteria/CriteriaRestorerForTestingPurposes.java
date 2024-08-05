package ua.com.fielden.platform.test.criteria;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * {@link ICriteriaEntityRestorer} implementation for tests.
 * <p>
 * With this implementation we use standard criteria generation from {@link ICentreDomainTreeManagerAndEnhancer} instance,
 * that was created and put into {@code centres} map by its root type.
 * <p>
 * All preparations for criteria entity mocking is done through {@link ICriteriaTestUtils#mockSelectionCrit(Class, java.util.function.Consumer)} method.
 * <p>
 * This implementation should be bound as singleton in test-related IoC module.
 */
@Singleton
public class CriteriaRestorerForTestingPurposes implements ICriteriaEntityRestorer {
    protected static final String TYPE_KEY = "@@type";
    /**
     * Singleton {@link ICriteriaGenerator} instance for criteria generation.
     */
    private final ICriteriaGenerator criteriaGenerator;
    /**
     * Cached centre managers by their root types.
     * <p>
     * No need to have {@link ConcurrentHashMap} here because every VM, that runs tests, do that sequentially. Also, {@code centreManager}s replacements are possible from test to test.
     */
    private final Map<Class<? extends AbstractEntity<?>>, ICentreDomainTreeManagerAndEnhancer> centreManagersByType = new HashMap<>();

    @Inject
    public CriteriaRestorerForTestingPurposes(final ICriteriaGenerator criteriaGenerator) {
        this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    public EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder) {
        return criteriaGenerator.generateCentreQueryCriteria(centreManagersByType.get(centreContextHolder.getCustomObject().get(TYPE_KEY)));
    }

    /**
     * Adds {@code centreManager} by its {@code type} to this instance. Replaces previous centre manager, if there was such.
     * 
     * @param type
     * @param centreManager
     */
    public void addCentre(final Class<? extends AbstractEntity<?>> type, final ICentreDomainTreeManagerAndEnhancer centreManager) {
        centreManagersByType.put(type, centreManager);
    }

}
