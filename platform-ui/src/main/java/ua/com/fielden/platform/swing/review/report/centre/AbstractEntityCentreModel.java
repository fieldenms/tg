package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public class AbstractEntityCentreModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractEntityReviewModel<T, DTM> {

    private final String name;

    public AbstractEntityCentreModel(final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria, final String name) {
	super(criteria);
	this.name = name;
	// TODO Auto-generated constructor stub
    }

    /**
     * Returns the name of the entity centre. If the name is null then entity centre is principle, otherwise it is non principle entity centre.
     * 
     * @return
     */
    public String getName() {
	return name;
    }

    /**
     * Determines whether this entity centre's model is valid or not.
     * 
     * @return
     */
    public Result validate() {
	// TODO Auto-generated method stub
	return null;
    }

}
