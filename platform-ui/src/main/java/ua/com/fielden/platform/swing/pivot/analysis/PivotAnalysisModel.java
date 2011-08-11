package ua.com.fielden.platform.swing.pivot.analysis;

import java.io.File;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.groupanalysis.GroupAnalysisModel;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.analysis.PivotAnalysisQueryExtender;

public class PivotAnalysisModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupAnalysisModel<T, DAO> {

    private final PivotAnalysisQueryExtender<T, DAO> analysisReportQuery;

    private PivotAnalysisReview<T, DAO> analysisReview;

    public PivotAnalysisModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final String name, final String reportName) {
	super(centerModel, detailsFrame, name, reportName);
	this.analysisReportQuery = new PivotAnalysisQueryExtender<T, DAO>();
	this.analysisReportQuery.setBaseCriteria(centerModel.getCriteria());
	this.analysisReportQuery.setAggregationProperties(null);
	this.analysisReportQuery.setDistributionProperties(null);
    }

    public void setAnalysisReview(final PivotAnalysisReview<T, DAO> analysisReview) {
	if (this.analysisReview == null) {
	    this.analysisReview = analysisReview;
	} else {
	    throw new IllegalStateException("It is possible to set pivot analysis view just once");
	}
    }

    public PivotAnalysisReview<T, DAO> getAnalysisReview() {
	return analysisReview;
    }

    @Override
    public Object runAnalysisQuery(final int pageSize) {
	final PivotAnalysisDataProvider<T, DAO> dataProvider = getAnalysisReview().getAnalysisReportModel().getDataProvider();
	dataProvider.loadData(getAnalysisData());
	return dataProvider.getData();
    }

    private List<? extends IBindingEntity> getAnalysisData() {
	return analysisReportQuery.runExtendedQuery(0);
    }

    @Override
    public void runDoubleClickAction(final AnalysisDoubleClickEvent analysisEvent) {
	createDoubleClickAction((List) analysisEvent.getSource()).actionPerformed(null);
    }

    void updateCriteria(final List<IDistributedProperty> selectedDistribution, final List<IAggregatedProperty> aggregationProperties) {
	final List<IDistributedProperty> distributionProperties = selectedDistribution;
	if (distributionProperties == null || distributionProperties.isEmpty()) {
	    throw new IllegalStateException("Please choose distribution property");
	}
	analysisReportQuery.setDistributionProperties(distributionProperties);
	final List<IAggregatedProperty> properties = aggregationProperties;
	analysisReportQuery.setAggregationProperties(properties);
    }

    String getAliasFor(final IDistributedProperty distributionProperty) {
	return analysisReportQuery.getAliasFor(distributionProperty);
    }

    public Result exportIntoFile(final File file) {
	return getAnalysisReview().getAnalysisReportModel().exportDataIntoFile(file, getAnalysisData());
    }

}
