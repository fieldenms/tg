package ua.com.fielden.platform.swing.categorychart;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.reportquery.AbstractCategoryChartEntryModel;
import ua.com.fielden.platform.reportquery.ChartModelChangedEvent;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;
import ua.com.fielden.platform.swing.review.analysis.GroupReportQueryCriteriaExtender;
import ua.com.fielden.platform.types.Money;

/**
 * The implementation of the {@link ICategoryChartEntryModel} interface. This implementation uses the list of {@link EntityAggregates} to populate the data set of the category
 * chart.
 * 
 * @author oleh
 * 
 */
public class AggregationQueryDataModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractCategoryChartEntryModel {

    private final List<EntityAggregates> aggregation = new ArrayList<EntityAggregates>();

    private final GroupReportQueryCriteriaExtender<T, DAO, ?> analysisReportCriteria;

    public AggregationQueryDataModel(final GroupReportQueryCriteriaExtender<T, DAO, ?> analysisReportCriteria) {
	this.analysisReportCriteria = analysisReportCriteria;
    }

    @Override
    public Comparable<?> getCategory(final int index) {
	if (analysisReportCriteria.getDistributionProperty() == null) {
	    return "category";
	}
	final Comparable<?> entity = (Comparable<?>) aggregation.get(index).getValueByKey(analysisReportCriteria.getAliasForDistributionProperty());
	return new EntityWrapper(entity);
    }

    @Override
    public int getCategoryCount() {
	return aggregation.size();
    }

    @Override
    public Comparable<?> getSeries(final int index) {
	final IAggregatedProperty aggregationProperty = analysisReportCriteria.getAggregationProperties().get(index);
	return aggregationProperty.toString();
    }

    @Override
    public int getSeriesCount() {
	return analysisReportCriteria.getAggregationProperties().size();
    }

    @Override
    public Number getValue(final int row, final int column) {
	final Object value = aggregation.get(column).getValueByKey(analysisReportCriteria.getAliasForAggregationProperty(row));
	if (value instanceof Money) {
	    return ((Money) value).getAmount();
	} else if (value instanceof Number) {
	    return (Number) value;
	}
	//TODO Throw exception otherwise. Consider.
	return 0;
    }

    public List<EntityAggregates> getModel(){
	return new ArrayList<EntityAggregates>(aggregation);
    }

    public void setModel(final List<EntityAggregates> aggregation){
	this.aggregation.clear();
	if(aggregation!=null){
	    this.aggregation.addAll(aggregation);
	}
	notifyChartModelChanged(new ChartModelChangedEvent(this));
    }

}