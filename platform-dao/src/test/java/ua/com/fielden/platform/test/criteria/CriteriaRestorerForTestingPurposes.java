package ua.com.fielden.platform.test.criteria;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

public class CriteriaRestorerForTestingPurposes implements ICriteriaEntityRestorer {

    static final String TYPE_KEY = "@@type";
    private final ICriteriaGenerator criteriaGenerator;
    private final Map<Class<? extends AbstractEntity<?>>, ICentreDomainTreeManagerAndEnhancer> centres = new HashMap<>();

    @Inject
    public CriteriaRestorerForTestingPurposes(final ICriteriaGenerator criteriaGenerator) {
        this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder) {
        final Class<? extends AbstractEntity<?>> type = (Class<? extends AbstractEntity<?>>) centreContextHolder.getCustomObject().get(TYPE_KEY);
        return criteriaGenerator.generateCentreQueryCriteria(centres.get(type));
    }

    public CriteriaRestorerForTestingPurposes addCentre(final Class<? extends AbstractEntity<?>> type, final ICentreDomainTreeManagerAndEnhancer centre) {
        this.centres.put(type, centre);
        return this;
    }

}