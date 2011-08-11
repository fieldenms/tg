package ua.com.fielden.platform.swing.review;

import javax.swing.JPanel;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.model.UmCustomEntityCentre;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.UvCustomEntityCentre;

/**
 * {@link DynamicEntityReview} for details view of the analysis reports.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class DetailsDynamicEntityReview<T extends AbstractEntity, DAO extends IEntityDao<T>> extends UvCustomEntityCentre<T, DAO, EntityQueryCriteria<T, DAO>,BaseFrame,UmCustomEntityCentre<T,DAO,EntityQueryCriteria<T, DAO>,BaseFrame>> {

    private static final long serialVersionUID = 8941357659824182994L;

    public DetailsDynamicEntityReview(final UmCustomEntityCentre<T,DAO,EntityQueryCriteria<T, DAO>,BaseFrame> model, final boolean showRecords) {
	super(model, showRecords);
    }

    @Override
    protected JPanel createCriteriaPanel(final EntityReviewModel<T, DAO, EntityQueryCriteria<T, DAO>> model) {
	return null;
    }

    @Override
    public ICloseGuard canClose() {
	return null;
    }

    @Override
    public String whyCannotClose() {
	return "Should be able to close";
    }

    @Override
    public String getInfo() {
	return null;
    }
}
