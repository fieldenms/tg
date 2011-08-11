package ua.com.fielden.platform.swing.groupanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteriaWithParameter;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReview;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.Pair;

public abstract class GroupAnalysisModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportModel<T, DAO> {

    public GroupAnalysisModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final String name, final String reportName) {
	super(centerModel, detailsFrame, name, reportName);
    }

    protected final Action createDoubleClickAction(final List<Pair<IDistributedProperty, Object>> choosenItems) {
	return new Command<Void>("Details") {

	    private static final long serialVersionUID = 1986658954874008023L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		final List<Object> entityKey = createKey(choosenItems);
		DetailsFrame detailsFrame = getDetailsFrame(entityKey);
		if (detailsFrame == null && getCenterModel().getCriteria() instanceof DynamicEntityQueryCriteria) {
		    final EntityQueryCriteria<T, DAO> criteria = new DynamicEntityQueryCriteriaWithParameter<T, DAO>((DynamicEntityQueryCriteria<T, DAO>) getCenterModel().getCriteria(), choosenItems);
		    final EntityReview<T, DAO, EntityQueryCriteria<T, DAO>> entityReview = getDetailsEntityReview(criteria);
		    final String frameTitle = createFrameTitle(choosenItems);
		    detailsFrame = new DetailsFrame(entityKey, frameTitle, entityReview, new ICloseHook<DetailsFrame>() {

			@Override
			public void closed(final DetailsFrame frame) {
			    removeDetailsFrame(frame);
			}

		    });
		    addDetailsFrame(detailsFrame);
		}
		detailsFrame.setVisible(true);

	    }

	    private String createFrameTitle(final List<Pair<IDistributedProperty, Object>> choosenItems) {
		return "Details for " + createDistributionPropertyTitle(choosenItems) + " " + createDistributionEntitiesTitle(choosenItems) + " (" + getReportName() + ": "
			+ getName() + ")";
	    }

	    private String createDistributionEntitiesTitle(final List<Pair<IDistributedProperty, Object>> choosenItems) {
		String titles = "";

		for (final Pair<IDistributedProperty, Object> pair : choosenItems) {
		    titles += ", " + createPairString(pair.getValue());
		}
		return titles.isEmpty() ? titles : titles.substring(2);
	    }

	    private String createPairString(final Object value) {
		if (value instanceof AbstractEntity) {
		    return ((AbstractEntity) value).getKey().toString() + " \u2012 " + ((AbstractEntity) value).getDesc();
		} else if (value != null) {
		    return value.toString() + " \u2012 " + value.toString();
		} else {
		    return "UNKNOWN \u2012 UNKNOWN";
		}
	    }

	    private String createDistributionPropertyTitle(final List<Pair<IDistributedProperty, Object>> choosenItems) {
		String name = "";

		for (final Pair<IDistributedProperty, Object> pair : choosenItems) {
		    name += '\u2192' + pair.getKey().toString();
		}
		return name.isEmpty() ? name : name.substring(1);

	    }

	    private List<Object> createKey(final List<Pair<IDistributedProperty, Object>> choosenItems) {
		final List<Object> keyList = new ArrayList<Object>();
		for (final Pair<IDistributedProperty, Object> pair : choosenItems) {
		    keyList.add(pair.getValue());
		}
		return keyList;
	    }

	};
    }
}
