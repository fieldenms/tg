package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdaterDefaultAction}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdaterDefaultAction.class)
public class CentreConfigUpdaterDefaultActionDao extends CommonEntityDao<CentreConfigUpdaterDefaultAction> implements ICentreConfigUpdaterDefaultAction {
    
    @Inject
    public CentreConfigUpdaterDefaultActionDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
    }
    
}
