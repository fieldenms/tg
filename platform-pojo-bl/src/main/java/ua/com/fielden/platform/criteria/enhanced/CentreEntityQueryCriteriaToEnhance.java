package ua.com.fielden.platform.criteria.enhanced;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.IDates;

/**
 * This class is the base class to enhance with criteria and resultant properties.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <DAO>
 */
@EntityTitle("Centre Selection Criteria")
public class CentreEntityQueryCriteriaToEnhance<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EnhancedCentreEntityQueryCriteria<T, DAO> {

    @Inject
    protected CentreEntityQueryCriteriaToEnhance(final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider, final IDates dates) {
        super(generatedEntityController, serialiser, controllerProvider, dates);
    }
}
