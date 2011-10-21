package ua.com.fielden.platform.swing.analysis.ndec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.ChartEntity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.analysis.ndec.dec.NDecModel;
import ua.com.fielden.platform.swing.categorychart.AggregationQueryDataModel;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisModel;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.analysis.NDecReportQueryCriteriaExtender;
import ua.com.fielden.platform.utils.Pair;

public class NDecAnalysisModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisModel<T, DAO>{

    private final NDecReportQueryCriteriaExtender<T, DAO> NDecReportQuery;

    private final AggregationQueryDataModel<T, DAO> aggregationModel;

    private final NDecModel multipleDecModel = new NDecModel();

    public NDecAnalysisModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final String name, final String reportName) {
	super(centerModel, detailsFrame, name, reportName);

	this.NDecReportQuery = new NDecReportQueryCriteriaExtender<T, DAO>();
	this.NDecReportQuery.setBaseCriteria(centerModel.getCriteria());
	this.NDecReportQuery.setAggregationProperties(new ArrayList<IAggregatedProperty>());
	this.NDecReportQuery.setDistributionProperty(null);
	this.NDecReportQuery.setSortOrder(SortOrder.UNSORTED);
	this.NDecReportQuery.setSortingProperty(null);

	this.aggregationModel = new AggregationQueryDataModel<T, DAO>(getQueryExtender());
    }

    protected NDecReportQueryCriteriaExtender<T,DAO> getQueryExtender(){
	return NDecReportQuery;
    }

    protected AggregationQueryDataModel<T, DAO> getAggregationModel() {
	return aggregationModel;
    }

    protected NDecModel getMultipleDecModel() {
	return multipleDecModel;
    }

    @Override
    public Object runAnalysisQuery(final int pageSize) {
	return NDecReportQuery.runExtendedQuery(pageSize);
    }

    @Override
    public void runDoubleClickAction(final AnalysisDoubleClickEvent doubleClickEvent) {
	final ChartMouseEvent chartEvent = (ChartMouseEvent) doubleClickEvent.getSourceMouseEvent();
	final ChartEntity entity = chartEvent.getEntity();
	if (entity instanceof CategoryItemEntity) {
	    createDoubleClickAction(createChoosenItem(((CategoryItemEntity) entity).getColumnKey())).actionPerformed(null);
	} else if (entity instanceof CategoryLabelEntity) {
	    createDoubleClickAction(createChoosenItem(((CategoryLabelEntity) entity).getKey())).actionPerformed(null);
	}
    }

    private List<Pair<IDistributedProperty, Object>> createChoosenItem(final Comparable columnKey) {
	final EntityWrapper entityWrapper = (EntityWrapper) columnKey;
	final List<Pair<IDistributedProperty, Object>> choosenItems = new ArrayList<Pair<IDistributedProperty, Object>>();
	choosenItems.add(new Pair<IDistributedProperty, Object>(NDecReportQuery.getDistributionProperty(), entityWrapper.getEntity()));
	return choosenItems;
    }
}
